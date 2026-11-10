package backend.manuhub.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByOrderByDateAsc();

    List<Match> findAllByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(LocalDateTime startDate,
                                                                           LocalDateTime endDate);

    List<Match> findAllByMatchIdIn(Set<Long> matchIds);

    @Query("""
            select m
            from Match m
            where (m.status = :notStartedStatus and m.date <= :preMatchCutoff)
               or m.status in :activeStatuses
            order by m.date asc
            """)
    List<Match> findAllToUpdateBeforeAndDuringMatch(
            @Param("notStartedStatus") String notStartedStatus,
            @Param("preMatchCutoff") LocalDateTime preMatchCutoff,
            @Param("activeStatuses") Collection<String> activeStatuses
    );

    @Query("""
            select m
            from Match m
            where m.lineupSyncStatus = :lineupSyncStatus
              and m.status in :matchStatuses
            order by m.date asc
            """)
    List<Match> findLineupSyncTargets(
            @Param("lineupSyncStatus") String lineupSyncStatus,
            @Param("matchStatuses") Collection<String> matchStatuses,
            Pageable pageable
    );
}
