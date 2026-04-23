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

    // 시즌 (시작 연도)
    @NotNull
    private Integer season;

    // 승 / 무 / 패
    private Integer wins;
    private Integer draws;
    private Integer loses;

    // 득점
    @Column(name = "goals_for")
    private Integer goalsFor;

    // 실점
    @Column(name = "goals_against")
    private Integer goalsAgainst;

    // 승점
    private Integer points;

    // 순위
    private Integer rank;

    public static TeamStatistics create(Integer season, Integer wins, Integer draws, Integer loses, Integer goalsFor, Integer goalsAgainst, Integer points, Integer rank) {
        return TeamStatistics.builder()
                .season(season)
                .wins(wins)
                .draws(draws)
                .loses(loses)
                .goalsFor(goalsFor)
                .goalsAgainst(goalsAgainst)
                .points(points)
                .rank(rank)
                .build();
    }

    @Builder
    private TeamStatistics(Integer season, Integer wins, Integer draws, Integer loses, Integer goalsFor, Integer goalsAgainst, Integer points, Integer rank) {
        this.season = season;
        this.wins = wins;
        this.draws = draws;
        this.loses = loses;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.points = points;
        this.rank = rank;
    }
}