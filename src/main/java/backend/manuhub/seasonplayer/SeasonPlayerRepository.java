package backend.manuhub.seasonplayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SeasonPlayerRepository extends JpaRepository<SeasonPlayer, SeasonPlayerId> {

    boolean existsBySeason(Integer season);

    @Query("""
            select sp
            from SeasonPlayer sp
            join fetch sp.player p
            where sp.season = :season
            order by sp.number asc, p.name asc
            """)
    List<SeasonPlayer> findAllBySeasonWithPlayer(@Param("season") Integer season);

    List<SeasonPlayer> findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(Collection<Long> playerIds);

    List<SeasonPlayer> findAllByPlayerIdOrderBySeasonAsc(Long playerId);
}
