package backend.manuhub.coach;

import backend.manuhub.common.entity.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "photo")
    private String photo;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "birth_place")
    private String birthPlace;

    @Column(name = "appointment_date")
    private String appointmentDate;

    @Column(name = "contract_end_date")
    private String contractEndDate;

    @Column(name = "nationality")
    private String nationality;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "coach_careers", joinColumns = @JoinColumn(name = "coach_id"))
    @OrderColumn(name = "career_order")
    private List<CoachCareer> careers = new ArrayList<>();

    public static Coach create(Long id, Long teamId, String name) {
        return create(id, teamId, name, null, null, null, null, null, null, List.of());
    }

    public static Coach create(Long id, Long teamId, String name, String photo, String birthDate,
                               String birthPlace, String appointmentDate, String contractEndDate,
                               String nationality, List<CoachCareer> careers) {
        return Coach.builder()
                .id(id)
                .teamId(teamId)
                .name(name)
                .photo(photo)
                .birthDate(birthDate)
                .birthPlace(birthPlace)
                .appointmentDate(appointmentDate)
                .contractEndDate(contractEndDate)
                .nationality(nationality)
                .careers(careers)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Coach(Long id, Long teamId, String name, String photo, String birthDate,
                  String birthPlace, String appointmentDate, String contractEndDate,
                  String nationality, List<CoachCareer> careers) {
        this.id = id;
        this.teamId = teamId;
        this.name = name;
        this.photo = photo;
        this.birthDate = birthDate;
        this.birthPlace = birthPlace;
        this.appointmentDate = appointmentDate;
        this.contractEndDate = contractEndDate;
        this.nationality = nationality;
        this.careers = careers == null ? new ArrayList<>() : new ArrayList<>(careers);
    }

    public void updateProfile(Coach coach) {
        this.teamId = coach.teamId;
        this.name = currentOrNew(this.name, coach.name);
        this.birthDate = currentOrNew(this.birthDate, coach.birthDate);
        this.birthPlace = currentOrNew(this.birthPlace, coach.birthPlace);
        this.appointmentDate = currentOrNew(this.appointmentDate, coach.appointmentDate);
        this.contractEndDate = currentOrNew(this.contractEndDate, coach.contractEndDate);
        this.nationality = currentOrNew(this.nationality, coach.nationality);

        if (this.careers.isEmpty() && coach.careers != null && !coach.careers.isEmpty()) {
            this.careers.addAll(coach.careers);
        }
    }

    public void updatePhoto(String photo) {
        this.photo = photo;
    }

    private String currentOrNew(String currentValue, String newValue) {
        return currentValue == null || currentValue.isBlank() ? newValue : currentValue;
    }
}
