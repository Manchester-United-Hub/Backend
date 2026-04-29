package backend.manuhub.external.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class NaverNewsResponse {

    private List<NaverNewsItem> items = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    public static class NaverNewsItem {

        @JsonProperty("title")
        private String title;

        @JsonProperty("originallink")
        private String originalLink;

        @JsonProperty("link")
        private String link;

        @JsonProperty("description")
        private String description;

        @JsonProperty("pubDate")
        private String publishedAt;
    }
}
