package backend.manuhub.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchSseController implements MatchSseAPI{

    private final MatchSseService matchSseService;

    // 경기별 SSE 연결. 종료된 경기는 연결하지 않고 204로 응답한다.
    @GetMapping(value = "/{matchId}/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@PathVariable Long matchId) {
        return matchSseService.connect(matchId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
