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

    // 오래된 NS 데이터의 반복 API 호출만 막고 실제 진행 중인 경기는 시간과 관계없이 계속 갱신한다.
    @Query("""
            select m
            from Match m
            where (m.status = :notStartedStatus
                    and m.date >= :notStartedWindowStart
                    and m.date <= :preMatchCutoff)
               or m.status in :activeStatuses
            order by m.date asc
            """)
    List<Match> findAllToUpdateBeforeAndDuringMatch(
            @Param("notStartedStatus") String notStartedStatus,
            @Param("notStartedWindowStart") LocalDateTime notStartedWindowStart,
            @Param("preMatchCutoff") LocalDateTime preMatchCutoff,
            @Param("activeStatuses") Collection<String> activeStatuses
    );

    // failed 상태는 재조회하지 않고, 재시도 횟수와 대기 시간이 남은 pending 경기만 가져온다.
    @Query("""
            select m
            from Match m
            where m.lineupSyncStatus = :lineupSyncStatus
              and m.lineupSyncAttempts < :maxAttempts
              and (m.lineupSyncNextRetryAt is null or m.lineupSyncNextRetryAt <= :now)
              and m.status in :matchStatuses
            order by m.date asc
            """)
    List<Match> findLineupSyncTargets(
            @Param("lineupSyncStatus") String lineupSyncStatus,
            @Param("maxAttempts") int maxAttempts,
            @Param("now") LocalDateTime now,
            @Param("matchStatuses") Collection<String> matchStatuses,
            Pageable pageable
    );
}
