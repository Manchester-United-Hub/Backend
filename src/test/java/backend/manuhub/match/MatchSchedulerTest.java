package backend.manuhub.match;

import backend.manuhub.common.util.SeasonProvider;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchSchedulerTest {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 26, 21, 0);
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "1H", "HT", "2H", "ET", "BT", "P", "SUSP", "INT", "LIVE"
    );

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchInitializeService matchInitializeService;

    @Mock
    private SeasonProvider seasonProvider;

    private MatchScheduler matchScheduler;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(KOREA_ZONE).toInstant(), KOREA_ZONE);
        matchScheduler = new MatchScheduler(matchRepository, matchInitializeService, seasonProvider, clock);
    }

    @Test
    @DisplayName("6시간마다 현재 시즌 전체 경기 일정을 upsert한다")
    void upsertsCurrentSeasonMatches() {
        when(seasonProvider.getCurrentSeason()).thenReturn(2026);

        matchScheduler.upsertCurrentSeasonMatches();

        verify(matchInitializeService).saveMatches(39L, 2026, 33L);
    }

    @Test
    @DisplayName("경기 시작 10분 전이거나 진행 중인 경기를 갱신한다")
    void updatesMatchesWhenMatchExistsInUpdateWindow() {
        Match notStartedMatch = match(100L, NOW.plusMinutes(10), "NS");
        Match liveMatch = match(101L, NOW.minusHours(1), "2H");
        when(matchRepository.findAllToUpdateBeforeAndDuringMatch("NS", NOW.plusMinutes(10), ACTIVE_STATUSES))
                .thenReturn(List.of(notStartedMatch, liveMatch));

        matchScheduler.updateMatchesDuringMatchWindow();

        verify(matchInitializeService).updateMatch(100L);
        verify(matchInitializeService).updateMatch(101L);
        verify(matchInitializeService, never()).saveMatches(39L, 2026, 33L);
    }

    @Test
    @DisplayName("갱신 대상 상태의 경기가 없으면 외부 API를 호출하지 않는다")
    void skipsUpdateWhenNoMatchExistsInUpdateWindow() {
        when(matchRepository.findAllToUpdateBeforeAndDuringMatch("NS", NOW.plusMinutes(10), ACTIVE_STATUSES))
                .thenReturn(List.of());

        matchScheduler.updateMatchesDuringMatchWindow();

        verify(matchInitializeService, never()).updateMatch(anyLong());
        verify(seasonProvider, never()).getCurrentSeason();
    }

    private Match match(Long matchId, LocalDateTime date, String status) {
        return Match.create(
                matchId,
                date,
                status,
                "Regular Season - 1",
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
