package backend.manuhub.season;

import backend.manuhub.season.dto.SeasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seasons")
public class SeasonController implements SeasonAPI {

    private final SeasonService seasonService;

    @GetMapping("/current")
    public ResponseEntity<SeasonResponse> getCurrentSeason() {
        return ResponseEntity.ok(seasonService.getCurrentSeason());
    }
}
