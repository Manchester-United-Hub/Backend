package backend.manuhub.lineup;

import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.lineup.dto.MatchLineupResponse.*;
import backend.manuhub.matchevent.MatchEvent;
import backend.manuhub.matchevent.MatchEventRepository;
import backend.manuhub.matchevent.MatchEventType;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LineupEventComposerTest {

    @Mock private MatchEventRepository eventRepository;
    @Mock private PlayerRepository playerRepository;
    @InjectMocks private LineupEventComposer composer;

    @Test
    void skipsQueriesWhenLineupUnavailable() {
        MatchLineupResponse unavailable = new MatchLineupResponse(1378985L, LineupStatus.NOT_AVAILABLE, null, null);

        assertThat(composer.compose(unavailable)).isSameAs(unavailable);
        // 없는 라인업에 대해서는 선수와 이벤트를 조회하지 않는다.
        verifyNoInteractions(eventRepository, playerRepository);
    }

    @Test
    void combinesPhotoGoalAndAssist() {
        when(playerRepository.findAllById(Set.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(Player.create(1L, "Scorer", null, null, null, null, "saved-photo")));
        when(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(1378985L))
                .thenReturn(List.of(event(73, MatchEventType.GOAL, "Normal Goal", 33L, 1L, 2L)));

        MatchLineupResponse result = composer.compose(lineup());

        // 저장된 사진과 골·도움이 최초 라인업의 각 선수에 붙는다.
        assertThat(result.home().startPlayers().getFirst().photo()).isEqualTo("saved-photo");
        assertThat(result.home().startPlayers().getFirst().goals())
                .containsExactly(new GoalResponse(73, null, GoalType.NORMAL));
        assertThat(result.home().substitutes().getFirst().assists())
                .containsExactly(new AssistResponse(73, null, 1L, "Player 1"));
    }

    @Test
    void keepsOwnGoalOnActualPlayerAndSubstitutionPositions() {
        when(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(1378985L))
                .thenReturn(List.of(
                        event(52, MatchEventType.SUBSTITUTION, "Substitution 1", 33L, 1L, 2L),
                        event(58, MatchEventType.GOAL, "Own Goal", 33L, 3L, null)));

        MatchLineupResponse result = composer.compose(lineup());

        // 자책골은 실제 선수 명단에, 교체는 기존 선발·후보 위치에 기록한다.
        assertThat(result.away().startPlayers().getFirst().goals())
                .containsExactly(new GoalResponse(58, null, GoalType.OWN_GOAL));
        assertThat(result.home().startPlayers().getFirst().substitutions().getFirst().direction())
                .isEqualTo(SubstitutionDirection.OUT);
        assertThat(result.home().substitutes().getFirst().substitutions().getFirst().direction())
                .isEqualTo(SubstitutionDirection.IN);
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
