package backend.manuhub.teamstatistics;

import backend.manuhub.exception.ApiServerException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamStatisticsInitializeService {

    private final TeamStatisticsService teamStatisticsService;

    @Retryable(
            maxAttemptsExpression = "${retry.team-statistics.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.team-statistics.delay:1000}", multiplier = 2), // 최대 1024분
            retryFor = {ApiServerException.class}
    )
    public void initializeTeamStatistics(int season) {
        teamStatisticsService.saveTeamStatistics(season);
    }

    @Recover
    public void recover(ApiServerException e) {
        log.warn(">>> TeamStatisticsInitializeService --> ApiServerException. code = {}, message = {}. All Retry Failed",
                e.getErrorCode().getCode(), e.getErrorCode().getMessage(), e);
    }

    @Recover
    public void recover(Exception e) {
        log.warn(">>> TeamStatisticsInitializeService --> Exception. All Retry Failed", e);
    }
}