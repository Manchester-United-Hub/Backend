package backend.manuhub.team;

import backend.manuhub.common.entity.BaseTimeEntity;
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
@Getter
@Table(name = "teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseTimeEntity {

    @Id
    @Column(name = "team_id")
    private Long id;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "stadium")
    private String stadium;

    @Column(name = "founded")
    private Integer founded;

    public static Team create(Long id, String name, String logoUrl, String city, String country, String stadium, Integer founded) {
        return Team.builder()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .city(city)
                .country(country)
                .stadium(stadium)
                .founded(founded)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Team(Long id, String name, String logoUrl, String city, String country, String stadium, Integer founded) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.city = city;
        this.country = country;
        this.stadium = stadium;
        this.founded = founded;
    }
}
