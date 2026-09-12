package backend.manuhub.seasonplayer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SeasonPlayerRepository extends JpaRepository<SeasonPlayer, SeasonPlayerId> {

    boolean existsBySeason(Integer season);

    @Query("""
        SELECT sp FROM SeasonPlayer sp
        JOIN FETCH sp.player p
        WHERE (:season IS NULL OR sp.season = :season)
        AND (:position IS NULL OR sp.position = :position)
        AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY p.name, p.playerId
        """)
    Page<SeasonPlayer> search(
            @Param("season") Integer season,
            @Param("position") String position,
            @Param("name") String name,
            Pageable pageable
    );

    @Query(value = """
            select sp
            from SeasonPlayer sp
            join fetch sp.player p
            where sp.season = :season
            order by p.name asc, p.playerId asc
            """, countQuery = """
            select count(sp)
            from SeasonPlayer sp
            where sp.season = :season
            """)
    Page<SeasonPlayer> findAllBySeasonWithPlayer(
            @Param("season") Integer season,
            Pageable pageable
    );

    List<SeasonPlayer> findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(Collection<Long> playerIds);

    List<SeasonPlayer> findAllByPlayerIdOrderBySeasonAsc(Long playerId);
}
