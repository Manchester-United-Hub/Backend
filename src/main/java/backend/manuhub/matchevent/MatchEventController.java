package backend.manuhub.matchevent;

import backend.manuhub.matchevent.dto.MatchEventListGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchEventController implements MatchEventAPI{

    private final MatchEventService matchEventService;

    @GetMapping("/{matchId}/events")
    public ResponseEntity<MatchEventListGetResponse> getMatchEvents(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchEventService.getMatchEvents(matchId));
    }
}
