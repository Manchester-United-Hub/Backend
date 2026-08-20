package backend.manuhub.playerdetail;

import backend.manuhub.seasonplayer.SeasonPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerDetailRepository extends JpaRepository<PlayerDetail, PlayerDetailId> {

    List<PlayerDetail> findAllBySeasonPlayerOrderByLeagueNameAsc(SeasonPlayer seasonPlayer);
}
