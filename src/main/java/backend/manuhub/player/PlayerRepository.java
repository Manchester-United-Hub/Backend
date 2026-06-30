package backend.manuhub.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    boolean existsBySeason(Integer season);

    List<Player> findAllBySeasonOrderByNumberAscNameAsc(Integer season);

    Optional<Player> findByPlayerIdAndSeason(Long playerId, Integer season);
}
