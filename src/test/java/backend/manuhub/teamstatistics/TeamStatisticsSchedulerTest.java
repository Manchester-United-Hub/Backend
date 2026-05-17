package backend.manuhub.teamstatistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TeamStatisticsSchedulerTest {

    @Mock
    private TeamStatisticsInitializeService teamStatisticsInitializeService;

    @InjectMocks
    private TeamStatisticsScheduler teamStatisticsScheduler;

    @Test
    @DisplayName("스케줄러 실행 시 팀 전적 저장 로직을 호출한다")
    void shouldCallSaveMethod_whenSchedulerRuns() {
        // when
        teamStatisticsScheduler.saveLastSeasonStatistics();

        // then
        verify(teamStatisticsInitializeService, times(1))
                .saveTeamStatistics(anyInt());
    }
}
