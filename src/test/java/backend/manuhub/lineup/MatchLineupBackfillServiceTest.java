package backend.manuhub.lineup;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchLineupBackfillServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 17, 12, 0);
    private static final Set<String> COMPLETED_STATUSES = Set.of("FT", "AET", "PEN");

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchLineupService matchLineupService;
    @Mock
    private MatchLineupSyncStatusService statusService;

    private MatchLineupBackfillService service;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        Clock clock = Clock.fixed(NOW.atZone(zone).toInstant(), zone);
        service = new MatchLineupBackfillService(
                matchRepository,
                matchLineupService,
                statusService,
                clock
        );
    }

    @Test
    @DisplayName("재시도 가능한 pending 종료 경기만 조회해 백필한다")
    void backfillsEligibleMatch() {
        Match match = createMatch(1L);
        when(matchRepository.findLineupSyncTargets(
                "pending", 3, NOW, COMPLETED_STATUSES, PageRequest.of(0, 5)
        )).thenReturn(List.of(match));
        when(matchLineupService.backfillLineup(1L)).thenReturn(true);

        service.syncOldest(5);

        verify(matchLineupService).backfillLineup(1L);
    }

    @Test
    @DisplayName("라인업을 받지 못하면 실패 횟수를 기록한다")
    void recordsFailureWhenLineupIsUnavailable() {
        Match match = createMatch(1L);
        when(matchRepository.findLineupSyncTargets(
                "pending", 3, NOW, COMPLETED_STATUSES, PageRequest.of(0, 5)
        )).thenReturn(List.of(match));
        when(matchLineupService.backfillLineup(1L)).thenReturn(false);

        service.syncOldest(5);

        verify(statusService).recordFailure(1L);
    }

    private Match createMatch(Long matchId) {
        return Match.create(
                matchId,
                NOW.minusDays(1),
                "FT",
                "Regular Season - 12",
                "Old Trafford",
                "Manchester",
                33L,
                "Manchester United",
                "home-logo",
                null,
                40L,
                "Liverpool",
                "away-logo",
                null,
                2,
                1
        );
    }
}
