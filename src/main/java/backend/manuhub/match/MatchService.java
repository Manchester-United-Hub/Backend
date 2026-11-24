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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private static final int SEASON_START_MONTH = 6;
    private static final int SEASON_START_DAY = 1;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final MatchRepository matchRepository;
    private final MatchLineupService matchLineupService;
    private final MatchEventService matchEventService;

    @Transactional(readOnly = true)
    public MatchListResponse getMatches(Integer season) {
        List<Match> matches = season == null ? getAllMatches() : getSeasonMatches(season);

        return MatchListResponse.from(matches, LocalDateTime.now(KOREA_ZONE));
    }

    // 외부 API·이미지 보충 중 DB 트랜잭션을 유지하지 않고, 기존 저장 서비스의 짧은 트랜잭션을 사용한다.
    public MatchResponse getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));

        if (match.isFinished()) {
            // 과거 상세는 스케줄러 수집 범위와 관계없이 누락된 라인업·이벤트만 보충해 재사용한다.
            matchLineupService.backfillLineup(matchId);
            matchEventService.getMatchEvents(matchId);
        }

        // 두 데이터의 저장이 끝난 뒤 한 번 조합하여 최초 라인업에 종료 시점의 전체 기록을 붙인다.
        MatchLineupResponse lineup = matchLineupService.getLineup(matchId);
        return MatchResponse.from(match, lineup);
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
