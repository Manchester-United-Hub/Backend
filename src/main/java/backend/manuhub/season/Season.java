package backend.manuhub.season;

import backend.manuhub.common.entity.BaseTimeEntity;
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

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "seasons", uniqueConstraints = {
        @UniqueConstraint(columnNames = "season_year")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Season extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "season_year", nullable = false)
    private Integer year;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public static Season create(Integer year, LocalDate startDate, LocalDate endDate) {
        return Season.builder()
                .year(year)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Season(Integer year, LocalDate startDate, LocalDate endDate) {
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
