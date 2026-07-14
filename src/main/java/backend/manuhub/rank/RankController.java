package backend.manuhub.rank;

import backend.manuhub.rank.dto.RankGetResponse;
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
    public ResponseEntity<RankGetResponse> getPLRank() {
        RankGetResponse result = rankService.getRank();
        return ResponseEntity.ok(result);
    }
}
