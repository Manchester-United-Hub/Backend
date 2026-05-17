package backend.manuhub.teamstatistics;

import backend.manuhub.teamstatistics.dto.TeamStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team/statistics")
public class TeamStatisticsController {

    private final TeamStatisticsService teamStatisticsService;


    @GetMapping
    public ResponseEntity<List<TeamStatisticsResponse>> getAllTeamStatistics() {
        List<TeamStatisticsResponse> response = teamStatisticsService.getAllTeamStatistics();

        return ResponseEntity.ok(response);
    }
}
