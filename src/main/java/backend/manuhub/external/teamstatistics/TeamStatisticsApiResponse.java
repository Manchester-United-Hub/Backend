package backend.manuhub.external.teamstatistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TeamStatisticsApiResponse(
        List<Response> response
) {

    public record Response(
            League league
    ) {}

    public record League(
            Integer season,
            List<List<Standing>> standings
    ) {}

    public record Standing(
            Integer rank,
            Team team,
            Integer points,
            All all
    ) {}

    public record Team(
            Long id,
            String name
    ) {}

    public record All(
            Integer played,
            Integer win,
            Integer draw,
            Integer lose,
            Goals goals
    ) {}

    public record Goals(
            @JsonProperty("for") Integer goalsFor,
            Integer against
    ) {}
}