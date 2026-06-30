package backend.manuhub.playerdetail;

import backend.manuhub.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerDetailRepository extends JpaRepository<PlayerDetail, Long> {

    List<PlayerDetail> findAllByPlayerAndSeasonOrderByLeagueNameAsc(Player player, Integer season);
}
