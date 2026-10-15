package backend.manuhub.highlight.dto;

import backend.manuhub.highlight.Highlight;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record HighlightGetResponse(
        String videoId,
        String title,
        String thumbnailUrl,
        String embedUrl,
        LocalDateTime publishedAt,
        Long viewCount
) {
    private static final String EMBED_URL_PREFIX = "https://www.youtube.com/embed/";

    public static HighlightGetResponse from(Highlight highlight) {
        return HighlightGetResponse.builder()
                .videoId(highlight.getVideoId())
                .title(highlight.getTitle())
                .thumbnailUrl(highlight.getThumbnailUrl())
                .embedUrl(EMBED_URL_PREFIX + highlight.getVideoId())
                .publishedAt(highlight.getPublishedAt())
                .viewCount(highlight.getViewCount())
                .build();
    }
}