package backend.manuhub.lineup;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchLineupPreMatchService {

    private static final String NOT_STARTED_STATUS = "NS";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final MatchRepository matchRepository;
    private final MatchLineupService matchLineupService;
    private final Clock clock;

    public void syncScheduledLineups() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), KOREA_ZONE)
                .truncatedTo(ChronoUnit.MINUTES);
        List<Match> matches = matchRepository.findPreMatchLineupSyncTargets(
                NOT_STARTED_STATUS,
                now.plusMinutes(45), now.plusMinutes(46),
                now.plusMinutes(30), now.plusMinutes(31),
                now.plusMinutes(15), now.plusMinutes(16)
        );

        for (Match match : matches) {
            try {
                // 이미 양 팀 명단이 저장된 경기는 외부 API를 호출하지 않는다.
                matchLineupService.backfillLineup(match.getMatchId());
            } catch (RuntimeException e) {
                // 경기 전 시도 실패는 다음 예약 시점에 다시 시도할 수 있다.
                log.warn("Pre-match lineup synchronization failed. matchId={}", match.getMatchId(), e);
            }
        }
    }
}
