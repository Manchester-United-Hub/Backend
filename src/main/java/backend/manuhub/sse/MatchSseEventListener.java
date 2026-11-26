package backend.manuhub.sse;

import backend.manuhub.matchevent.LiveMatchEventsChanged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchSseEventListener {

    private final MatchSseService sseService;

    @EventListener
    public void handleLiveMatchEventsChanged(LiveMatchEventsChanged event) {
        log.info("[MatchSseEventListener] live events changed --> matchId={}", event.events().matchId());
        // TODO : sseService.send(event.events().matchId(), "match-events", event.events());
    }

    // 라인업 이벤트


    // 경기 이벤트 (스코어, 경기진행상태)
}
