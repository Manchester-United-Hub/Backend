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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSseService {

    private static final long TIMEOUT_MS = 2 * 60 * 60 * 1000L;

    private final MatchSseRegistry matchSseRegistry;
    private final MatchRepository matchRepository;
    private final MatchLineupService matchLineupService;

    // 연결 직후의 응답과 저장 후 갱신이 역순으로 도착하지 않도록 경기별 전송을 직렬화한다.
    private final Object[] matchLocks = IntStream.range(0, 64).mapToObj(i -> new Object()).toArray();

    public Optional<SseEmitter> connect(Long matchId) {
        // 1. 경기 확인: 없으면 404
        Match match = findMatch(matchId);

        // 2. 종료된 경기면 연결하지 않음 (컨트롤러가 204로 응답)
        if (match.isFinished()) {
            return Optional.empty();
        }

        // 3. 연결 객체를 만들고, 끝날 때 정리되도록 콜백을 붙임
        SseEmitter emitter = createEmitter(matchId);

        synchronized (lockFor(matchId)) {
            // 연결 등록과 초기 전송 사이에 갱신 메시지가 끼어들지 않도록 순서만 보호한다.
            MatchLineupResponse lineup = matchLineupService.getLineup(matchId);
            // 4. 준비가 끝났으니 보관함에 등록
            matchSseRegistry.register(matchId, emitter);
            // 5. 기존 연결 성공 신호를 그대로 보낸 다음 라인업을 추가로 전송한다.
            if (sendConnectEvent(matchId, emitter)) {
                // 기존 connect 응답에 더해 같은 SSE 연결로 라인업을 별도 이벤트로 보낸다.
                sendEvent(matchId, emitter, "match-lineup.updated", lineup);
            }
        }

        return Optional.of(emitter);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLineupRefresh(MatchLineupRefreshEvent event) {
        try {
            synchronized (lockFor(event.matchId())) {
                List<SseEmitter> subscribers = matchSseRegistry.getEmitters(event.matchId());
                if (subscribers.isEmpty()) return;

                // 커밋된 데이터를 경기당 한 번 조합해 모든 구독자에게 같은 스냅샷을 보낸다.
                MatchLineupResponse lineup = matchLineupService.getLineup(event.matchId());
                for (SseEmitter subscriber : subscribers) {
                    sendEvent(event.matchId(), subscriber, "match-lineup.updated", lineup);
                }
            }
        } catch (Exception e) {
            // SSE 장애가 이미 성공한 DB 저장을 실패로 바꾸지 않도록 전송 실패만 기록한다.
            log.warn("[MatchSseService] Lineup broadcast failed. matchId={}", event.matchId(), e);
        }
    }

    // 이벤트가 없어도 프록시가 유휴 연결을 끊지 않도록 구독자에게 주기적으로 신호를 보낸다.
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

    private boolean sendConnectEvent(Long matchId, SseEmitter emitter) {
        // 기존 connect 이벤트 형식을 그대로 유지한다.
        return sendEvent(matchId, emitter, "connect", "connected");
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
