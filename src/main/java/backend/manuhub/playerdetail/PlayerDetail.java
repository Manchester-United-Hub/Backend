package backend.manuhub.playerdetail;

import backend.manuhub.seasonplayer.SeasonPlayer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_details")
@IdClass(PlayerDetailId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerDetail {

    @Id
    @Column(name = "player_id", nullable = false)
    private Long playerId;                   // API-Football player ID

    @Id
    @Column(name = "season", nullable = false)
    private Integer season;                  // Season year for the statistic

    @Id
    @Column(name = "league_id", nullable = false)
    private Long leagueId;                   // Competition ID (league or cup)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "player_id", referencedColumnName = "player_id",
                    insertable = false, updatable = false),
            @JoinColumn(name = "season", referencedColumnName = "season",
                    insertable = false, updatable = false)
    })
    private SeasonPlayer seasonPlayer;       // Player's season profile

    private String leagueName;               // Competition name (league or cup)

    private Integer appearances;             // Matches played
    private Integer lineups;                 // Matches started
    private Integer minutes;                 // Total minutes played
    private String rating;                   // Average match rating
    private Boolean captain;                 // Whether the player was captain
    private Integer substitutesIn;           // Substituted on count
    private Integer substitutesOut;          // Substituted off count
    private Integer substitutesBench;        // Bench appearances

    private Integer shotsTotal;              // Total shots
    private Integer shotsOn;                 // Shots on target
    private Integer goals;                   // Goals scored
    private Integer assists;                 // Assists
    private Integer dribblesAttempts;        // Dribble attempts
    private Integer dribblesSuccess;         // Successful dribbles
    private Integer dribblesPast;            // Times dribbled past by an opponent
    private Integer penaltiesWon;            // Penalties won
    private Integer penaltiesScored;         // Penalties scored
    private Integer penaltiesMissed;         // Penalties missed

    private Integer passesTotal;             // Total passes attempted
    private Integer passesKey;               // Key passes
    private String passesAccuracy;           // Pass completion rate

    private Integer tacklesTotal;            // Total tackles
    private Integer tacklesBlocks;           // Blocks
    private Integer tacklesInterceptions;    // Interceptions
    private Integer duelsTotal;              // Total duels
    private Integer duelsWon;                // Duels won
    private Integer foulsDrawn;              // Fouls drawn
    private Integer foulsCommitted;          // Fouls committed

    private Integer goalsConceded;           // Goals conceded
    private Integer saves;                   // Goalkeeper saves
    private Integer penaltiesSaved;          // Penalties saved
    private Integer yellowCards;             // Yellow cards
    private Integer yellowRedCards;          // Second-yellow dismissals
    private Integer redCards;                // Direct red cards

    @Builder
    private PlayerDetail(Long playerId, Integer season, SeasonPlayer seasonPlayer, Long leagueId, String leagueName,
                         Integer appearances, Integer lineups, Integer minutes, String rating, Boolean captain,
                         Integer substitutesIn, Integer substitutesOut, Integer substitutesBench,
                         Integer shotsTotal, Integer shotsOn, Integer goals, Integer assists,
                         Integer dribblesAttempts, Integer dribblesSuccess, Integer dribblesPast,
                         Integer penaltiesWon, Integer penaltiesScored, Integer penaltiesMissed,
                         Integer passesTotal, Integer passesKey, String passesAccuracy,
                         Integer tacklesTotal, Integer tacklesBlocks, Integer tacklesInterceptions,
                         Integer duelsTotal, Integer duelsWon, Integer foulsDrawn, Integer foulsCommitted,
                         Integer goalsConceded, Integer saves, Integer penaltiesSaved,
                         Integer yellowCards, Integer yellowRedCards, Integer redCards) {
        this.playerId = playerId;
        this.season = season;
        this.leagueId = leagueId;
        this.seasonPlayer = seasonPlayer;
        this.leagueName = leagueName;
        this.appearances = appearances;
        this.lineups = lineups;
        this.minutes = minutes;
        this.rating = rating;
        this.captain = captain;
        this.substitutesIn = substitutesIn;
        this.substitutesOut = substitutesOut;
        this.substitutesBench = substitutesBench;
        this.shotsTotal = shotsTotal;
        this.shotsOn = shotsOn;
        this.goals = goals;
        this.assists = assists;
        this.dribblesAttempts = dribblesAttempts;
        this.dribblesSuccess = dribblesSuccess;
        this.dribblesPast = dribblesPast;
        this.penaltiesWon = penaltiesWon;
        this.penaltiesScored = penaltiesScored;
        this.penaltiesMissed = penaltiesMissed;
        this.passesTotal = passesTotal;
        this.passesKey = passesKey;
        this.passesAccuracy = passesAccuracy;
        this.tacklesTotal = tacklesTotal;
        this.tacklesBlocks = tacklesBlocks;
        this.tacklesInterceptions = tacklesInterceptions;
        this.duelsTotal = duelsTotal;
        this.duelsWon = duelsWon;
        this.foulsDrawn = foulsDrawn;
        this.foulsCommitted = foulsCommitted;
        this.goalsConceded = goalsConceded;
        this.saves = saves;
        this.penaltiesSaved = penaltiesSaved;
        this.yellowCards = yellowCards;
        this.yellowRedCards = yellowRedCards;
        this.redCards = redCards;
    }
}
