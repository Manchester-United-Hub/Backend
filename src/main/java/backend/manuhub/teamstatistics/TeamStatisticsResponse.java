package backend.manuhub.teamstatistics;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamStatisticsResponse {

    private Long teamId;   // 팀 Id
    private String teamName;    // 팀 이름
    private Integer season;    // 시즌

    private Integer win;       // 승
    private Integer draw;      // 무
    private Integer lose;      // 패

    private Integer goalsFor;      // 득점
    private Integer goalsAgainst;  // 실점

    private Integer points;    // 승점
    private Integer rank;      // 순위

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