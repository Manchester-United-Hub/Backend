package backend.manuhub.teamstatistics;

import backend.manuhub.exception.ApiServerException;
import backend.manuhub.external.teamstatistics.TeamStatisticsClient;
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

    private final TeamStatisticsRepository teamStatisticsRepository;
    private final TeamStatisticsClient teamStatisticsClient;

    @Retryable(
            maxAttemptsExpression = "${retry.team-statistics.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.team-statistics.delay:1000}", multiplier = 2),
            retryFor = {ApiServerException.class}
    )
    public void saveTeamStatistics(Integer season) {
        log.info("[TeamStatistics] {}년도 기록 저장 시작", season.toString());

        if(teamStatisticsRepository.existsBySeason(season)) {
            log.warn(">>> TeamStatistics --> 이미 존재하는 시즌 기록입니다. season = {}",
                    season);
            return;
        }

        teamStatisticsClient.getTeamStatistics(season)
                .map(TeamStatisticsMapper::toEntity)
                .ifPresent(
                        entity -> {
                            teamStatisticsRepository.save(entity);
                            log.info("[TeamStatistics] {}년도 기록 저장 완료", season);
                        }
                );
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
