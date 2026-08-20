package backend.manuhub.match.dto;

import backend.manuhub.match.Match;

import java.time.LocalDateTime;
import java.util.List;

public record MatchListResponse(
        List<MatchResponse> pastMatches,
        List<MatchResponse> upcomingMatches
) {

    public static MatchListResponse from(List<Match> matches, LocalDateTime now) {
        List<MatchResponse> pastMatches = matches.stream()
                .filter(match -> !match.getDate().isAfter(now))
                .map(MatchResponse::from)
                .toList();
        List<MatchResponse> upcomingMatches = matches.stream()
                .filter(match -> match.getDate().isAfter(now))
                .map(MatchResponse::from)
                .toList();

        return new MatchListResponse(pastMatches, upcomingMatches);
    }
}
