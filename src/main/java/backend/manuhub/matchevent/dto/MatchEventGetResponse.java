package backend.manuhub.matchevent.dto;

import backend.manuhub.matchevent.MatchEvent;
import backend.manuhub.matchevent.MatchEventType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record MatchEventGetResponse(
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
    public static MatchEventGetResponse from(MatchEvent e) {
        return MatchEventGetResponse.builder()
                .elapsedMinute(e.getElapsedMinute())
                .extraMinute(e.getExtraMinute())
                .teamId(e.getTeamId())
                .teamName(e.getTeamName())
                .playerId(e.getPlayerId())
                .playerName(e.getPlayerName())
                .assistPlayerId(e.getAssistPlayerId())
                .assistPlayerName(e.getAssistPlayerName())
                .type(e.getType())
                .detail(e.getDetail())
                .comments(e.getComments())
                .build();
    }
}
