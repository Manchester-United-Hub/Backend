package backend.manuhub.rank;

import backend.manuhub.rank.dto.PlayerRankGetResponse;
import backend.manuhub.rank.dto.TeamRankGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankController implements RankAPI{

    private final RankService rankService;

    @GetMapping("/premier-league")
    public ResponseEntity<TeamRankGetResponse> getPLRank(@RequestParam int season) {
        TeamRankGetResponse result = rankService.getRank(season);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/premier-league/topscorers")
    public ResponseEntity<PlayerRankGetResponse> getPLTopScorers(@RequestParam int season) {
        PlayerRankGetResponse result = rankService.getTopScorers(season);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/premier-league/topassists")
    public ResponseEntity<PlayerRankGetResponse> getPLTopAssists(@RequestParam int season) {
        PlayerRankGetResponse result = rankService.getTopAssists(season);
        return ResponseEntity.ok(result);
    }
}
