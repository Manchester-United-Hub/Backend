package backend.manuhub.matchevent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {

    @Modifying
    @Query("DELETE FROM MatchEvent me WHERE me.match.matchId = :matchId")
    void deleteAllByMatchId(@Param("matchId") Long matchId);

    List<MatchEvent> findAllByMatch_MatchIdOrderByIdAsc(Long matchId);

    List<MatchEvent> findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(Long matchId);
}
