package backend.manuhub.teamstatistics;

import backend.manuhub.external.teamstatistics.TeamStatisticsApiResponse;
import backend.manuhub.external.teamstatistics.TeamStatisticsClient;
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
    private final TeamStatisticsClient teamStatisticsClient;

    @Transactional(readOnly = true)
    public List<TeamStatisticsResponse> getAllTeamStatistics() {

        log.info("[TeamStatistics] 전체 시즌 기록 조회");

        List<TeamStatistics> statisticsList = teamStatisticsRepository.findAllByOrderBySeasonAsc();

        return statisticsList.stream()
                .map(TeamStatisticsResponse::from)
                .toList();
    }

    @Transactional
    public void saveTeamStatistics(Integer season) {
        log.info("[TeamStatistics] {}년도 기록 저장 시작", season.toString());

        if(teamStatisticsRepository.existsBySeason(season)) {
            log.warn(">>> TeamStatistics --> EntityAlreadyExistsException. season = {}. TeamStatistics Entity Already Exists",
                    season);
            return;
        }

        TeamStatisticsApiResponse response =
                teamStatisticsClient.getTeamStatistics(season);

        TeamStatistics teamStatistics = response.toEntity();

        teamStatisticsRepository.save(teamStatistics);

        log.info("[TeamStatistics] {}년도 기록 저장 완료", season.toString());
    }
}
