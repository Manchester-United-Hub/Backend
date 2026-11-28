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

    // 최초 선발·후보 명단을 유지하면서 선수별 이벤트 기록을 함께 전달한다.
    public record PlayerResponse(
            Long playerId,
            String name,
            Integer number,
            String position,
            String grid,
            String photo,
            List<GoalResponse> goals,
            List<AssistResponse> assists,
            List<CardResponse> cards,
            List<SubstitutionResponse> substitutions
    ) {
        public PlayerResponse(Long playerId, String name, Integer number, String position, String grid) {
            this(playerId, name, number, position, grid, null, List.of(), List.of(), List.of(), List.of());
        }
    }

    public enum GoalType { NORMAL, PENALTY, OWN_GOAL }

    public enum CardType { YELLOW, RED, SECOND_YELLOW }

    public enum SubstitutionDirection { IN, OUT }

    public record GoalResponse(Integer elapsedMinute, Integer extraMinute, GoalType type) {}

    public record AssistResponse(Integer elapsedMinute, Integer extraMinute,
                                 Long scorerPlayerId, String scorerPlayerName) {}

    public record CardResponse(Integer elapsedMinute, Integer extraMinute, CardType type) {}

    public record SubstitutionResponse(SubstitutionDirection direction, Integer elapsedMinute,
                                       Integer extraMinute, Long counterpartPlayerId, String counterpartPlayerName) {}
}
