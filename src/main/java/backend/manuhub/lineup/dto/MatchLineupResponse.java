package backend.manuhub.lineup.dto;

import java.util.List;

public record MatchLineupResponse(
        Long matchId,
        LineupStatus lineupStatus,
        TeamLineupResponse home,
        TeamLineupResponse away
) {

    public enum LineupStatus {
        AVAILABLE,
        NOT_AVAILABLE
    }

    public record TeamLineupResponse(
            Long teamId,
            String name,
            String logo,
            String formation,
            CoachResponse coach,
            List<PlayerResponse> startPlayers,
            List<PlayerResponse> substitutes
    ) {
    }

    public record CoachResponse(
            Long coachId,
            String name,
            String photo
    ) {
    }

    public record PlayerResponse(
            Long playerId,
            String name,
            Integer number,
            String position,
            String grid
    ) {
    }
}
