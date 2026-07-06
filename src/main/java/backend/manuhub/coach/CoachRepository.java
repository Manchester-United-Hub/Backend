package backend.manuhub.coach;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByTeamId(Long teamId);
    void deleteByTeamId(Long teamId);

    @Query("SELECT c.teamId FROM Coach c")
    List<Long> findAllTeamIds();
}
