package backend.manuhub.playerdetail;

import backend.manuhub.playerdetail.dto.PlayerDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/player-details")
public class PlayerDetailController implements PlayerDetailAPI {

    private final PlayerDetailService playerDetailService;

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerDetailResponse> getPlayerDetail(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(playerDetailService.getPlayerDetail(playerId, season));
    }
}
