package backend.manuhub.match;

import backend.manuhub.external.match.MatchApiResponse;
import backend.manuhub.external.match.MatchClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
//@ActiveProfiles("test")
class MatchInitializeServiceTest {

    private static final Long LEAGUE = 39L;
    private static final Integer SEASON = 2025;
    private static final Long TEAM_ID = 33L;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchClient matchClient;

    @InjectMocks
    private MatchInitializeService matchInitializeService;

    @Test
    @DisplayName("API에서 조회한 신규 경기를 저장한다")
    void savesNewMatches() {
        MatchApiResponse.Response response = createResponse(100L, "2025-08-17T15:30:00Z");
        when(matchClient.getMatches(LEAGUE, SEASON, TEAM_ID)).thenReturn(List.of(response));
        when(matchRepository.findAllByMatchIdIn(Set.of(100L))).thenReturn(List.of());

        matchInitializeService.saveMatches(LEAGUE, SEASON, TEAM_ID);

        verify(matchClient).getMatches(LEAGUE, SEASON, TEAM_ID);
        verify(matchRepository).saveAll(argThat(matches -> {
            var iterator = matches.iterator();
            if (!iterator.hasNext()) {
                return false;
            }

            Match savedMatch = iterator.next();
            return !iterator.hasNext()
                    && savedMatch.getMatchId().equals(100L)
                    && savedMatch.getDate().equals(OffsetDateTime.parse("2025-08-17T15:30:00Z")
                    .atZoneSameInstant(java.time.ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime())
                    && savedMatch.getHomeTeamName().equals("Manchester United")
                    && savedMatch.getAwayTeamName().equals("Liverpool")
                    && savedMatch.getHomeScore().equals(2)
                    && savedMatch.getAwayScore().equals(1);
        }));
    }

    @Test
    @DisplayName("이미 저장된 경기를 제외하고 신규 경기만 저장한다")
    void savesOnlyNewMatches() {
        MatchApiResponse.Response existingResponse = createResponse(100L, "2025-08-17T15:30:00Z");
        MatchApiResponse.Response newResponse = createResponse(101L, "2025-08-24T15:30:00Z");
        Match existingMatch = Match.create(
                100L,
                OffsetDateTime.parse("2025-08-17T15:30:00Z").toLocalDateTime(),
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
                2,
                1
        );
        when(matchClient.getMatches(LEAGUE, SEASON, TEAM_ID))
                .thenReturn(List.of(existingResponse, newResponse));
        when(matchRepository.findAllByMatchIdIn(Set.of(100L, 101L)))
                .thenReturn(List.of(existingMatch));

        matchInitializeService.saveMatches(LEAGUE, SEASON, TEAM_ID);

        verify(matchRepository).saveAll(argThat(matches -> {
            var iterator = matches.iterator();
            return iterator.hasNext()
                    && iterator.next().getMatchId().equals(101L)
                    && !iterator.hasNext();
        }));
    }

    @Test
    @DisplayName("모든 경기가 이미 저장되어 있으면 저장을 생략한다")
    void skipsSavingWhenAllMatchesAlreadyExist() {
        MatchApiResponse.Response response = createResponse(100L, "2025-08-17T15:30:00Z");
        Match existingMatch = Match.create(
                100L,
                OffsetDateTime.parse("2025-08-17T15:30:00Z").toLocalDateTime(),
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
                2,
                1
        );
        when(matchClient.getMatches(LEAGUE, SEASON, TEAM_ID)).thenReturn(List.of(response));
        when(matchRepository.findAllByMatchIdIn(Set.of(100L))).thenReturn(List.of(existingMatch));

        matchInitializeService.saveMatches(LEAGUE, SEASON, TEAM_ID);

        verify(matchRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    private MatchApiResponse.Response createResponse(Long matchId, String date) {
        return new MatchApiResponse.Response(
                new MatchApiResponse.Fixture(
                        matchId,
                        OffsetDateTime.parse(date),
                        new MatchApiResponse.Venue(1L, "Old Trafford", "Manchester")
                ),
                new MatchApiResponse.League(LEAGUE, "Premier League", "England", SEASON, "Regular Season"),
                new MatchApiResponse.Teams(
                        new MatchApiResponse.Team(33L, "Manchester United", "home-logo", true),
                        new MatchApiResponse.Team(40L, "Liverpool", "away-logo", false)
                ),
                new MatchApiResponse.Goals(2, 1)
        );
    }
}
