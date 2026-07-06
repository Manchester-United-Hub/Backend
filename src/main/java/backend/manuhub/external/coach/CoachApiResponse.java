package backend.manuhub.external.coach;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoachApiResponse(List<CoachResponse> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoachResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("team") TeamInfo team,
            @JsonProperty("career") List<CareerInfo> career
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
            @JsonProperty("id") Long id
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CareerInfo( @JsonProperty("team") TeamInfo team,
                              @JsonProperty("start") String start
    ) {
    }
}
