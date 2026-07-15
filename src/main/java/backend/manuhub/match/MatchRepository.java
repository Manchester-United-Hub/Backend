package backend.manuhub.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByOrderByDateAsc();

    List<Match> findAllByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(LocalDateTime startDate,
                                                                           LocalDateTime endDate);

    Optional<Match> findByMatchId(Long matchId);

    List<Match> findAllByMatchIdIn(Set<Long> matchIds);
}
