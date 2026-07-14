package backend.manuhub.match;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.match.MatchApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.List;

@Slf4j
public class MatchMapper {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private MatchMapper() {
    }

    public static List<Match> toEntities(List<MatchApiResponse.Response> responses) {
        try {
            return responses.stream()
                    .map(MatchMapper::toEntity)
                    .toList();
        } catch (NullPointerException e) {
            log.error(">>> MatchMapper --> API-Football fixtures response is invalid.", e);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_MATCH_INVALID_RESPONSE_ERROR);
        }
    }

    private static Match toEntity(MatchApiResponse.Response response) {
        MatchApiResponse.Fixture fixture = response.fixture();
        MatchApiResponse.Venue venue = fixture.venue();
        MatchApiResponse.Team homeTeam = response.teams().home();
        MatchApiResponse.Team awayTeam = response.teams().away();
        MatchApiResponse.Goals goals = response.goals();

        return Match.create(
                fixture.id(),
                fixture.date().atZoneSameInstant(KOREA_ZONE).toLocalDateTime(),
                venue.name(),
                venue.city(),
                homeTeam.id(),
                homeTeam.name(),
                homeTeam.logo(),
                homeTeam.winner(),
                awayTeam.id(),
                awayTeam.name(),
                awayTeam.logo(),
                awayTeam.winner(),
                goals.home(),
                goals.away()
        );
    }
}
