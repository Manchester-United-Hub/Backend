package backend.manuhub.lineup;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchLineupPreMatchScheduler {

    private final MatchLineupPreMatchService matchLineupPreMatchService;

    // 킥오프 45·30·15분 전 조회 시점을 놓치지 않도록 매분 확인한다.
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void syncPreMatchLineups() {
        matchLineupPreMatchService.syncScheduledLineups();
    }
}
