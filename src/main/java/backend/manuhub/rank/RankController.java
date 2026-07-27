package backend.manuhub.rank;

import backend.manuhub.rank.dto.PlayerRankGetResponse;
import backend.manuhub.rank.dto.TeamRankGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankController implements RankAPI{

    private final RankService rankService;

    @GetMapping("/premier-league")
    public ResponseEntity<TeamRankGetResponse> getPLRank() {
        TeamRankGetResponse result = rankService.getRank();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/premier-league/topscorers")
    public ResponseEntity<PlayerRankGetResponse> getPLTopScorers() {
        PlayerRankGetResponse result = rankService.getTopScorers();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/premier-league/topassists")
    public ResponseEntity<PlayerRankGetResponse> getPLTopAssists() {
        PlayerRankGetResponse result = rankService.getTopAssists();
        return ResponseEntity.ok(result);
    }
}
