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

    // 선발·후보 명단을 유지하면서 각 선수의 경기 기록을 화면에 함께 표시하기 위한 응답이다.
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
            // 기존 라인업 매퍼는 기본 정보만 만들고, 조회 시 저장된 사진과 이벤트를 조합한다.
            this(playerId, name, number, position, grid, null, List.of(), List.of(), List.of(), List.of());
        }
    }

    public enum GoalType { NORMAL, PENALTY, OWN_GOAL }

    public enum CardType { YELLOW, RED, SECOND_YELLOW }

    public enum SubstitutionDirection { IN, OUT }

    public record GoalResponse(Integer elapsedMinute, Integer extraMinute, GoalType type) {}

    // 도움을 기록한 선수에게 득점 시간과 득점자를 함께 전달해 득점 기록과 별도로 표시한다.
    public record AssistResponse(
            Integer elapsedMinute,
            Integer extraMinute,
            Long scorerPlayerId,
            String scorerPlayerName
    ) {}

    public record CardResponse(Integer elapsedMinute, Integer extraMinute, CardType type) {}

    // 교체로 들어온 선수가 다시 나갈 수 있으므로 방향과 상대 선수를 목록으로 전달한다.
    public record SubstitutionResponse(
            SubstitutionDirection direction,
            Integer elapsedMinute,
            Integer extraMinute,
            Long counterpartPlayerId,
            String counterpartPlayerName
    ) {}
}
