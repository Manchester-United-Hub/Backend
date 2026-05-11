package backend.manuhub.news.dto;

import backend.manuhub.news.News;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record NewsListGetResponse(
        List<NewsGetResponse> newsList,
        Long nextCursorId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime nextCursorAt
) {
    public static NewsListGetResponse from(List<News> list) {
        List<NewsGetResponse> newsList = list.stream()
                .map(NewsGetResponse::from)
                .toList();

        Long lastNewsId = null;
        LocalDateTime lastNewsPublishedAt = null;
        if(!newsList.isEmpty()){
            NewsGetResponse lastNews = newsList.getLast();
            lastNewsId = lastNews.id();
            lastNewsPublishedAt = lastNews.publishedAt();
        }

        return NewsListGetResponse.builder()
                .newsList(newsList)
                .nextCursorId(lastNewsId)
                .nextCursorAt(lastNewsPublishedAt)
                .build();
    }
}
