package backend.manuhub.lineup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    List<MatchLineup> findAllByMatchId(Long matchId);
}
