package backend.manuhub.lineup;

import backend.manuhub.coach.Coach;
import backend.manuhub.team.Team;

import java.util.Map;

public record LineupReferenceData(
        Map<Long, Team> teams,
        Map<Long, Coach> coaches
) {
}
