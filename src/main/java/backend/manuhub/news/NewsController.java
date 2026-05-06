package backend.manuhub.news;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/news")
    public ResponseEntity<NewsListGetResponse> getNewsList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {
        NewsListGetResponse newsList = newsService.getNewsList(cursorAt, cursorId, size);
        return ResponseEntity.ok(newsList);
    }
}
