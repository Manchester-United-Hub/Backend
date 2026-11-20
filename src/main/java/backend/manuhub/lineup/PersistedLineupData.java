package backend.manuhub.lineup;

import backend.manuhub.match.Match;

import java.util.List;

public record PersistedLineupData(
        Match match,
        List<MatchLineup> lineups,
        List<LineupPlayer> players
) {
}
