package backend.manuhub.lineup;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchLineupBackfillService {

    private static final String PENDING = "pending";
    private static final Set<String> COMPLETED_MATCH_STATUSES = Set.of("FT", "AET", "PEN");

    private final MatchRepository matchRepository;
    private final MatchLineupService matchLineupService;
    private final MatchLineupSyncStatusService matchLineupSyncStatusService;

    public void syncOldest(int size) {
        List<Match> matches = matchRepository.findLineupSyncTargets(
                PENDING,
                COMPLETED_MATCH_STATUSES,
                PageRequest.of(0, size)
        );

        for (Match match : matches) {
            try {
                matchLineupService.backfillLineup(match.getMatchId());
            } catch (RuntimeException e) {
                matchLineupSyncStatusService.markFailed(match.getMatchId());
                log.error(">>> MatchLineupBackfillService --> Lineup backfill failed. matchId={}",
                        match.getMatchId(), e);
            }
        }
    }
}
