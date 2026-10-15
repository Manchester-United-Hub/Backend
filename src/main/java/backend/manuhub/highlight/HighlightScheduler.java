package backend.manuhub.highlight;

import backend.manuhub.common.util.SeasonProvider;
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
public class HighlightScheduler {

    private final HighlightService highlightService;
    private final SeasonProvider seasonProvider;

    @Retryable(maxAttempts = 2, backoff = @Backoff(delayExpression = "${retry.highlight.delay:30000}"), retryFor = {ApiServerException.class})
    @Scheduled(cron = "0 0 0/8 * * *", zone = "Asia/Seoul")
    public void updateHighlights() {
        log.info(">>> HighlightScheduler START");
        int currentSeason = seasonProvider.getCurrentSeason();
        log.info(">>> HighlightScheduler season = {}", currentSeason);
        highlightService.syncHighlights(currentSeason);
    }

    @Recover
    public void recover(ApiServerException e) {
        log.warn(">>> HighlightScheduler --> ApiServerException. code = {}, message = {}. All Retry Failed",
                e.getErrorCode().getCode(), e.getErrorCode().getMessage(), e);
    }

    @Recover
    public void recover(Exception e) {
        log.error(">>> HighlightScheduler --> Unexpected Error. All Retry Failed", e);
    }
}
