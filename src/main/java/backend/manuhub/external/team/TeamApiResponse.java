package backend.manuhub.external.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamApiResponse(List<TeamResponse> response) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamResponse(
            @JsonProperty("team") TeamInfo team,
            @JsonProperty("venue") VenueInfo venue) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(Long id, String name, String country, Integer founded, String logo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VenueInfo(String name, String city) {}

}
