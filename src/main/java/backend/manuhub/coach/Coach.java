package backend.manuhub.coach;

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
@Table(name = "coaches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coach extends BaseTimeEntity {

    @Id
    @Column(name = "coach_id")
    private Long id;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "name")
    @NotNull
    private String name;

    public static Coach create(Long id, Long teamId, String name) {
        return Coach.builder()
                .id(id)
                .teamId(teamId)
                .name(name)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Coach(Long id, Long teamId, String name) {
        this.id = id;
        this.teamId = teamId;
        this.name = name;
    }



}
