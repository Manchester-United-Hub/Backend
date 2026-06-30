package backend.manuhub.player;

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

@Entity
@Table(name = "players", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"player_id", "season"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long playerId;

    @NotNull
    private Integer season;

    @NotNull
    private String name;

    private String birthDate;
    private String nationality;
    private String height;
    private String weight;
    private Integer number;
    private String position;
    private String photo;

    @Builder
    private Player(Long playerId, Integer season, String name, String birthDate, String nationality,
                   String height, String weight, Integer number, String position, String photo) {
        this.playerId = playerId;
        this.season = season;
        this.name = name;
        this.birthDate = birthDate;
        this.nationality = nationality;
        this.height = height;
        this.weight = weight;
        this.number = number;
        this.position = position;
        this.photo = photo;
    }
}
