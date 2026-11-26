package backend.manuhub.matchevent;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchService;
import backend.manuhub.matchevent.dto.MatchEventListGetResponse;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchEventSchedulerTest {

    private static final Long MATCH_ID = 1557450L;

    @Mock
    private MatchEventService matchEventService;
    @Mock private MatchService matchService;
    @Mock private MatchEventUpdateService matchEventUpdateService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private MatchEventScheduler scheduler;

    @Test
    @DisplayName("진행 중인 경기가 없으면 아무것도 호출하지 않는다")
    void noLiveMatches() {
        given(matchService.findLiveMatches()).willReturn(List.of());

        scheduler.updateLiveMatchEvents();

        verifyNoInteractions(matchEventService, matchEventUpdateService, eventPublisher);
    }

    @Test
    @DisplayName("이벤트 변경이 없으면 저장도 발행도 하지 않는다")
    void notChanged() {
        Match match = liveMatch();
        List<MatchEventResponse> latest = List.of();
        given(matchService.findLiveMatches()).willReturn(List.of(match));
        given(matchEventService.getLiveMatchEvents(MATCH_ID)).willReturn(latest);
        given(matchEventService.isChanged(latest, MATCH_ID)).willReturn(false);

        scheduler.updateLiveMatchEvents();

        verifyNoInteractions(matchEventUpdateService, eventPublisher);
    }

    @Test
    @DisplayName("이벤트가 변경되면 저장하고 변경 이벤트를 발행한다")
    void changed() {
        Match match = liveMatch();
        List<MatchEventResponse> latest = List.of();
        MatchEventListGetResponse saved = new MatchEventListGetResponse(MATCH_ID, List.of());
        given(matchService.findLiveMatches()).willReturn(List.of(match));
        given(matchEventService.getLiveMatchEvents(MATCH_ID)).willReturn(latest);
        given(matchEventService.isChanged(latest, MATCH_ID)).willReturn(true);
        given(matchEventUpdateService.updateLiveMatchEvents(latest, match)).willReturn(saved);

        scheduler.updateLiveMatchEvents();

        ArgumentCaptor<LiveMatchEventsChanged> captor = ArgumentCaptor.forClass(LiveMatchEventsChanged.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().events().matchId()).isEqualTo(MATCH_ID);
    }

    @Test
    @DisplayName("외부 API 오류가 나도 예외가 밖으로 나가지 않고 저장·발행도 하지 않는다")
    void apiFails() {
        Match match = liveMatch();
        given(matchService.findLiveMatches()).willReturn(List.of(match));
        given(matchEventService.getLiveMatchEvents(MATCH_ID)).willThrow(new RuntimeException("api error"));

        assertThatCode(() -> scheduler.updateLiveMatchEvents()).doesNotThrowAnyException();

        verifyNoInteractions(matchEventUpdateService, eventPublisher);
    }

    private Match liveMatch() {
        Match match = mock(Match.class);
        given(match.getMatchId()).willReturn(MATCH_ID);
        return match;
    }
}
