package backend.manuhub.external.highlight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlaylistSearchApiResponse(
        @JsonProperty("items") List<SearchItem> items
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchItem(
            @JsonProperty("id") IdInfo id,
            @JsonProperty("snippet") SnippetInfo snippet
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IdInfo(
            @JsonProperty("playlistId") String playlistId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SnippetInfo(
            @JsonProperty("title") String title
    ) {}
}
