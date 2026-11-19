package backend.manuhub.player;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Page<Player> findAllByOrderByNameAscPlayerIdAsc(Pageable pageable);

    @Query(value = """
            select p
            from Player p
            where (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
              and (:position is null or exists (
                    select sp.playerId
                    from SeasonPlayer sp
                    where sp.playerId = p.playerId
                      and sp.position = :position
              ))
            order by p.name asc, p.playerId asc
            """, countQuery = """
            select count(p)
            from Player p
            where (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
              and (:position is null or exists (
                    select sp.playerId
                    from SeasonPlayer sp
                    where sp.playerId = p.playerId
                      and sp.position = :position
              ))
            """)
    Page<Player> searchPlayers(
            @Param("position") String position,
            @Param("name") String name,
            Pageable pageable
    );
}
