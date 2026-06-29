package backend.manuhub.player;

import backend.manuhub.player.dto.PlayerResponse;
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
public class PlayerController implements PlayerAPI {

    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getPlayers(@RequestParam Integer season) {
        return ResponseEntity.ok(playerService.getPlayers(season));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> getPlayer(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(playerService.getPlayer(playerId, season));
    }
}
