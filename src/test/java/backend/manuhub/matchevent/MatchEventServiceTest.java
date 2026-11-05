package backend.manuhub.matchevent;

import backend.manuhub.exception.MatchNotFoundException;
import backend.manuhub.external.matchevent.MatchEventApiResponse;
import backend.manuhub.external.matchevent.MatchEventClient;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import backend.manuhub.matchevent.dto.MatchEventListGetResponse;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MatchEventServiceTest {

    private static final Long MATCH_ID = 1557450L;

    @Mock
    private MatchEventUpdateService matchEventUpdateService;
    @Mock private MatchEventRepository matchEventRepository;
    @Mock private MatchEventClient matchEventClient;
    @Mock private MatchRepository matchRepository;
    @InjectMocks private MatchEventService matchEventService;

    @Captor private ArgumentCaptor<List<MatchEventResponse>> responsesCaptor;

    @Test
    @DisplayName("경기가 없으면 MatchNotFoundException이 발생하고 조회·API 호출을 하지 않는다")
    void getMatchEvents_matchNotFound() {
        given(matchRepository.findById(MATCH_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> matchEventService.getMatchEvents(MATCH_ID))
                .isInstanceOf(MatchNotFoundException.class);

        verify(matchEventRepository, never()).findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(any());
        verify(matchEventClient, never()).fetchEvents(any());
    }

    @Test
    @DisplayName("저장된 이벤트가 있으면 API를 호출하지 않고 그대로 반환한다")
    void getMatchEvents_returnsStoredEvents() {
        Match match = mock(Match.class);
        given(matchRepository.findById(MATCH_ID)).willReturn(Optional.of(match));
        given(matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(MATCH_ID))
                .willReturn(List.of(entity(match, MatchEventType.GOAL)));

        MatchEventListGetResponse response = matchEventService.getMatchEvents(MATCH_ID);

        assertThat(response.matchId()).isEqualTo(MATCH_ID);
        assertThat(response.events()).hasSize(1);
        verify(matchEventClient, never()).fetchEvents(any());
        verify(matchEventUpdateService, never()).updateMatchEvents(any(), any());
    }

    @Test
    @DisplayName("저장된 이벤트가 없으면 API에서 가져와 저장하고, 재조회한 결과를 반환한다")
    void getMatchEvents_fallbackSavesAndReturnsReloaded() {
        Match match = mock(Match.class);
        given(matchRepository.findById(MATCH_ID)).willReturn(Optional.of(match));
        given(matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(MATCH_ID))
                .willReturn(List.of())
                .willReturn(List.of(entity(match, MatchEventType.GOAL)));
        given(matchEventClient.fetchEvents(MATCH_ID)).willReturn(List.of(apiEvent("Goal", 34, null)));

        MatchEventListGetResponse response = matchEventService.getMatchEvents(MATCH_ID);

        verify(matchEventUpdateService).updateMatchEvents(eq(match), responsesCaptor.capture());
        assertThat(responsesCaptor.getValue()).hasSize(1);
        verify(matchEventRepository, times(2))
                .findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(MATCH_ID);
        assertThat(response.events()).hasSize(1);
    }

    @Test
    @DisplayName("API 응답도 비어 있으면 저장·재조회 없이 빈 목록을 반환한다")
    void getMatchEvents_emptyApiResponse() {
        Match match = mock(Match.class);
        given(matchRepository.findById(MATCH_ID)).willReturn(Optional.of(match));
        given(matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(MATCH_ID))
                .willReturn(List.of());
        given(matchEventClient.fetchEvents(MATCH_ID)).willReturn(List.of());

        MatchEventListGetResponse response = matchEventService.getMatchEvents(MATCH_ID);

        assertThat(response.matchId()).isEqualTo(MATCH_ID);
        assertThat(response.events()).isEmpty();
        verify(matchEventUpdateService, never()).updateMatchEvents(any(), any());
        verify(matchEventRepository, times(1))
                .findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(MATCH_ID);
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입은 제외하고 나머지만 저장한다")
    void getMatchEvents_skipsUnknownType() {
        Match match = mock(Match.class);
        stubEmptyStoredEvents(match);
        given(matchEventClient.fetchEvents(MATCH_ID))
                .willReturn(List.of(apiEvent("Unknown", 10, null), apiEvent("Goal", 34, null)));

        matchEventService.getMatchEvents(MATCH_ID);

        verify(matchEventUpdateService).updateMatchEvents(any(), responsesCaptor.capture());
        assertThat(responsesCaptor.getValue()).hasSize(1);
        assertThat(responsesCaptor.getValue().get(0).type()).isEqualTo(MatchEventType.GOAL);
    }

    @Test
    @DisplayName("알 수 없는 타입만 있으면 저장하지 않는다")
    void getMatchEvents_onlyUnknownTypes() {
        Match match = mock(Match.class);
        stubEmptyStoredEvents(match);
        given(matchEventClient.fetchEvents(MATCH_ID)).willReturn(List.of(apiEvent("Unknown", 10, null)));

        MatchEventListGetResponse response = matchEventService.getMatchEvents(MATCH_ID);

        assertThat(response.events()).isEmpty();
        verify(matchEventUpdateService, never()).updateMatchEvents(any(), any());
    }

    @Test
    @DisplayName("API 타입 문자열(Goal/Card/subst/Var)을 MatchEventType으로 변환한다")
    void getMatchEvents_mapsAllTypes() {
        Match match = mock(Match.class);
        stubEmptyStoredEvents(match);
        given(matchEventClient.fetchEvents(MATCH_ID)).willReturn(List.of(
                apiEvent("Goal", 1, null), apiEvent("Card", 2, null),
                apiEvent("subst", 3, null), apiEvent("Var", 4, null)));

        matchEventService.getMatchEvents(MATCH_ID);

        verify(matchEventUpdateService).updateMatchEvents(any(), responsesCaptor.capture());
        assertThat(responsesCaptor.getValue()).extracting(MatchEventResponse::type)
                .containsExactly(MatchEventType.GOAL, MatchEventType.CARD,
                        MatchEventType.SUBSTITUTION, MatchEventType.VAR);
    }

    @Test
    @DisplayName("추가시간(extra)을 그대로 매핑한다")
    void getMatchEvents_mapsExtraMinute() {
        Match match = mock(Match.class);
        stubEmptyStoredEvents(match);
        given(matchEventClient.fetchEvents(MATCH_ID)).willReturn(List.of(apiEvent("Card", 90, 3)));

        matchEventService.getMatchEvents(MATCH_ID);

        verify(matchEventUpdateService).updateMatchEvents(any(), responsesCaptor.capture());
        MatchEventResponse saved = responsesCaptor.getValue().get(0);
        assertThat(saved.elapsedMinute()).isEqualTo(90);
        assertThat(saved.extraMinute()).isEqualTo(3);
    }

    @Test
    @DisplayName("player, assist가 null이어도 변환된다")
    void getMatchEvents_nullPlayerAndAssist() {
        Match match = mock(Match.class);
        stubEmptyStoredEvents(match);
        given(matchEventClient.fetchEvents(MATCH_ID)).willReturn(List.of(
                new MatchEventApiResponse.Event(
                        new MatchEventApiResponse.TimeInfo(34, null),
                        new MatchEventApiResponse.TeamInfo(33L, "Manchester United"),
                        null, null, "Var", "Goal cancelled", null)));

        matchEventService.getMatchEvents(MATCH_ID);

        verify(matchEventUpdateService).updateMatchEvents(any(), responsesCaptor.capture());
        MatchEventResponse saved = responsesCaptor.getValue().get(0);
        assertThat(saved.playerId()).isNull();
        assertThat(saved.playerName()).isNull();
        assertThat(saved.assistPlayerId()).isNull();
        assertThat(saved.assistPlayerName()).isNull();
        assertThat(saved.type()).isEqualTo(MatchEventType.VAR);
    }

    @Test
    @DisplayName("교체 이벤트는 player(아웃)와 assist(인)를 각각 매핑한다")
    void getMatchEvents_mapsSubstitution() {
        Match match = mock(Match.class);
        stubEmptyStoredEvents(match);
        given(matchEventClient.fetchEvents(MATCH_ID)).willReturn(List.of(
                new MatchEventApiResponse.Event(
                        new MatchEventApiResponse.TimeInfo(58, null),
                        new MatchEventApiResponse.TeamInfo(33L, "Manchester United"),
                        new MatchEventApiResponse.PlayerInfo(356041L, "Carlos Baleba"),
                        new MatchEventApiResponse.PlayerInfo(284322L, "Kobbie Mainoo"),
                        "subst", "Substitution 1", null)));

        matchEventService.getMatchEvents(MATCH_ID);

        verify(matchEventUpdateService).updateMatchEvents(any(), responsesCaptor.capture());
        MatchEventResponse saved = responsesCaptor.getValue().get(0);
        assertThat(saved.playerId()).isEqualTo(356041L);
        assertThat(saved.assistPlayerId()).isEqualTo(284322L);
        assertThat(saved.type()).isEqualTo(MatchEventType.SUBSTITUTION);
    }

    private void stubEmptyStoredEvents(Match match) {
        given(matchRepository.findById(MATCH_ID)).willReturn(Optional.of(match));
        given(matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(MATCH_ID))
                .willReturn(List.of());
    }

    private MatchEventApiResponse.Event apiEvent(String type, int elapsed, Integer extra) {
        return new MatchEventApiResponse.Event(
                new MatchEventApiResponse.TimeInfo(elapsed, extra),
                new MatchEventApiResponse.TeamInfo(33L, "Manchester United"),
                new MatchEventApiResponse.PlayerInfo(1165L, "Matheus Cunha"),
                null, type, "detail", null);
    }

    private MatchEvent entity(Match match, MatchEventType type) {
        return MatchEvent.create(match, 34, null, 33L, "Manchester United",
                1165L, "Matheus Cunha", null, null, type, "Normal Goal", null);
    }
}
