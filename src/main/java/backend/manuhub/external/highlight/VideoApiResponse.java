package backend.manuhub.external.highlight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoApiResponse(
        @JsonProperty("items") List<VideoItem> items
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoItem(
            @JsonProperty("id") String id,
            @JsonProperty("statistics") StatisticsInfo statistics
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatisticsInfo(
            @JsonProperty("viewCount") Long viewCount
    ) {}
}
