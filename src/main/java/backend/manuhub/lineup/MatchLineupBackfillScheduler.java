package backend.manuhub.lineup;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchLineupBackfillScheduler {

    private static final int BATCH_SIZE = 5;

    private final MatchLineupBackfillService matchLineupBackfillService;

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void backfillLineups() {
        matchLineupBackfillService.syncOldest(BATCH_SIZE);
    }
}
