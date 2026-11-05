package backend.manuhub.matchevent;


import backend.manuhub.common.entity.BaseTimeEntity;
import backend.manuhub.match.Match;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "elapsed_minute", nullable = false)
    private Integer elapsedMinute;

    @Column(name = "extra_minute")
    private Integer extraMinute;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "player_name")
    private String playerName;

    @Column(name = "assist_player_id")
    private Long assistPlayerId;

    @Column(name = "assist_player_name")
    private String assistPlayerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchEventType type;

    @Column(nullable = false)
    private String detail;

    private String comments;

    public static MatchEvent create(Match match, Integer elapsedMinute, Integer extraMinute,
                                    Long teamId, String teamName, Long playerId, String playerName,
                                    Long assistPlayerId, String assistPlayerName,
                                    MatchEventType type, String detail, String comments) {
        return MatchEvent.builder()
                .match(match)
                .elapsedMinute(elapsedMinute)
                .extraMinute(extraMinute)
                .teamId(teamId)
                .teamName(teamName)
                .playerId(playerId)
                .playerName(playerName)
                .assistPlayerId(assistPlayerId)
                .assistPlayerName(assistPlayerName)
                .type(type)
                .detail(detail)
                .comments(comments)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private MatchEvent(Match match, Integer elapsedMinute, Integer extraMinute,
                       Long teamId, String teamName, Long playerId, String playerName,
                       Long assistPlayerId, String assistPlayerName,
                       MatchEventType type, String detail, String comments) {
        this.match = match;
        this.elapsedMinute = elapsedMinute;
        this.extraMinute = extraMinute;
        this.teamId = teamId;
        this.teamName = teamName;
        this.playerId = playerId;
        this.playerName = playerName;
        this.assistPlayerId = assistPlayerId;
        this.assistPlayerName = assistPlayerName;
        this.type = type;
        this.detail = detail;
        this.comments = comments;
    }
}
