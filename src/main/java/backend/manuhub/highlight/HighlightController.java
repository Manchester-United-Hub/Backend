package backend.manuhub.highlight;

import backend.manuhub.highlight.dto.HighlightListGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/highlights")
public class HighlightController implements HighlightAPI{

    private final HighlightService highlightService;

    @GetMapping
    public ResponseEntity<HighlightListGetResponse> getHighlights(@RequestParam int season,
                                                                  @RequestParam(defaultValue = "LATEST") HighlightSort sort,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        HighlightListGetResponse result = highlightService.getHighlights(season, sort, page, size);
        return ResponseEntity.ok(result);
    }
}
