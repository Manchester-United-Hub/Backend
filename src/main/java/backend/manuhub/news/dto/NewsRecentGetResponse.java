package backend.manuhub.news.dto;

import backend.manuhub.news.News;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record NewsRecentGetResponse(
        Long id,
        String title,
        String link
) {

    public static List<NewsRecentGetResponse> from(List<News> newsList) {
        return newsList.stream()
                .map(n -> NewsRecentGetResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .link(n.getLink())
                        .build())
                .toList();
    }
}
