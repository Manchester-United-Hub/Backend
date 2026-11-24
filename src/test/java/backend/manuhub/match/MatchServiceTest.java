package backend.manuhub.match;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.match.dto.MatchListResponse;
import backend.manuhub.match.dto.MatchResponse;
import backend.manuhub.lineup.MatchLineupService;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.matchevent.MatchEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchLineupService matchLineupService;

    @Mock
    private MatchEventService matchEventService;

    @InjectMocks
    private MatchService matchService;

    @Test
    @DisplayName("시즌이 없으면 전체 경기를 날짜 오름차순으로 조회한다")
    void getsAllMatchesWhenSeasonIsNull() {
        Match match = createMatch(100L, LocalDateTime.of(2025, 8, 17, 0, 30), 2, 1);
        when(matchRepository.findAllByOrderByDateAsc()).thenReturn(List.of(match));

        MatchListResponse result = matchService.getMatches(null);

        assertEquals(1, result.pastMatches().size());
        assertMatchResponse(result.pastMatches().getFirst(), match);
        verify(matchRepository).findAllByOrderByDateAsc();
        verify(matchRepository, never())
                .findAllByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
                        LocalDateTime.of(2025, 6, 1, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 0, 0)
                );
    }

    @Test
    @DisplayName("시즌이 있으면 해당 시즌 기간의 경기를 조회한다")
    void getsMatchesBySeason() {
        LocalDateTime startDate = LocalDateTime.of(2025, 6, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 6, 1, 0, 0);
        Match match = createMatch(101L, LocalDateTime.of(2026, 1, 3, 21, 0), null, null);
        when(matchRepository.findAllByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(startDate, endDate))
                .thenReturn(List.of(match));

        MatchListResponse result = matchService.getMatches(2025);

        assertEquals(1, result.pastMatches().size());
        assertMatchResponse(result.pastMatches().getFirst(), match);
        assertNull(result.pastMatches().getFirst().score());
        verify(matchRepository)
                .findAllByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(startDate, endDate);
        verify(matchRepository, never()).findAllByOrderByDateAsc();
    }

    @Test
    @DisplayName("경기 ID로 경기 상세를 조회한다")
    void getsMatchByMatchId() {
        Match match = createMatch(102L, LocalDateTime.of(2025, 9, 20, 23, 0), 3, 2);
        when(matchRepository.findById(102L)).thenReturn(Optional.of(match));
        MatchLineupResponse lineup = new MatchLineupResponse(
                102L, MatchLineupResponse.LineupStatus.NOT_AVAILABLE, null, null);
        when(matchLineupService.getLineup(102L)).thenReturn(lineup);

        MatchResponse result = matchService.getMatch(102L);

        assertMatchResponse(result, match);
        // 상세 응답이 같은 경기의 라인업을 포함하고 종료 경기 보충 경로를 사용하는지 검증한다.
        assertEquals(lineup, result.lineup());
        verify(matchLineupService).backfillLineup(102L);
        verify(matchEventService).getMatchEvents(102L);
        verify(matchRepository).findById(102L);
    }

    @Test
    @DisplayName("존재하지 않는 경기를 조회하면 예외가 발생한다")
    void throwsExceptionWhenMatchDoesNotExist() {
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        ManuHubException exception = assertThrows(
                ManuHubException.class,
                () -> matchService.getMatch(999L)
        );

        assertEquals(ErrorCode.NOT_FOUND_ERROR, exception.getErrorCode());
        verify(matchRepository).findById(999L);
    }

    private Match createMatch(Long matchId, LocalDateTime date, Integer homeScore, Integer awayScore) {
        return Match.create(
                matchId,
                date,
                "FT",
                "Regular Season - 1",
                "Old Trafford",
                "Manchester",
                33L,
                "Manchester United",
                "home-logo",
                true,
                40L,
                "Liverpool",
                "away-logo",
                false,
                homeScore,
                awayScore
        );
    }

    private void assertMatchResponse(MatchResponse response, Match match) {
        assertEquals(match.getMatchId(), response.matchId());
        assertEquals(match.getDate(), response.date());
        assertEquals(match.getStatus(), response.status());
        assertEquals(match.getRound(), response.round().name());
        assertEquals(1, response.round().number());
        assertEquals(match.getVenueName(), response.venue().name());
        assertEquals(match.getVenueCity(), response.venue().city());
        assertEquals(match.getHomeTeamId(), response.homeTeam().teamId());
        assertEquals(match.getHomeTeamName(), response.homeTeam().name());
        assertEquals(match.getAwayTeamId(), response.awayTeam().teamId());
        assertEquals(match.getAwayTeamName(), response.awayTeam().name());

        if (match.getHomeScore() == null && match.getAwayScore() == null) {
            assertNull(response.score());
            return;
        }

        assertEquals(match.getHomeScore(), response.score().home());
        assertEquals(match.getAwayScore(), response.score().away());
    }
}
