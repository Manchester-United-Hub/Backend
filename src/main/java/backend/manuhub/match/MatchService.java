package backend.manuhub.match;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.match.dto.MatchListResponse;
import backend.manuhub.match.dto.MatchResponse;
import backend.manuhub.lineup.MatchLineupService;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.matchevent.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private static final int SEASON_START_MONTH = 6;
    private static final int SEASON_START_DAY = 1;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final Set<String> LIVE_TARGET_STATUSES = Set.of(
            "NS", "1H", "HT", "2H", "ET", "BT", "P", "SUSP", "INT", "LIVE"
    );
    private static final long LIVE_WINDOW_HOURS = 3;

    private final Clock clock;

    private final MatchRepository matchRepository;
    private final MatchLineupService matchLineupService;
    private final MatchEventService matchEventService;

    @Transactional(readOnly = true)
    public MatchListResponse getMatches(Integer season) {
        List<Match> matches = season == null ? getAllMatches() : getSeasonMatches(season);

        return MatchListResponse.from(matches, LocalDateTime.now(KOREA_ZONE));
    }

    // 저장 서비스가 각자 트랜잭션을 마친 뒤 경기 상세에 필요한 기록을 조합한다.
    public MatchResponse getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        if (match.isFinished()) {
            // 종료 경기는 저장된 라인업과 이벤트를 우선 사용하고 누락분만 보충한다.
            matchLineupService.backfillLineup(matchId);
            matchEventService.getMatchEvents(matchId);
        }
        MatchLineupResponse lineup = matchLineupService.getLineup(matchId);
        return MatchResponse.from(match, lineup);
    }

    @Transactional(readOnly = true)
    public List<Match> findLiveMatches() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), KOREA_ZONE);
        LocalDateTime kickoffFrom = now.minusHours(LIVE_WINDOW_HOURS);
        return matchRepository.findLiveTargets(LIVE_TARGET_STATUSES, kickoffFrom, now);
    }

    private List<Match> getAllMatches() {
        return matchRepository.findAllByOrderByDateAsc();
    }

    private List<Match> getSeasonMatches(Integer season) {
        LocalDateTime startDate = LocalDate.of(season, SEASON_START_MONTH, SEASON_START_DAY).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(season + 1, SEASON_START_MONTH, SEASON_START_DAY).atStartOfDay();

        return matchRepository.findAllByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(startDate, endDate);
    }
}
