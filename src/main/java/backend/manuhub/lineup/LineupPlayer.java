package backend.manuhub.lineup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lineup_players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineupPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lineup_player_id")
    private Long id;

    @Column(name = "lineup_id", nullable = false)
    private Long lineupId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "number")
    private Integer number;

    @Column(name = "position")
    private String position;

    @Column(name = "grid")
    private String grid;

    @Column(name = "player_type", nullable = false)
    private String playerType;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public static LineupPlayer create(Long lineupId, Long playerId, String playerName, Integer number,
                                      String position, String grid, String playerType, Integer displayOrder) {
        return LineupPlayer.builder()
                .lineupId(lineupId)
                .playerId(playerId)
                .playerName(playerName)
                .number(number)
                .position(position)
                .grid(grid)
                .playerType(playerType)
                .displayOrder(displayOrder)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private LineupPlayer(Long lineupId, Long playerId, String playerName, Integer number,
                         String position, String grid, String playerType, Integer displayOrder) {
        this.lineupId = lineupId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.number = number;
        this.position = position;
        this.grid = grid;
        this.playerType = playerType;
        this.displayOrder = displayOrder;
    }
}
