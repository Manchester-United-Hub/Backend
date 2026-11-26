package backend.manuhub.matchevent;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchService;
import backend.manuhub.matchevent.dto.MatchEventListGetResponse;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEventScheduler {

    private final MatchEventService matchEventService;
    private final MatchService matchService;
    private final MatchEventUpdateService matchEventUpdateService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 * * * * *")
    public void updateLiveMatchEvents() {
        // 진행중인 경기를 찾는다.
        List<Match> liveMatches = matchService.findLiveMatches();
        // 진행중인 경기가 없다면 return
        if (liveMatches.isEmpty()) return;

        Match match = liveMatches.getFirst();
        try {
            // 풋볼 API 에 중계 데이터를 요청하고 받는다.
            List<MatchEventResponse> liveMatchEvents = matchEventService.getLiveMatchEvents(match.getMatchId());

            // 기존 DB 에 있던 중계 데이터하고 동일하다면 return;
            if (!matchEventService.isChanged(liveMatchEvents, match.getMatchId())) {
                return;
            }
            /**
             * 기존 DB에 있던 중계 데이터와 다르다면 아래 작업들을 수행한다.
             */
            // 라이브 매치 중계 데이터들을 저장한다.
            MatchEventListGetResponse updatedLiveMatchEvents = matchEventUpdateService.updateLiveMatchEvents(liveMatchEvents, match);
            /**
             * eventPublisher 를 통해 해당 이벤트 발행
             * -> sse 이벤트 리스너에서 해당 이벤트 타입 sseService.sendToClient() 호출
             */
            eventPublisher.publishEvent(new LiveMatchEventsChanged(updatedLiveMatchEvents));
        } catch (Exception e) {
            log.error("[MatchEventScheduler] updateLiveMatchEvents failed. matchId={}", match.getMatchId(), e);
        }
    }
}
