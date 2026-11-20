package backend.manuhub.lineup;

import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MatchLineupPersistenceServiceTest {

    private static final Long MATCH_ID = 12345L;

    @Autowired
    private MatchLineupPersistenceService persistenceService;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MatchLineupRepository matchLineupRepository;
    @Autowired
    private LineupPlayerRepository lineupPlayerRepository;

    @AfterEach
    void clear() {
        lineupPlayerRepository.deleteAll();
        matchLineupRepository.deleteAll();
        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("불완전한 기존 라인업을 제거하고 홈·원정 라인업과 선수를 함께 저장한다")
    void replacesIncompleteLineup() {
        matchRepository.save(createMatch());
        MatchLineup incomplete = matchLineupRepository.save(
                MatchLineup.create(MATCH_ID, "home", 33L, "4-4-2", 100L)
        );
        lineupPlayerRepository.save(
                LineupPlayer.create(incomplete.getId(), 99L, "Old Player", 99, "F", null, "starter", 0)
        );

        PersistedLineupData result = persistenceService.replaceLineups(
                MATCH_ID, homeResponse(), awayResponse()
        );

        assertThat(result.lineups()).hasSize(2);
        assertThat(matchLineupRepository.findAllByMatchId(MATCH_ID))
                .extracting(MatchLineup::getTeamType)
                .containsExactlyInAnyOrder("home", "away");
        assertThat(lineupPlayerRepository.findAll())
                .hasSize(4)
                .extracting(LineupPlayer::getPlayerId)
                .doesNotContain(99L);
        assertThat(matchRepository.findById(MATCH_ID).orElseThrow().getLineupSyncStatus())
                .isEqualTo("completed");
    }

    @Test
    @DisplayName("홈·원정 라인업이 이미 저장되어 있으면 중복으로 저장하지 않는다")
    void reusesCompleteLineup() {
        matchRepository.save(createMatch());
        List<MatchLineup> existing = matchLineupRepository.saveAll(List.of(
                MatchLineup.create(MATCH_ID, "home", 33L, "3-4-2-1", 100L),
                MatchLineup.create(MATCH_ID, "away", 40L, "4-3-3", 200L)
        ));

        PersistedLineupData result = persistenceService.replaceLineups(
                MATCH_ID, homeResponse(), awayResponse()
        );

        assertThat(result.lineups()).extracting(MatchLineup::getId)
                .containsExactlyInAnyOrderElementsOf(existing.stream().map(MatchLineup::getId).toList());
        assertThat(matchLineupRepository.findAllByMatchId(MATCH_ID)).hasSize(2);
    }

    private Match createMatch() {
        return Match.create(
                MATCH_ID,
                LocalDateTime.of(2026, 11, 17, 12, 0),
                "FT",
                "Regular Season - 12",
                "Old Trafford",
                "Manchester",
                33L,
                "Manchester United",
                "home-logo",
                null,
                40L,
                "Liverpool",
                "away-logo",
                null,
                2,
                1
        );
    }

    private LineupApiResponse.Response homeResponse() {
        return response(33L, "Manchester United", "3-4-2-1", 100L, 1L, 2L);
    }

    private LineupApiResponse.Response awayResponse() {
        return response(40L, "Liverpool", "4-3-3", 200L, 3L, 4L);
    }

    private LineupApiResponse.Response response(
            Long teamId,
            String teamName,
            String formation,
            Long coachId,
            Long starterId,
            Long substituteId
    ) {
        return new LineupApiResponse.Response(
                new LineupApiResponse.Team(teamId, teamName, "logo"),
                formation,
                new LineupApiResponse.Coach(coachId, "Coach", "photo"),
                List.of(lineupPlayer(starterId, "Starter")),
                List.of(lineupPlayer(substituteId, "Substitute"))
        );
    }

    private LineupApiResponse.LineupPlayer lineupPlayer(Long playerId, String name) {
        return new LineupApiResponse.LineupPlayer(
                new LineupApiResponse.Player(playerId, name, 10, "M", "4:1")
        );
    }
}
