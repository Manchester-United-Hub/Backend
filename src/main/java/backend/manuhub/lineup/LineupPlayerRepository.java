package backend.manuhub.lineup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LineupPlayerRepository extends JpaRepository<LineupPlayer, Long> {

    List<LineupPlayer> findAllByLineupIdInOrderByLineupIdAscDisplayOrderAsc(Collection<Long> lineupIds);

    void deleteAllByLineupIdIn(Collection<Long> lineupIds);
}
