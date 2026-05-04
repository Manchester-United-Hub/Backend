package backend.manuhub.external.teamstatistics;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.teamstatistics.TeamStatistics;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TeamStatisticsApiResponse {

    private List<Response> response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private League league;
    }

    @Getter
    @NoArgsConstructor
    public static class League {
        private Integer season;
        private List<List<Standing>> standings;
    }

    @Getter
    @NoArgsConstructor
    public static class Standing {
        private Integer rank;
        private Team team;
        private Integer points;
        private All all;
    }

    @Getter
    @NoArgsConstructor
    public static class Team {
        private Long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class All {
        private Integer played;
        private Integer win;
        private Integer draw;
        private Integer lose;
        private Goals goals;
    }

    @Getter
    @NoArgsConstructor
    public static class Goals {

        @JsonProperty("for")
        private Integer goalsFor;

        private Integer against;
    }

    public TeamStatistics toEntity() {
        try {
            League league = response.getFirst().getLeague();
            Standing standing = league.getStandings().getFirst().getFirst();
            Team team = standing.getTeam();
            All all = standing.getAll();
            Goals goals = all.getGoals();

            return TeamStatistics.create(
                    team.getName(),
                    team.getId(),
                    league.getSeason(),
                    all.getWin(),
                    all.getDraw(),
                    all.getLose(),
                    goals.getGoalsFor(),
                    goals.getAgainst(),
                    standing.getPoints(),
                    standing.getRank()
            );
        } catch (IllegalStateException e) {
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_TEAM_STATISTICS_INVALID_RESPONSE_ERROR);
        }
    }
}