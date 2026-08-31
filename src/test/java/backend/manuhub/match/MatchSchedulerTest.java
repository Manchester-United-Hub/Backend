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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchSchedulerTest {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 26, 21, 0);

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
    @DisplayName("경기 시작 후 3시간 이내의 경기가 있으면 해당 경기만 갱신한다")
    void updatesMatchesWhenMatchExistsInUpdateWindow() {
        Match match = match(100L, NOW.minusHours(1));
        when(matchRepository.findAllByDateAfterAndDateLessThanEqualOrderByDateAsc(NOW.minusHours(3), NOW))
                .thenReturn(List.of(match));

        matchScheduler.updateMatchesDuringMatchWindow();

        verify(matchInitializeService).updateMatch(100L);
        verify(matchInitializeService, never()).saveMatches(39L, 2026, 33L);
    }

    @Test
    @DisplayName("경기 시작 후 3시간 이내의 경기가 없으면 외부 API를 호출하지 않는다")
    void skipsUpdateWhenNoMatchExistsInUpdateWindow() {
        when(matchRepository.findAllByDateAfterAndDateLessThanEqualOrderByDateAsc(NOW.minusHours(3), NOW))
                .thenReturn(List.of());

        matchScheduler.updateMatchesDuringMatchWindow();

        verify(matchInitializeService, never()).updateMatch(anyLong());
        verify(seasonProvider, never()).getCurrentSeason();
    }

    private Match match(Long matchId, LocalDateTime date) {
        return Match.create(
                matchId,
                date,
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
