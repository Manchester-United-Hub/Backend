package backend.manuhub.teamstatistics;

import backend.manuhub.teamstatistics.dto.TeamStatisticsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamStatisticsServiceTest {

    @Mock
    private TeamStatisticsRepository teamStatisticsRepository;

    @InjectMocks
    private TeamStatisticsService teamStatisticsService;

    @Test
    @DisplayName("전체 팀 전적을 조회한다")
    void shouldGetAllTeamStatistics() {
        // given
        TeamStatistics statistics2022 = mock(TeamStatistics.class);
        TeamStatistics statistics2023 = mock(TeamStatistics.class);

        when(teamStatisticsRepository.findAllByOrderBySeasonAsc())
                .thenReturn(List.of(statistics2022, statistics2023));

        // when
        List<TeamStatisticsResponse> result =
                teamStatisticsService.getAllTeamStatistics();

        // then
        assertEquals(2, result.size());

        verify(teamStatisticsRepository, times(1))
                .findAllByOrderBySeasonAsc();
    }
    @Test
    @DisplayName("데이터가 없으면 빈 리스트를 반환한다")
    void shouldReturnEmptyList_whenNoTeamStatisticsExist() {
        // given
        when(teamStatisticsRepository.findAllByOrderBySeasonAsc())
                .thenReturn(List.of()); // 빈 리스트

        // when
        List<TeamStatisticsResponse> result =
                teamStatisticsService.getAllTeamStatistics();

        // then
        assertEquals(0, result.size());

        verify(teamStatisticsRepository, times(1))
                .findAllByOrderBySeasonAsc();
    }
}