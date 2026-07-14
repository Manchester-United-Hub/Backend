package backend.manuhub.rank;

import backend.manuhub.exception.ApiServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankScheduler {

    private final RankService rankService;

    @Retryable(maxAttempts = 2, backoff = @Backoff(delayExpression = "${retry.rank.delay:30000}"), retryFor = {ApiServerException.class})
    @Scheduled(cron = "0 0 9 * * *")
    public void updateRank() {
        rankService.updateRank();
    }

    @Recover
    public void recover(ApiServerException e) {
        log.warn(">>> RankScheduler --> ApiServerException. code = {}, message = {}. All Retry Failed",
                e.getErrorCode().getCode(), e.getErrorCode().getMessage(), e);
    }

    @Recover
    public void recover(Exception e) {
        log.error(">>> RankScheduler --> Unexpected Error. All Retry Failed", e);
    }
}
