package backend.manuhub.coach;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByTeamId(Long teamId);
}
