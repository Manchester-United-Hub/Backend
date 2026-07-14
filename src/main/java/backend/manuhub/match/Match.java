package backend.manuhub.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = "match_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "match_id")
    private Long matchId;

    @NotNull
    @Column(name = "match_date")
    private LocalDateTime date;

    @Column(name = "venue_name")
    private String venueName;

    @Column(name = "venue_city")
    private String venueCity;

    @NotNull
    @Column(name = "home_team_id")
    private Long homeTeamId;

    @NotNull
    @Column(name = "home_team_name")
    private String homeTeamName;

    @Column(name = "home_team_logo")
    private String homeTeamLogo;

    @Column(name = "home_team_winner")
    private Boolean homeTeamWinner;

    @NotNull
    @Column(name = "away_team_id")
    private Long awayTeamId;

    @NotNull
    @Column(name = "away_team_name")
    private String awayTeamName;

    @Column(name = "away_team_logo")
    private String awayTeamLogo;

    @Column(name = "away_team_winner")
    private Boolean awayTeamWinner;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    public static Match create(Long matchId, LocalDateTime date, String venueName, String venueCity,
                               Long homeTeamId, String homeTeamName, String homeTeamLogo, Boolean homeTeamWinner,
                               Long awayTeamId, String awayTeamName, String awayTeamLogo, Boolean awayTeamWinner,
                               Integer homeScore, Integer awayScore) {
        return Match.builder()
                .matchId(matchId)
                .date(date)
                .venueName(venueName)
                .venueCity(venueCity)
                .homeTeamId(homeTeamId)
                .homeTeamName(homeTeamName)
                .homeTeamLogo(homeTeamLogo)
                .homeTeamWinner(homeTeamWinner)
                .awayTeamId(awayTeamId)
                .awayTeamName(awayTeamName)
                .awayTeamLogo(awayTeamLogo)
                .awayTeamWinner(awayTeamWinner)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Match(Long matchId, LocalDateTime date, String venueName, String venueCity,
                  Long homeTeamId, String homeTeamName, String homeTeamLogo, Boolean homeTeamWinner,
                  Long awayTeamId, String awayTeamName, String awayTeamLogo, Boolean awayTeamWinner,
                  Integer homeScore, Integer awayScore) {
        this.matchId = matchId;
        this.date = date;
        this.venueName = venueName;
        this.venueCity = venueCity;
        this.homeTeamId = homeTeamId;
        this.homeTeamName = homeTeamName;
        this.homeTeamLogo = homeTeamLogo;
        this.homeTeamWinner = homeTeamWinner;
        this.awayTeamId = awayTeamId;
        this.awayTeamName = awayTeamName;
        this.awayTeamLogo = awayTeamLogo;
        this.awayTeamWinner = awayTeamWinner;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }
}
