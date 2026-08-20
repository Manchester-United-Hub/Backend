package backend.manuhub.seasonplayer;

import backend.manuhub.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "season_players")
@IdClass(SeasonPlayerId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeasonPlayer {

    @Id
    @NotNull
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Id
    @NotNull
    @Column(name = "season", nullable = false)
    private Integer season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id",
            insertable = false, updatable = false)
    private Player player;

    private Integer number;
    private String position;

    @Builder
    private SeasonPlayer(Long playerId, Integer season, Player player, Integer number, String position) {
        this.playerId = playerId;
        this.season = season;
        this.player = player;
        this.number = number;
        this.position = position;
    }
}
