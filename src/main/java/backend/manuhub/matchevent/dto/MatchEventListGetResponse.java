package backend.manuhub.matchevent.dto;

import backend.manuhub.matchevent.MatchEvent;

import java.util.List;

public record MatchEventListGetResponse(
        Long matchId,
        List<MatchEventGetResponse> events
) {
    public static MatchEventListGetResponse of(Long matchId, List<MatchEvent> events) {
        return new MatchEventListGetResponse(
                matchId,
                events.stream().map(MatchEventGetResponse::from).toList()
        );
    }
}
