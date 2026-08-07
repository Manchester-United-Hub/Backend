package backend.manuhub.season;

import backend.manuhub.season.dto.SeasonResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonServiceTest {

    @Mock
    private SeasonRepository seasonRepository;

    @ParameterizedTest(name = "{0}: season={2}, started={3}")
    @MethodSource("seasonCases")
    @DisplayName("현재 날짜에 맞는 시즌과 시작 여부를 반환한다")
    void returnsSeasonAndStartedStatus(
            LocalDate today,
            Season savedSeason,
            Integer expectedSeason,
            boolean expectedStarted
    ) {
        Clock clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        SeasonService seasonService = new SeasonService(seasonRepository, clock);
        when(seasonRepository.findFirstByEndDateGreaterThanEqualOrderByStartDateAsc(today))
                .thenReturn(Optional.of(savedSeason));

        SeasonResponse result = seasonService.getCurrentSeason();

        assertEquals(expectedSeason, result.season());
        assertEquals(expectedStarted, result.started());
        verify(seasonRepository).findFirstByEndDateGreaterThanEqualOrderByStartDateAsc(today);
    }

    private static Stream<Arguments> seasonCases() {
        Season season2025 = Season.create(
                2025,
                LocalDate.of(2025, 8, 16),
                LocalDate.of(2026, 5, 24)
        );
        Season season2026 = Season.create(
                2026,
                LocalDate.of(2026, 8, 21),
                LocalDate.of(2027, 5, 23)
        );

        return Stream.of(
                Arguments.of(LocalDate.of(2026, 4, 10), season2025, 2025, true),
                Arguments.of(LocalDate.of(2026, 6, 1), season2026, 2026, false),
                Arguments.of(LocalDate.of(2026, 8, 21), season2026, 2026, true)
        );
    }
}
