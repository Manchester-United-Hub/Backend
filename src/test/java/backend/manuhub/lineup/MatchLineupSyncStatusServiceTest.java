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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchLineupSyncStatusServiceTest {

    private static final Long MATCH_ID = 12345L;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 17, 12, 0);

    @Mock
    private MatchRepository matchRepository;

    private MatchLineupSyncStatusService service;
    private Match match;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(KOREA_ZONE).toInstant(), KOREA_ZONE);
        service = new MatchLineupSyncStatusService(matchRepository, clock);
        match = createMatch();
        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
    }

    @Test
    @DisplayName("라인업 동기화는 첫 실패 10분 후, 두 번째 실패 1시간 후 재시도한다")
    void schedulesRetryWithBackoff() {
        service.recordFailure(MATCH_ID);

        assertEquals("pending", match.getLineupSyncStatus());
        assertEquals(1, match.getLineupSyncAttempts());
        assertEquals(NOW.plusMinutes(10), match.getLineupSyncNextRetryAt());

        service.recordFailure(MATCH_ID);

        assertEquals("pending", match.getLineupSyncStatus());
        assertEquals(2, match.getLineupSyncAttempts());
        assertEquals(NOW.plusHours(1), match.getLineupSyncNextRetryAt());
    }

    @Test
    @DisplayName("라인업 동기화가 세 번 실패하면 failed로 변경하고 재시도를 중단한다")
    void stopsRetryAfterThirdFailure() {
        service.recordFailure(MATCH_ID);
        service.recordFailure(MATCH_ID);
        service.recordFailure(MATCH_ID);

        assertEquals("failed", match.getLineupSyncStatus());
        assertEquals(3, match.getLineupSyncAttempts());
        assertNull(match.getLineupSyncNextRetryAt());
    }

    @Test
    @DisplayName("라인업 동기화가 완료되면 다음 재시도 시각을 제거한다")
    void clearsRetryTimeWhenCompleted() {
        service.recordFailure(MATCH_ID);

        service.markCompleted(MATCH_ID);

        assertEquals("completed", match.getLineupSyncStatus());
        assertNull(match.getLineupSyncNextRetryAt());
    }

    private Match createMatch() {
        return Match.create(
                MATCH_ID,
                LocalDateTime.ofInstant(Instant.parse("2026-11-15T12:00:00Z"), KOREA_ZONE),
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
