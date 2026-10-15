package backend.manuhub.highlight.dto;

import java.time.LocalDateTime;

public record HighlightResponse(String videoId,
                                String title,
                                String thumbnailUrl,
                                LocalDateTime publishedAt,
                                Long viewCount) {

    public static HighlightResponse create(String videoId, String title, String thumbnailUrl,
                                           LocalDateTime publishedAt, Long viewCount) {
        return new HighlightResponse(videoId, title, thumbnailUrl, publishedAt, viewCount);
    }
}
