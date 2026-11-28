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

    // 킥오프 45·30·15분 전의 1분 구간에 속한 경기만 매분 조회한다.
    @Query("""
            select m from Match m
            where m.status = :notStartedStatus
              and ((m.date >= :before45Start and m.date < :before45End)
                or (m.date >= :before30Start and m.date < :before30End)
                or (m.date >= :before15Start and m.date < :before15End))
            order by m.date asc
            """)
    List<Match> findPreMatchLineupSyncTargets(
            @Param("notStartedStatus") String notStartedStatus,
            @Param("before45Start") LocalDateTime before45Start,
            @Param("before45End") LocalDateTime before45End,
            @Param("before30Start") LocalDateTime before30Start,
            @Param("before30End") LocalDateTime before30End,
            @Param("before15Start") LocalDateTime before15Start,
            @Param("before15End") LocalDateTime before15End
    );

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

    // 라이브 중계 이벤트 수집 대상: 수집 대상 status이면서 킥오프가 [kickoffFrom, kickoffTo] 구간에 있는 경기
    @Query("""
        select m
        from Match m
        where m.status in :statuses
          and m.date >= :kickoffFrom
          and m.date <= :kickoffTo
        order by m.date asc
        """)
    List<Match> findLiveTargets(
            @Param("statuses") Collection<String> statuses,
            @Param("kickoffFrom") LocalDateTime kickoffFrom,
            @Param("kickoffTo") LocalDateTime kickoffTo
    );
}
