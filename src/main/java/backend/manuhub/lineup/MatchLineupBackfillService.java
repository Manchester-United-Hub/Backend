package backend.manuhub.lineup;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchLineupBackfillService {

    private static final String PENDING = "pending";
    private static final int MAX_ATTEMPTS = 3;
    private static final Set<String> COMPLETED_MATCH_STATUSES = Set.of("FT", "AET", "PEN");

    private final MatchRepository matchRepository;
    private final MatchLineupService matchLineupService;
    private final MatchLineupSyncStatusService matchLineupSyncStatusService;
    private final Clock clock;

    public void syncOldest(int size) {
        // 실패한 작업을 매번 조회하지 않고, 재시도 가능 시각이 지난 pending 작업만 처리한다.
        List<Match> matches = matchRepository.findLineupSyncTargets(
                PENDING,
                MAX_ATTEMPTS,
                LocalDateTime.now(clock),
                COMPLETED_MATCH_STATUSES,
                PageRequest.of(0, size)
        );

        for (Match match : matches) {
            try {
                boolean completed = matchLineupService.backfillLineup(match.getMatchId());
                if (!completed) {
                    matchLineupSyncStatusService.recordFailure(match.getMatchId());
                }
            } catch (RuntimeException e) {
                matchLineupSyncStatusService.recordFailure(match.getMatchId());
                log.error(">>> MatchLineupBackfillService --> Lineup backfill failed. matchId={}",
                        match.getMatchId(), e);
            }
        }
    }
}
