package backend.manuhub.teamstatistics;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "team_statistics",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_Name", "season"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 팀인지
    @NotNull
    @Column(name = "team_name")
    private String teamName;

    @NotNull
    @Column(name = "team_id")
    private Long teamId;

    // 시즌 (시작 연도)
    @NotNull
    private Integer season;

    // 승 / 무 / 패
    private Integer win;
    private Integer draw;
    private Integer lose;

    // 득점
    @Column(name = "goals_for")
    private Integer goalsFor;

    // 실점
    @Column(name = "goals_against")
    private Integer goalsAgainst;

    // 승점
    private Integer points;

    // 순위
    @Column(name = "team_rank")
    private Integer teamRank;

    public static TeamStatistics create(String teamName, Long teamId, Integer season, Integer win, Integer draw, Integer lose, Integer goalsFor, Integer goalsAgainst, Integer points, Integer teamRank) {
        return TeamStatistics.builder()
                .teamName(teamName)
                .teamId(teamId)
                .season(season)
                .win(win)
                .draw(draw)
                .lose(lose)
                .goalsFor(goalsFor)
                .goalsAgainst(goalsAgainst)
                .points(points)
                .teamRank(teamRank)
                .build();
    }

    @Builder
    private TeamStatistics(String teamName, Long teamId, Integer season, Integer win, Integer draw, Integer lose, Integer goalsFor, Integer goalsAgainst, Integer points, Integer teamRank) {
        this.teamName = teamName;
        this.teamId = teamId;
        this.season = season;
        this.win = win;
        this.draw = draw;
        this.lose = lose;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.points = points;
        this.teamRank = teamRank;
    }
}