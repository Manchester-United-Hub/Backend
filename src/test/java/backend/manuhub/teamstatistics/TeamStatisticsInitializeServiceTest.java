package backend.manuhub.teamstatistics;

import backend.manuhub.external.teamstatistics.TeamStatisticsApiResponse;
import backend.manuhub.external.teamstatistics.TeamStatisticsClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamStatisticsInitializeServiceTest {

    @InjectMocks
    private TeamStatisticsInitializeService teamStatisticsInitializeService;

    @Mock
    private TeamStatisticsRepository teamStatisticsRepository;

    @Mock
    private TeamStatisticsClient teamStatisticsClient;

    @Test
    @DisplayName("정상적으로 팀 통계를 저장한다")
    void saveTeamStatistics_success() {
        // given
        Integer season = 2024;
        TeamStatisticsApiResponse mockResponse = createMockResponse();

        given(teamStatisticsRepository.existsBySeason(season)).willReturn(false);
        given(teamStatisticsClient.getTeamStatistics(season)).willReturn(Optional.of(mockResponse));

        // when
        teamStatisticsInitializeService.saveTeamStatistics(season);

        // then
        verify(teamStatisticsRepository, times(1)).save(any(TeamStatistics.class));
    }
//
//    @Test
//    @DisplayName("이미 존재하는 시즌이면 EntityAlreadyExistsException을 던진다")
//    void saveTeamStatistics_alreadyExists() {
//        // given
//        Integer season = 2024;
//        given(teamStatisticsRepository.existsBySeason(season)).willReturn(true);
//
//        // when & then
//        assertThatThrownBy(() -> teamStatisticsInitializeService.saveTeamStatistics(season))
//                .isInstanceOf(EntityAlreadyExistsException.class);
//
//        verify(teamStatisticsClient, never()).getTeamStatistics(any());
//        verify(teamStatisticsRepository, never()).save(any());
//    }

    @Test
    @DisplayName("빈 응답이면 저장하지 않는다")
    void saveTeamStatistics_emptyResponse() {
        // given
        Integer season = 2024;
        given(teamStatisticsRepository.existsBySeason(season)).willReturn(false);
        given(teamStatisticsClient.getTeamStatistics(season)).willReturn(Optional.empty());

        // when
        teamStatisticsInitializeService.saveTeamStatistics(season);

        // then
        verify(teamStatisticsRepository, never()).save(any());
    }

    private TeamStatisticsApiResponse createMockResponse() {
        TeamStatisticsApiResponse.Goals goals = new TeamStatisticsApiResponse.Goals(50, 30);
        TeamStatisticsApiResponse.All all = new TeamStatisticsApiResponse.All(38, 20, 10, 8, goals);
        TeamStatisticsApiResponse.Team team = new TeamStatisticsApiResponse.Team(33L, "Manchester United");
        TeamStatisticsApiResponse.Standing standing = new TeamStatisticsApiResponse.Standing(3, team, 70, all);
        TeamStatisticsApiResponse.League league = new TeamStatisticsApiResponse.League(2024, List.of(List.of(standing)));
        TeamStatisticsApiResponse.Response response = new TeamStatisticsApiResponse.Response(league);
        return new TeamStatisticsApiResponse(List.of(response));
    }
}