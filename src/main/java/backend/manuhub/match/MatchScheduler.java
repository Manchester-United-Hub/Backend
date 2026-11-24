package backend.manuhub.match;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.matchevent.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchScheduler {

    private static final Long PREMIER_LEAGUE_ID = 39L;
    private static final Long MANCHESTER_UNITED_TEAM_ID = 33L;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final String NOT_STARTED_STATUS = "NS";
    private static final Set<String> FINISHED_STATUSES = Set.of("FT", "AET", "PEN");
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "1H", "HT", "2H", "ET", "BT", "P", "SUSP", "INT", "LIVE"
    );

    private final MatchRepository matchRepository;
    private final MatchInitializeService matchInitializeService;
    private final SeasonProvider seasonProvider;
    private final Clock clock;
    private final MatchEventService matchEventService;

    @Scheduled(cron = "0 0 */6 * * *", zone = "Asia/Seoul")
    public void upsertCurrentSeasonMatches() {
        int currentSeason = seasonProvider.getCurrentSeason();

        log.info("[MatchScheduler] Current season match upsert started. season={}", currentSeason);
        matchInitializeService.saveMatches(PREMIER_LEAGUE_ID, currentSeason, MANCHESTER_UNITED_TEAM_ID);
        log.info("[MatchScheduler] Current season match upsert completed. season={}", currentSeason);
    }

    // 점수·상태 갱신과 이벤트 수집을 같은 1분 주기로 묶어 별도 스케줄러의 중복 수집을 피한다.
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void updateMatchesDuringMatchWindow() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), KOREA_ZONE);
        // NS 상태가 비정상적으로 남아도 하루가 지난 경기는 반복 호출하지 않는다.
        LocalDateTime notStartedWindowStart = now.minusDays(1);
        LocalDateTime preMatchCutoff = now.plusMinutes(10);
        List<Long> matchIds = matchRepository
                .findAllToUpdateBeforeAndDuringMatch(
                        NOT_STARTED_STATUS,
                        notStartedWindowStart,
                        preMatchCutoff,
                        ACTIVE_STATUSES
                )
                .stream()
                .map(Match::getMatchId)
                .toList();

        Set<Long> eventSyncTargets = new LinkedHashSet<>(matchIds);
        for (Long matchId : matchIds) {
            try {
                matchInitializeService.updateMatch(matchId);
            } catch (Exception e) {
                // 한 경기의 외부 API 실패가 나머지 경기의 이벤트 수집까지 중단시키지 않게 한다.
                log.warn("[MatchScheduler] Match refresh failed. matchId={}", matchId, e);
            }
        }

        // 종료 상태로 변경된 뒤 수집이 실패해도, 킥오프 후 1일 이내에는 다음 주기에 다시 시도한다.
        matchRepository.findFinishedEventSyncTargets(FINISHED_STATUSES, now.minusDays(1))
                .stream().map(Match::getMatchId).forEach(eventSyncTargets::add);
        for (Long matchId : eventSyncTargets) {
            try {
                matchEventService.syncMatchEvents(matchId);
            } catch (Exception e) {
                log.warn("[MatchScheduler] Event sync failed. matchId={}", matchId, e);
            }
        }
    }
}
