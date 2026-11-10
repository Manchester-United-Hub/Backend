package backend.manuhub.lineup;

import backend.manuhub.coach.Coach;
import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.external.lineup.LineupClient;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import backend.manuhub.team.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MatchLineupService 테스트")
class MatchLineupServiceTest {

    private static final Long MATCH_ID = 12345L;

    @Autowired
    private MatchLineupService matchLineupService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private LineupPlayerRepository lineupPlayerRepository;

    @MockitoBean
    private LineupClient lineupClient;

    @MockitoBean
    private LineupReferenceSyncService lineupReferenceSyncService;

    @AfterEach
    void clear() {
        lineupPlayerRepository.deleteAll();
        matchLineupRepository.deleteAll();
        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("API 라인업을 저장하고 다음 조회에서는 DB 라인업을 반환한다")
    void savesApiLineupAndReadsSavedLineup() {
        matchRepository.save(createMatch());
        given(lineupClient.getLineups(MATCH_ID)).willReturn(createLineups());
        given(lineupReferenceSyncService.syncApiReferences(anyList())).willReturn(createReferences());
        given(lineupReferenceSyncService.sync(anyList(), anyList())).willReturn(createReferences());

        MatchLineupResponse firstResponse = matchLineupService.getLineup(MATCH_ID);

        assertAvailableLineup(firstResponse);
        assertThat(matchLineupRepository.findAllByMatchId(MATCH_ID))
                .hasSize(2)
                .extracting(MatchLineup::getTeamType)
                .containsExactlyInAnyOrder("home", "away");
        assertThat(lineupPlayerRepository.findAll()).hasSize(4);
        assertThat(matchRepository.findById(MATCH_ID).orElseThrow().getLineupSyncStatus())
                .isEqualTo("completed");

        MatchLineupResponse secondResponse = matchLineupService.getLineup(MATCH_ID);

        assertAvailableLineup(secondResponse);
        then(lineupClient).should(times(1)).getLineups(MATCH_ID);
        then(lineupReferenceSyncService).should(times(2))
                .sync(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("배치 백필은 팀과 코치 참조를 동기화하고 라인업을 저장한다")
    void backfillsLineupWithoutReferenceSync() {
        matchRepository.save(createMatch());
        given(lineupClient.getLineups(MATCH_ID)).willReturn(createLineups());
        given(lineupReferenceSyncService.syncApiReferences(anyList())).willReturn(createReferences());

        matchLineupService.backfillLineup(MATCH_ID);

        assertThat(matchLineupRepository.findAllByMatchId(MATCH_ID)).hasSize(2);
        assertThat(lineupPlayerRepository.findAll()).hasSize(4);
        assertThat(matchRepository.findById(MATCH_ID).orElseThrow().getLineupSyncStatus())
                .isEqualTo("completed");
        then(lineupReferenceSyncService).should().syncApiReferences(anyList());
    }

    @Test
    @DisplayName("배치 백필에서 라인업을 찾지 못하면 failed 상태로 변경한다")
    void marksFailedWhenBackfillLineupIsUnavailable() {
        matchRepository.save(createMatch());
        given(lineupClient.getLineups(MATCH_ID)).willReturn(List.of());
        given(lineupReferenceSyncService.syncApiReferences(anyList())).willReturn(
                new LineupReferenceData(Map.of(), Map.of()));

        matchLineupService.backfillLineup(MATCH_ID);

        assertThat(matchLineupRepository.findAllByMatchId(MATCH_ID)).isEmpty();
        assertThat(lineupPlayerRepository.findAll()).isEmpty();
        assertThat(matchRepository.findById(MATCH_ID).orElseThrow().getLineupSyncStatus())
                .isEqualTo("failed");
        then(lineupReferenceSyncService).should().syncApiReferences(anyList());
    }

    private Match createMatch() {
        return Match.create(
                MATCH_ID,
                LocalDateTime.of(2026, 10, 13, 20, 0),
                "NS",
                "Regular Season - 8",
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
                null,
                null
        );
    }

    private List<LineupApiResponse.Response> createLineups() {
        return List.of(
                new LineupApiResponse.Response(
                        new LineupApiResponse.Team(33L, "Manchester United", "home-logo"),
                        "3-4-2-1",
                        new LineupApiResponse.Coach(100L, "Home Coach", "home-coach-photo"),
                        List.of(lineupPlayer(1L, "Home Starter", 10, "F", "4:1")),
                        List.of(lineupPlayer(2L, "Home Substitute", 20, "M", null))
                ),
                new LineupApiResponse.Response(
                        new LineupApiResponse.Team(40L, "Liverpool", "away-logo"),
                        "4-3-3",
                        new LineupApiResponse.Coach(200L, "Away Coach", "away-coach-photo"),
                        List.of(lineupPlayer(3L, "Away Starter", 9, "F", "4:2")),
                        List.of(lineupPlayer(4L, "Away Substitute", 21, "D", null))
                )
        );
    }

    private LineupReferenceData createReferences() {
        return new LineupReferenceData(
                Map.of(
                        33L, Team.create(33L, "Manchester United", "home-r2-logo", null, null, null, null),
                        40L, Team.create(40L, "Liverpool", "away-r2-logo", null, null, null, null)
                ),
                Map.of(
                        100L, Coach.create(100L, 33L, "Home Coach"),
                        200L, Coach.create(200L, 40L, "Away Coach")
                )
        );
    }

    private LineupApiResponse.LineupPlayer lineupPlayer(
            Long playerId,
            String name,
            Integer number,
            String position,
            String grid
    ) {
        return new LineupApiResponse.LineupPlayer(
                new LineupApiResponse.Player(playerId, name, number, position, grid)
        );
    }

    private void assertAvailableLineup(MatchLineupResponse response) {
        assertThat(response.matchId()).isEqualTo(MATCH_ID);
        assertThat(response.lineupStatus()).isEqualTo(MatchLineupResponse.LineupStatus.AVAILABLE);
        assertThat(response.home().teamId()).isEqualTo(33L);
        assertThat(response.home().formation()).isEqualTo("3-4-2-1");
        assertThat(response.home().startPlayers()).singleElement().satisfies(player -> {
            assertThat(player.playerId()).isEqualTo(1L);
            assertThat(player.name()).isEqualTo("Home Starter");
        });
        assertThat(response.home().substitutes()).singleElement()
                .extracting(MatchLineupResponse.PlayerResponse::playerId)
                .isEqualTo(2L);
        assertThat(response.away().teamId()).isEqualTo(40L);
        assertThat(response.away().formation()).isEqualTo("4-3-3");
        assertThat(response.away().startPlayers()).singleElement()
                .extracting(MatchLineupResponse.PlayerResponse::playerId)
                .isEqualTo(3L);
        assertThat(response.away().substitutes()).singleElement()
                .extracting(MatchLineupResponse.PlayerResponse::playerId)
                .isEqualTo(4L);
    }
}
