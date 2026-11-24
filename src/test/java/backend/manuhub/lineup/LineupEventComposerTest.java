package backend.manuhub.lineup;

import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.lineup.dto.MatchLineupResponse.*;
import backend.manuhub.matchevent.MatchEvent;
import backend.manuhub.matchevent.MatchEventRepository;
import backend.manuhub.matchevent.MatchEventType;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineupEventComposerTest {

    @Mock private MatchEventRepository eventRepository;
    @Mock private PlayerRepository playerRepository;
    @InjectMocks private LineupEventComposer composer;

    @Test
    @DisplayName("라인업이 없으면 선수와 이벤트를 추가 조회하지 않는다")
    void unavailableLineupSkipsQueries() {
        MatchLineupResponse unavailable = new MatchLineupResponse(
                1378985L, LineupStatus.NOT_AVAILABLE, null, null);

        assertThat(composer.compose(unavailable)).isSameAs(unavailable);
        verifyNoInteractions(eventRepository, playerRepository);
    }

    @Test
    @DisplayName("사진은 일괄 조회하고 득점과 도움을 각 선수에게 배정한다")
    void composesGoalsAssistsAndPhotosInBatches() {
        when(playerRepository.findAllById(Set.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(Player.create(1L, "Scorer", null, null, null, null, "saved-photo")));
        when(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(1378985L))
                .thenReturn(List.of(event(73, MatchEventType.GOAL, "Normal Goal", 33L, 1L, 2L)));

        MatchLineupResponse result = composer.compose(lineup());

        assertThat(result.home().startPlayers().getFirst().photo()).isEqualTo("saved-photo");
        assertThat(result.home().startPlayers().getFirst().goals())
                .containsExactly(new GoalResponse(73, null, GoalType.NORMAL));
        assertThat(result.home().substitutes().getFirst().photo()).isNull();
        assertThat(result.home().substitutes().getFirst().assists())
                .containsExactly(new AssistResponse(73, null, 1L, "Player 1"));
        verify(playerRepository).findAllById(Set.of(1L, 2L, 3L, 4L));
        verifyNoMoreInteractions(playerRepository);
    }

    @Test
    @DisplayName("자책골은 득점 수혜 팀이 아니라 실제 선수의 원래 명단에 표시한다")
    void assignsOwnGoalToOpposingPlayerWithoutAssist() {
        when(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(1378985L))
                .thenReturn(List.of(event(58, MatchEventType.GOAL, "Own Goal", 33L, 3L, 2L)));

        MatchLineupResponse result = composer.compose(lineup());

        assertThat(result.away().startPlayers().getFirst().goals())
                .containsExactly(new GoalResponse(58, null, GoalType.OWN_GOAL));
        assertThat(result.home().startPlayers().getFirst().goals()).isEmpty();
        assertThat(result.home().substitutes().getFirst().assists()).isEmpty();
    }

    @Test
    @DisplayName("교체 후에도 원래 선발과 후보 명단을 유지하고 IN과 OUT을 각각 기록한다")
    void preservesInitialLineupAndMultipleSubstitutions() {
        when(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(1378985L))
                .thenReturn(List.of(
                        event(52, MatchEventType.SUBSTITUTION, "Substitution 1", 33L, 1L, 2L),
                        event(87, MatchEventType.SUBSTITUTION, "Substitution 2", 33L, 2L, 1L)));

        MatchLineupResponse result = composer.compose(lineup());

        assertThat(result.home().startPlayers()).extracting(PlayerResponse::playerId).containsExactly(1L);
        assertThat(result.home().substitutes()).extracting(PlayerResponse::playerId).containsExactly(2L);
        assertThat(result.home().startPlayers().getFirst().grid()).isEqualTo("1:1");
        assertThat(result.home().substitutes().getFirst().substitutions())
                .extracting(SubstitutionResponse::direction)
                .containsExactly(SubstitutionDirection.IN, SubstitutionDirection.OUT);
        assertThat(result.home().startPlayers().getFirst().substitutions().getFirst().counterpartPlayerId())
                .isEqualTo(2L);
        assertThat(result.home().substitutes().getFirst().assists()).isEmpty();
    }

    @Test
    @DisplayName("취소된 득점과 VAR를 골로 세지 않고 카드 종류를 구분한다")
    void ignoresCancelledGoalsAndMapsCards() {
        when(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(1378985L))
                .thenReturn(List.of(
                        event(35, MatchEventType.VAR, "Goal cancelled", 33L, 1L, null),
                        event(40, MatchEventType.GOAL, "Missed Penalty", 33L, 1L, 2L),
                        event(39, MatchEventType.CARD, "Yellow Card", 33L, 1L, null),
                        event(86, MatchEventType.CARD, "Second Yellow Card", 33L, 1L, null)));

        MatchLineupResponse result = composer.compose(lineup());

        assertThat(result.home().startPlayers().getFirst().goals()).isEmpty();
        assertThat(result.home().substitutes().getFirst().assists()).isEmpty();
        assertThat(result.home().startPlayers().getFirst().cards()).extracting(CardResponse::type)
                .containsExactly(CardType.YELLOW, CardType.SECOND_YELLOW);
    }

    @Test
    @DisplayName("이벤트 삭제나 정정 후 재조합하면 이전 기록이 누적되지 않는다")
    void recomposesFromLatestSnapshotWithoutAccumulation() {
        when(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(1378985L))
                .thenReturn(List.of(event(73, MatchEventType.GOAL, "Normal Goal", 33L, 1L, 2L)))
                .thenReturn(List.of());

        assertThat(composer.compose(lineup()).home().startPlayers().getFirst().goals()).hasSize(1);
        MatchLineupResponse corrected = composer.compose(lineup());
        assertThat(corrected.home().startPlayers().getFirst().goals()).isEmpty();
        assertThat(corrected.home().substitutes().getFirst().assists()).isEmpty();
    }

    private MatchLineupResponse lineup() {
        return new MatchLineupResponse(1378985L, LineupStatus.AVAILABLE,
                new TeamLineupResponse(33L, "Manchester United", null, "4-4-2", null,
                        List.of(player(1L)), List.of(player(2L))),
                new TeamLineupResponse(36L, "Fulham", null, "4-4-2", null,
                        List.of(player(3L)), List.of(player(4L))));
    }

    private PlayerResponse player(Long id) {
        return new PlayerResponse(id, "Player " + id, 10, "M", "1:1");
    }

    private MatchEvent event(int minute, MatchEventType type, String detail,
                             Long teamId, Long playerId, Long assistId) {
        return MatchEvent.create(null, minute, null, teamId, "Team", playerId,
                "Player " + playerId, assistId, assistId == null ? null : "Player " + assistId,
                type, detail, null);
    }
}
