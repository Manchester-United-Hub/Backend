package backend.manuhub.coach;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoachCareer {

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "team_name")
    private String team;

    public static CoachCareer create(String startDate, String endDate, String team) {
        return new CoachCareer(startDate, endDate, team);
    }

    private CoachCareer(String startDate, String endDate, String team) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.team = team;
    }
}
