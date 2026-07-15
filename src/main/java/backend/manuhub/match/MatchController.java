package backend.manuhub.match;

import backend.manuhub.match.dto.MatchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchController implements MatchAPI {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchResponse>> getMatches(@RequestParam(required = false) Integer season) {
        return ResponseEntity.ok(matchService.getMatches(season));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getMatch(matchId));
    }
}
