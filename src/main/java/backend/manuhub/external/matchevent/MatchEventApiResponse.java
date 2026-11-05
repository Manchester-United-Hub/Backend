package backend.manuhub.external.matchevent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchEventApiResponse(
        @JsonProperty("response") List<Event> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Event(
            @JsonProperty("time") TimeInfo time,
            @JsonProperty("team") TeamInfo team,
            @JsonProperty("player") PlayerInfo player,
            @JsonProperty("assist") PlayerInfo assist,
            @JsonProperty("type") String type,
            @JsonProperty("detail") String detail,
            @JsonProperty("comments") String comments
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TimeInfo(
            @JsonProperty("elapsed") Integer elapsed,
            @JsonProperty("extra") Integer extra
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerInfo(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {}
}
