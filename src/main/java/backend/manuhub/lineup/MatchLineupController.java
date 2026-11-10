package backend.manuhub.lineup;

import backend.manuhub.lineup.dto.MatchLineupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchLineupController implements MatchLineupAPI {

    private final MatchLineupService matchLineupService;

    @GetMapping("/{matchId}/lineups")
    public ResponseEntity<MatchLineupResponse> getLineup(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchLineupService.getLineup(matchId));
    }
}
