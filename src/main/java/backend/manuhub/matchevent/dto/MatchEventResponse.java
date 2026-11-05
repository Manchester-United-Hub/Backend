package backend.manuhub.matchevent.dto;

import backend.manuhub.matchevent.MatchEventType;

public record MatchEventResponse(
        Integer elapsedMinute,
        Integer extraMinute,
        Long teamId,
        String teamName,
        Long playerId,
        String playerName,
        Long assistPlayerId,
        String assistPlayerName,
        MatchEventType type,
        String detail,
        String comments
) {
    public static MatchEventResponse create(Integer elapsedMinute, Integer extraMinute,
                                            Long teamId, String teamName,
                                            Long playerId, String playerName,
                                            Long assistPlayerId, String assistPlayerName,
                                            MatchEventType type, String detail, String comments) {
        return new MatchEventResponse(elapsedMinute, extraMinute, teamId, teamName,
                playerId, playerName, assistPlayerId, assistPlayerName, type, detail, comments);
    }
}
