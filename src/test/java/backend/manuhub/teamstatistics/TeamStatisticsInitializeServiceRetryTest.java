package backend.manuhub.teamstatistics;

import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.teamstatistics.TeamStatisticsClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TeamStatisticsInitializeServiceRetryTest {

    @Autowired
    private TeamStatisticsInitializeService teamStatisticsInitializeService;

    @MockitoBean
    private TeamStatisticsRepository teamStatisticsRepository;

    @MockitoBean
    private TeamStatisticsClient teamStatisticsClient;

    @Test
    @DisplayName("ApiServerException 발생시 재시도 후 recover가 호출된다")
    void saveTeamStatistics_retryAndRecover() {
        // given
        Integer season = 2024;
        given(teamStatisticsRepository.existsBySeason(season)).willReturn(false);
        given(teamStatisticsClient.getTeamStatistics(season))
                .willThrow(new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR));

        // when
        teamStatisticsInitializeService.saveTeamStatistics(season);

        // then
        verify(teamStatisticsClient, times(4)).getTeamStatistics(season); // maxAttempts 기본값
    }
}