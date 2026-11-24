package backend.manuhub.sse;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.MatchNotFoundException;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import backend.manuhub.lineup.MatchLineupService;
import backend.manuhub.lineup.MatchLineupRefreshEvent;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSseService {

    private static final long TIMEOUT_MS = 2 * 60 * 60 * 1000L;

    private final MatchSseRegistry matchSseRegistry;
    private final MatchRepository matchRepository;
    private final MatchLineupService matchLineupService;

    // 최초 응답과 갱신 전송의 순서가 뒤집히지 않게 직렬화하고, 고정 개수 락으로 메모리 증가를 막는다.
    private final Object[] matchLocks = IntStream.range(0, 64).mapToObj(i -> new Object()).toArray();

    /** 재연결 중 놓친 변경도 복구하도록 연결할 때마다 DB의 현재 전체 라인업을 전송한다. */
    public Optional<SseEmitter> connect(Long matchId) {
        synchronized (lockFor(matchId)) {
            Match match = findMatch(matchId);
            if (match.isFinished()) {
                return Optional.empty();
            }

            MatchLineupResponse lineup = matchLineupService.getLineup(matchId);
            SseEmitter emitter = createEmitter(matchId);
            matchSseRegistry.register(matchId, emitter);

            if (sendEvent(matchId, emitter, "connect", "connected")) {
                sendEvent(matchId, emitter, "match-lineup.updated", lineup);
            }
            return Optional.of(emitter);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLineupRefresh(MatchLineupRefreshEvent event) {
        try {
            synchronized (lockFor(event.matchId())) {
                List<SseEmitter> subscribers = matchSseRegistry.getEmitters(event.matchId());
                if (subscribers.isEmpty()) return;

                // 커밋된 상태를 경기당 한 번만 조합해 구독자 수만큼 DB 조회가 반복되지 않게 한다.
                MatchLineupResponse lineup = matchLineupService.getLineup(event.matchId());
                for (SseEmitter subscriber : subscribers) {
                    sendEvent(event.matchId(), subscriber, "match-lineup.updated", lineup);
                }
            }
        } catch (Exception e) {
            // SSE 전송 장애가 이미 성공한 DB 저장을 수집 실패처럼 처리하게 만들지 않는다.
            log.warn("[MatchSseService] Lineup broadcast failed. matchId={}", event.matchId(), e);
        }
    }

    // 경기 이벤트가 오랫동안 없어도 중간 프록시가 유휴 연결로 판단해 끊지 않도록 신호를 보낸다.
    @Scheduled(fixedDelay = 25000)
    public void sendHeartbeats() {
        for (Long matchId : matchSseRegistry.getSubscribedMatchIds()) {
            synchronized (lockFor(matchId)) {
                for (SseEmitter emitter : matchSseRegistry.getEmitters(matchId)) {
                    sendEvent(matchId, emitter, "heartbeat", "ping");
                }
            }
        }
    }

    private Object lockFor(Long matchId) {
        return matchLocks[Math.floorMod(matchId.hashCode(), matchLocks.length)];
    }

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(ErrorCode.MATCH_NOT_FOUND_ERROR));
    }

    private SseEmitter createEmitter(Long matchId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);

        // 연결이 어떤 이유로든 끝나면 레지스트리에서 뺀다.
        emitter.onCompletion(() -> disconnect(matchId, emitter)); // 정상 종료, 클라이언트 close/탭 닫기
        emitter.onTimeout(emitter::complete);                      // 타임아웃 → complete() → onCompletion으로 해제
        emitter.onError(e -> disconnect(matchId, emitter));        // 전송 실패 등

        return emitter;
    }

    private void disconnect(Long matchId, SseEmitter emitter) {
        // 같은 연결에 콜백이 여러 번 불려도 안전하다 (이미 없으면 아무 일도 안 한다).
        matchSseRegistry.unregister(matchId, emitter);
        log.info("[MatchSseService] disconnected. matchId={}", matchId);
    }

    private boolean sendEvent(Long matchId, SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
            return true;
        } catch (IOException | IllegalStateException e) {
            disconnect(matchId, emitter);
            emitter.completeWithError(e);
            return false;
        }
    }
}
