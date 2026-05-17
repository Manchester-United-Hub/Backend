package backend.manuhub.teamstatistics;

import backend.manuhub.teamstatistics.dto.TeamStatisticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamStatisticsService {

    private final TeamStatisticsRepository teamStatisticsRepository;

    @Transactional(readOnly = true)
    public List<TeamStatisticsResponse> getAllTeamStatistics() {
        List<TeamStatistics> statisticsList = teamStatisticsRepository.findAllByOrderBySeasonAsc();

        return statisticsList.stream()
                .map(TeamStatisticsResponse::from)
                .toList();
    }
}
