package backend.manuhub.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @NotNull
    @Column(nullable = false)
    private String name;

    private String birthDate;
    private String nationality;
    private String height;
    private String weight;
    private String photo;

    public static Player create(Long playerId, String name, String birthDate, String nationality,
                                String height, String weight, String photo) {
        return Player.builder()
                .playerId(playerId)
                .name(name)
                .birthDate(birthDate)
                .nationality(nationality)
                .height(height)
                .weight(weight)
                .photo(photo)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Player(Long playerId, String name, String birthDate, String nationality,
                   String height, String weight, String photo) {
        this.playerId = playerId;
        this.name = name;
        this.birthDate = birthDate;
        this.nationality = nationality;
        this.height = height;
        this.weight = weight;
        this.photo = photo;
    }

    public void updateProfile(Player player) {
        this.name = player.name;
        this.birthDate = player.birthDate;
        this.nationality = player.nationality;
        this.height = player.height;
        this.weight = player.weight;
    }

    public void updatePhoto(String photo) {
        this.photo = photo;
    }
}
