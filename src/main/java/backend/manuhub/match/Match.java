package backend.manuhub.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

    @Id
    @NotNull
    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @NotNull
    @Column(name = "match_date")
    private LocalDateTime date;

    // API-Football fixture.status.short 값입니다.
    // TBD: 경기 시간 미정, NS: 경기 시작 전, 1H: 전반전 진행 중, HT: 하프타임,
    // 2H: 후반전 진행 중, ET: 연장전 진행 중, BT: 연장전 휴식,
    // P: 승부차기 진행 중, SUSP: 경기 일시 중단, INT: 경기 중단,
    // FT: 정규시간 종료, AET: 연장전 종료, PEN: 승부차기 종료,
    // PST: 경기 연기, CANC: 경기 취소, ABD: 경기 포기·중단,
    // AWD: 몰수패 등 행정 판정 완료, WO: 상대 팀 불참으로 부전승 처리, LIVE: 경기 진행 중
    @Column(name = "match_status")
    private String status;

    // API-Football league.round 원문입니다. 예: "Regular Season - 1"
    @Column(name = "match_round")
    private String round;

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

    @Column(name = "lineup_sync_status", nullable = false,
            columnDefinition = "varchar(20) default 'pending'")
    private String lineupSyncStatus = "pending";

    public static Match create(Long matchId, LocalDateTime date, String status, String round,
                               String venueName, String venueCity,
                               Long homeTeamId, String homeTeamName, String homeTeamLogo, Boolean homeTeamWinner,
                               Long awayTeamId, String awayTeamName, String awayTeamLogo, Boolean awayTeamWinner,
                               Integer homeScore, Integer awayScore) {
        return Match.builder()
                .matchId(matchId)
                .date(date)
                .status(status)
                .round(round)
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
    private Match(Long matchId, LocalDateTime date, String status, String round,
                  String venueName, String venueCity,
                  Long homeTeamId, String homeTeamName, String homeTeamLogo, Boolean homeTeamWinner,
                  Long awayTeamId, String awayTeamName, String awayTeamLogo, Boolean awayTeamWinner,
                  Integer homeScore, Integer awayScore) {
        this.matchId = matchId;
        this.date = date;
        this.status = status;
        this.round = round;
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

    void updateFrom(Match latestMatch) {
        this.date = latestMatch.date;
        this.status = latestMatch.status;
        this.round = latestMatch.round;
        this.venueName = latestMatch.venueName;
        this.venueCity = latestMatch.venueCity;
        this.homeTeamId = latestMatch.homeTeamId;
        this.homeTeamName = latestMatch.homeTeamName;
        this.homeTeamLogo = latestMatch.homeTeamLogo;
        this.homeTeamWinner = latestMatch.homeTeamWinner;
        this.awayTeamId = latestMatch.awayTeamId;
        this.awayTeamName = latestMatch.awayTeamName;
        this.awayTeamLogo = latestMatch.awayTeamLogo;
        this.awayTeamWinner = latestMatch.awayTeamWinner;
        this.homeScore = latestMatch.homeScore;
        this.awayScore = latestMatch.awayScore;
    }

    public void completeLineupSync() {
        this.lineupSyncStatus = "completed";
    }

    public void failLineupSync() {
        this.lineupSyncStatus = "failed";
    }
}
