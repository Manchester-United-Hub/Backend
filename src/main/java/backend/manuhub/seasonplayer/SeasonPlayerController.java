package backend.manuhub.seasonplayer;

import backend.manuhub.seasonplayer.dto.SeasonPlayerListResponse;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/players")
public class SeasonPlayerController implements SeasonPlayerAPI {

    private final SeasonPlayerService seasonPlayerService;

    @GetMapping
    public ResponseEntity<SeasonPlayerListResponse> getSeasonPlayers(
            @RequestParam(required = false) Integer season,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(seasonPlayerService.getSeasonPlayers(season, page, size));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<SeasonPlayerResponse> getSeasonPlayer(
            @PathVariable Long playerId,
            @RequestParam(required = false) Integer season) {
        return ResponseEntity.ok(seasonPlayerService.getSeasonPlayer(playerId, season));
    }
}
