package backend.manuhub.news.dto;

import backend.manuhub.news.News;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record NewsGetResponse(
        Long id,
        String title,
        String description,
        String link,
        String originalLink,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime publishedAt
) {

    public static NewsGetResponse from(News news) {
        return NewsGetResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .description(news.getDescription())
                .link(news.getLink())
                .originalLink(news.getOriginalLink())
                .publishedAt(news.getPublishedAt())
                .build();
    }
}
