package backend.manuhub.teamstatistics.dto;

import backend.manuhub.teamstatistics.TeamStatistics;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record TeamStatisticsResponse (
        Long teamId,
        String teamName,
        Integer season,
        Integer win,
        Integer draw,
        Integer lose,
        Integer goalsFor,
        Integer goalsAgainst,
        Integer points,
        Integer rank
) {
    public static TeamStatisticsResponse from(TeamStatistics teamStatistics) {
        return TeamStatisticsResponse.builder()
                .teamId(teamStatistics.getTeamId())
                .teamName(teamStatistics.getTeamName())
                .season(teamStatistics.getSeason())
                .win(teamStatistics.getWin())
                .draw(teamStatistics.getDraw())
                .lose(teamStatistics.getLose())
                .goalsFor(teamStatistics.getGoalsFor())
                .goalsAgainst(teamStatistics.getGoalsAgainst())
                .points(teamStatistics.getPoints())
                .rank(teamStatistics.getTeamRank())
                .build();
    }
}