package backend.manuhub.teamstatistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamStatisticsService {

    private final TeamStatisticsRepository teamStatisticsRepository;

    @Transactional(readOnly = true)
    public List<TeamStatisticsResponse> getAllTeamStatistics() {

        log.info("[TeamStatistics] 전체 시즌 기록 조회");

        List<TeamStatistics> statisticsList = teamStatisticsRepository.findAllByOrderBySeasonAsc();

        return statisticsList.stream()
                .sorted(Comparator.comparing(TeamStatistics::getSeason))
                .map(TeamStatisticsResponse::from)
                .toList();
    }
}
