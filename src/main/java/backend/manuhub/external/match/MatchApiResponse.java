package backend.manuhub.external.match;

import java.time.OffsetDateTime;
import java.util.List;

public record MatchApiResponse(
        List<Response> response
) {

    public record Response(
            Fixture fixture,
            League league,
            Teams teams,
            Goals goals
    ) {
    }

    public record Fixture(
            Long id,
            OffsetDateTime date,
            Venue venue
    ) {
    }

    public record Venue(
            Long id,
            String name,
            String city
    ) {
    }

    public record League(
            Long id,
            String name,
            String country,
            Integer season,
            String round
    ) {
    }

    public record Teams(
            Team home,
            Team away
    ) {
    }

    public record Team(
            Long id,
            String name,
            String logo,
            Boolean winner
    ) {
    }

    public record Goals(
            Integer home,
            Integer away
    ) {
    }
}
