package backend.manuhub.external.rank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamRankApiResponse(List<LeagueWrapper> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueWrapper(
            @JsonProperty("league") LeagueInfo league
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueInfo(
            @JsonProperty("standings") List<List<RankInfo>> standings
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RankInfo(
            @JsonProperty("rank") Integer rank,
            @JsonProperty("team") TeamInfo team,
            @JsonProperty("points") Integer points,
            @JsonProperty("goalsDiff") Integer goalsDiff,
            @JsonProperty("form") String form,
            @JsonProperty("all") AllInfo all
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("logo") String logo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AllInfo(
            @JsonProperty("played") Integer played,
            @JsonProperty("win") Integer win,
            @JsonProperty("draw") Integer draw,
            @JsonProperty("lose") Integer lose,
            @JsonProperty("goals") GoalsInfo goals
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalsInfo(
            @JsonProperty("for") Integer goalsFor,
            @JsonProperty("against") Integer goalsAgainst
    ) {}
}
