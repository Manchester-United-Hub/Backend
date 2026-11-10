package backend.manuhub.lineup;

import backend.manuhub.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_lineups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchLineup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lineup_id")
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "team_type", nullable = false)
    private String teamType;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "formation")
    private String formation;

    @Column(name = "coach_id")
    private Long coachId;

    public static MatchLineup create(Long matchId, String teamType, Long teamId, String formation, Long coachId) {
        return MatchLineup.builder()
                .matchId(matchId)
                .teamType(teamType)
                .teamId(teamId)
                .formation(formation)
                .coachId(coachId)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private MatchLineup(Long matchId, String teamType, Long teamId, String formation, Long coachId) {
        this.matchId = matchId;
        this.teamType = teamType;
        this.teamId = teamId;
        this.formation = formation;
        this.coachId = coachId;
    }
}
