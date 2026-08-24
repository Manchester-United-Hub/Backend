package backend.manuhub.seasonplayer;

import backend.manuhub.common.util.SeasonProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeasonPlayerScheduler {

    private final SeasonProvider seasonProvider;
    private final SeasonPlayerInitializeService seasonPlayerInitializeService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncCurrentSeasonPlayers() {
        int currentSeason = seasonProvider.getCurrentSeason();

        log.info("[SeasonPlayerScheduler] Current season player synchronization started. season={}", currentSeason);
        seasonPlayerInitializeService.syncSeasonPlayers(currentSeason);
        log.info("[SeasonPlayerScheduler] Current season player synchronization completed. season={}", currentSeason);
    }
}
