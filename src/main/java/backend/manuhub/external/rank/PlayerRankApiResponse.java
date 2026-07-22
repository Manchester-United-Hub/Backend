package backend.manuhub.external.rank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerRankApiResponse(List<PlayerRankInfo> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerRankInfo(
            @JsonProperty("player") PlayerInfo player,
            @JsonProperty("statistics") List<StatisticsInfo> statistics
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerInfo(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("photo") String photo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatisticsInfo(
            @JsonProperty("team") TeamInfo team,
            @JsonProperty("games") GamesInfo games,
            @JsonProperty("shots") ShotsInfo shots,
            @JsonProperty("goals") GoalsInfo goals,
            @JsonProperty("passes") PassesInfo passes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("logo") String logo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GamesInfo(
            @JsonProperty("appearences") Integer appearences,
            @JsonProperty("minutes") Integer minutes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ShotsInfo(
            @JsonProperty("total") Integer total,
            @JsonProperty("on") Integer on
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalsInfo(
            @JsonProperty("total") Integer total,
            @JsonProperty("assists") Integer assists
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PassesInfo(
            @JsonProperty("key") Integer key
    ) {}
}