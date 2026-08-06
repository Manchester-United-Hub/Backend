package backend.manuhub.seasonplayer;

import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
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
@RequestMapping("/api/players")
public class SeasonPlayerController implements SeasonPlayerAPI {

    private final SeasonPlayerService seasonPlayerService;

    @GetMapping
    public ResponseEntity<List<SeasonPlayerResponse>> getSeasonPlayers(
            @RequestParam(required = false) Integer season) {
        return ResponseEntity.ok(seasonPlayerService.getSeasonPlayers(season));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<SeasonPlayerResponse> getSeasonPlayer(
            @PathVariable Long playerId,
            @RequestParam(required = false) Integer season) {
        return ResponseEntity.ok(seasonPlayerService.getSeasonPlayer(playerId, season));
    }
}
