package backend.manuhub.external.highlight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlaylistItemApiResponse(
        @JsonProperty("items") List<PlaylistItem> items,
        @JsonProperty("nextPageToken") String nextPageToken
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlaylistItem(
            @JsonProperty("snippet") SnippetInfo snippet
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SnippetInfo(
            @JsonProperty("title") String title,
            @JsonProperty("publishedAt") String publishedAt,
            @JsonProperty("thumbnails") ThumbnailsInfo thumbnails,
            @JsonProperty("resourceId") ResourceIdInfo resourceId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ThumbnailsInfo(
            @JsonProperty("high") ThumbnailInfo high
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ThumbnailInfo(
            @JsonProperty("url") String url
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceIdInfo(
            @JsonProperty("videoId") String videoId
    ) {}
}
