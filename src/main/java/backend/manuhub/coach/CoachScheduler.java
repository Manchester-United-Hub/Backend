package backend.manuhub.coach;

import backend.manuhub.exception.ApiServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoachScheduler {

    private final CoachService coachService;

    @Retryable(maxAttempts = 2, backoff = @Backoff(delayExpression = "${retry.coach.delay:30000}"), retryFor = {ApiServerException.class})
    @Scheduled(cron = "0 0 9 * * THU", zone = "Asia/Seoul")
    public void updateCoach() {
        List<Long> teamIds = coachService.getAllTeamIds();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = teamIds.stream()
                    .map(teamId -> CompletableFuture.runAsync(
                            () -> coachService.updateCoach(teamId), executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    @Recover
    public void recover(ApiServerException e) {
        log.warn(">>> CoachScheduler --> ApiServerException. code = {}, message = {}. All Retry Failed",
                e.getErrorCode().getCode(), e.getErrorCode().getMessage(), e);
    }

    @Recover
    public void recover(Exception e) {
        log.error(">>> CoachScheduler --> Unexpected Error. All Retry Failed", e);
    }
}
