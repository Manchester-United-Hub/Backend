package backend.manuhub.external.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsResponse(
        @JsonProperty("items") List<NaverNewsItem> items
) {
    public NaverNewsResponse {
        items = items != null ? items : List.of();
    }
    public record NaverNewsItem(
            @JsonProperty("title") String title,
            @JsonProperty("originallink") String originalLink,
            @JsonProperty("link") String link,
            @JsonProperty("description") String description,
            @JsonProperty("pubDate") String publishedAt
    ) {}
}



