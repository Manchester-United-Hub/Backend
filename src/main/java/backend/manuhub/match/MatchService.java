package backend.manuhub.match;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.match.dto.MatchListResponse;
import backend.manuhub.match.dto.MatchResponse;
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

    @Transactional(readOnly = true)
    public MatchListResponse getMatches(Integer season) {
        List<Match> matches = season == null ? getAllMatches() : getSeasonMatches(season);

        return MatchListResponse.from(matches, LocalDateTime.now(KOREA_ZONE));
    }

    @Transactional(readOnly = true)
    public MatchResponse getMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .map(MatchResponse::from)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
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
