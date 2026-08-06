package backend.manuhub.match.dto;

import backend.manuhub.match.Match;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchListResponseTest {

    @Test
    @DisplayName("현재 시간을 기준으로 시작 전 경기와 시작 후 경기를 정확한 개수로 분리한다")
    void splitsMatchesBeforeAndAfterCurrentTime() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 26, 20, 0);
        Match pastMatch = createMatch(100L, now.minusDays(1));
        Match startedNowMatch = createMatch(101L, now);
        Match upcomingMatch1 = createMatch(102L, now.plusHours(1));
        Match upcomingMatch2 = createMatch(103L, now.plusDays(1));

        MatchListResponse result = MatchListResponse.from(
                List.of(pastMatch, startedNowMatch, upcomingMatch1, upcomingMatch2),
                now
        );

        assertEquals(2, result.pastMatches().size());
        assertEquals(List.of(100L, 101L), result.pastMatches().stream()
                .map(MatchResponse::matchId)
                .toList());
        assertEquals(2, result.upcomingMatches().size());
        assertEquals(List.of(102L, 103L), result.upcomingMatches().stream()
                .map(MatchResponse::matchId)
                .toList());
    }

    private Match createMatch(Long matchId, LocalDateTime date) {
        return Match.create(
                matchId,
                date,
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
}
