package backend.manuhub.team;

import backend.manuhub.team.dto.TeamGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController implements TeamAPI{

    private final TeamDetailService teamDetailService;

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamGetResponse> getTeam(@PathVariable Long teamId) {
        TeamGetResponse result = teamDetailService.getTeamWithCoachName(teamId);
        return ResponseEntity.ok(result);
    }
}
