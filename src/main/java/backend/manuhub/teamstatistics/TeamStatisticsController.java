package backend.manuhub.teamstatistics;

import backend.manuhub.exception.ManuHubException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team-statistics")
public class TeamStatisticsController {

    private final TeamStatisticsService teamStatisticsService;


    @GetMapping
    public ResponseEntity<List<TeamStatisticsResponse>> getAllTeamStatistics() {
        List<TeamStatisticsResponse> response = teamStatisticsService.getAllTeamStatistics();

        return ResponseEntity.ok(response);
    }
}
