package backend.manuhub.match.dto;

import backend.manuhub.match.Match;

import java.time.LocalDateTime;

public record MatchResponse(
        Long matchId,
        LocalDateTime date,
        VenueResponse venue,
        TeamResponse homeTeam,
        TeamResponse awayTeam,
        ScoreResponse score
) {
    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getMatchId(),
                match.getDate(),
                new VenueResponse(match.getVenueName(), match.getVenueCity()),
                new TeamResponse(
                        match.getHomeTeamId(),
                        match.getHomeTeamName(),
                        match.getHomeTeamLogo(),
                        match.getHomeTeamWinner()
                ),
                new TeamResponse(
                        match.getAwayTeamId(),
                        match.getAwayTeamName(),
                        match.getAwayTeamLogo(),
                        match.getAwayTeamWinner()
                ),
                ScoreResponse.from(match)
        );
    }

    public record VenueResponse(
            String name,
            String city
    ) {
    }

    public record TeamResponse(
            Long teamId,
            String name,
            String logo,
            Boolean winner
    ) {
    }

    public record ScoreResponse(
            Integer home,
            Integer away
    ) {
        private static ScoreResponse from(Match match) {
            if (match.getHomeScore() == null && match.getAwayScore() == null) {
                return null;
            }

            return new ScoreResponse(match.getHomeScore(), match.getAwayScore());
        }
    }
}
