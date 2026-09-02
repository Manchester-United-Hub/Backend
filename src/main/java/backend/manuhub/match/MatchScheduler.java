package backend.manuhub.match;

import backend.manuhub.common.util.SeasonProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchScheduler {

    private static final Long PREMIER_LEAGUE_ID = 39L;
    private static final Long MANCHESTER_UNITED_TEAM_ID = 33L;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final MatchRepository matchRepository;
    private final MatchInitializeService matchInitializeService;
    private final SeasonProvider seasonProvider;
    private final Clock clock;

    @Scheduled(cron = "0 0 */6 * * *", zone = "Asia/Seoul")
    public void upsertCurrentSeasonMatches() {
        int currentSeason = seasonProvider.getCurrentSeason();

        log.info("[MatchScheduler] Current season match upsert started. season={}", currentSeason);
        matchInitializeService.saveMatches(PREMIER_LEAGUE_ID, currentSeason, MANCHESTER_UNITED_TEAM_ID);
        log.info("[MatchScheduler] Current season match upsert completed. season={}", currentSeason);
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void updateMatchesDuringMatchWindow() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), KOREA_ZONE);
        LocalDateTime windowStart = now.minusHours(3);
        List<Long> matchIds = matchRepository
                .findAllByDateAfterAndDateLessThanEqualOrderByDateAsc(windowStart, now)
                .stream()
                .map(Match::getMatchId)
                .toList();

        if (matchIds.isEmpty()) {
            return;
        }

        log.info("[MatchScheduler] Live-window match update started. matchIds={}, windowStart={}, now={}",
                matchIds, windowStart, now);
        matchIds.forEach(matchInitializeService::updateMatch);
        log.info("[MatchScheduler] Live-window match update completed. matchIds={}", matchIds);
    }
}
