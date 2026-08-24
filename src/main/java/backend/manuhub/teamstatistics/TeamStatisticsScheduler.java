package backend.manuhub.teamstatistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamStatisticsScheduler {
    private final TeamStatisticsInitializeService teamStatisticsInitializeService;

    @Scheduled(cron = "0 0 3 1 6 *", zone = "Asia/Seoul") // 임의로 6/1일
    public void saveLastSeasonStatistics() {
        int season = LocalDate.now().getYear() - 1;

        log.info("[TeamStatisticsScheduler] 시즌 종료 기록 저장 시작 season={}", season);

        teamStatisticsInitializeService.saveTeamStatistics(season);

        log.info("[TeamStatisticsScheduler] 시즌 종료 기록 저장 완료 season={}", season);
    }
}
