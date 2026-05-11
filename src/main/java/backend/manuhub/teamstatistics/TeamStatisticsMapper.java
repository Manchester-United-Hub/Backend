package backend.manuhub.teamstatistics;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.teamstatistics.TeamStatisticsApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TeamStatisticsMapper {

    public static TeamStatistics toEntity(TeamStatisticsApiResponse response) {
        try {
            TeamStatisticsApiResponse.Standing standing = response.response()
                    .getFirst()
                    .league()
                    .standings()
                    .getFirst()
                    .getFirst();

            TeamStatisticsApiResponse.League league = response.response()
                    .getFirst()
                    .league();

            TeamStatisticsApiResponse.Team team = standing.team();
            TeamStatisticsApiResponse.All all = standing.all();

            return TeamStatistics.create(
                    team.name(),
                    team.id(),
                    league.season(),
                    all.win(),
                    all.draw(),
                    all.lose(),
                    all.goals().goalsFor(),
                    all.goals().against(),
                    standing.points(),
                    standing.rank()
            );
        } catch (NullPointerException e) {
            log.error(">>> TeamStatisticsMapper --> API 응답 파싱 실패 response : {}", response, e);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_TEAM_STATISTICS_INVALID_RESPONSE_ERROR);
        }
    }
}
