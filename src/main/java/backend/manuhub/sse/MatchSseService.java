package backend.manuhub.sse;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.MatchNotFoundException;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSseService {

    private static final long TIMEOUT_MS = 2 * 60 * 60 * 1000L;

    private final MatchSseRegistry matchSseRegistry;
    private final MatchRepository matchRepository;

    public Optional<SseEmitter> connect(Long matchId) {
        // 1. 경기 확인: 없으면 404
        Match match = findMatch(matchId);

        // 2. 종료된 경기면 연결하지 않음 (컨트롤러가 204로 응답)
        if (match.isFinished()) {
            return Optional.empty();
        }

        // 3. 연결 객체를 만들고, 끝날 때 정리되도록 콜백을 붙임
        SseEmitter emitter = createEmitter(matchId);

        // 4. 준비가 끝났으니 보관함에 등록
        matchSseRegistry.register(matchId, emitter);

        // 5. 연결됐다는 첫 신호 전송
        sendConnectEvent(matchId, emitter);

        return Optional.of(emitter);
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

    private void sendConnectEvent(Long matchId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            disconnect(matchId, emitter);
        }
    }
}
