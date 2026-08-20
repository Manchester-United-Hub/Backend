package backend.manuhub.season;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    Optional<Season> findByYear(Integer year);

    Optional<Season> findFirstByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate date);
}
