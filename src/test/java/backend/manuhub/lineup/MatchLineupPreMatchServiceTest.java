package backend.manuhub.lineup;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchLineupPreMatchServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 20, 12, 0);

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchLineupService matchLineupService;

    private MatchLineupPreMatchService service;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        Clock clock = Clock.fixed(NOW.atZone(zone).toInstant(), zone);
        service = new MatchLineupPreMatchService(matchRepository, matchLineupService, clock);
    }

    @Test
    @DisplayName("킥오프 45분, 30분, 15분 전 구간의 라인업 동기화를 시도한다")
    void syncsLineupsAtScheduledOffsets() {
        Match before45 = match(1L, NOW.plusMinutes(45));
        Match before30 = match(2L, NOW.plusMinutes(30));
        Match before15 = match(3L, NOW.plusMinutes(15));
        when(matchRepository.findPreMatchLineupSyncTargets(
                "NS",
                NOW.plusMinutes(45), NOW.plusMinutes(46),
                NOW.plusMinutes(30), NOW.plusMinutes(31),
                NOW.plusMinutes(15), NOW.plusMinutes(16)
        )).thenReturn(List.of(before15, before30, before45));

        service.syncScheduledLineups();

        verify(matchLineupService).backfillLineup(1L);
        verify(matchLineupService).backfillLineup(2L);
        verify(matchLineupService).backfillLineup(3L);
    }

    @Test
    @DisplayName("한 경기의 외부 API 호출이 실패해도 나머지 경기 동기화를 계속한다")
    void continuesAfterSynchronizationFailure() {
        Match first = match(1L, NOW.plusMinutes(15));
        Match second = match(2L, NOW.plusMinutes(15));
        when(matchRepository.findPreMatchLineupSyncTargets(
                "NS",
                NOW.plusMinutes(45), NOW.plusMinutes(46),
                NOW.plusMinutes(30), NOW.plusMinutes(31),
                NOW.plusMinutes(15), NOW.plusMinutes(16)
        )).thenReturn(List.of(first, second));
        when(matchLineupService.backfillLineup(1L)).thenThrow(new RuntimeException("API failure"));

        service.syncScheduledLineups();

        verify(matchLineupService).backfillLineup(2L);
    }

    private Match match(Long matchId, LocalDateTime date) {
        return Match.create(
                matchId,
                date,
                "NS",
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
                null,
                null
        );
    }
}
