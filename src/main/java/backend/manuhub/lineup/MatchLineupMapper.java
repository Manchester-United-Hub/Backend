package backend.manuhub.lineup;

import backend.manuhub.coach.Coach;
import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.match.Match;
import backend.manuhub.team.Team;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class MatchLineupMapper {

    private static final String HOME = "home";
    private static final String AWAY = "away";
    private static final String STARTER = "starter";
    private static final String SUBSTITUTE = "substitute";

    private MatchLineupMapper() {
    }

    public static MatchLineupResponse toResponse(
            Match match,
            List<LineupApiResponse.Response> lineups,
            LineupReferenceData references
    ) {
        if (lineups.isEmpty()) {
            return new MatchLineupResponse(
                    match.getMatchId(),
                    MatchLineupResponse.LineupStatus.NOT_AVAILABLE,
                    null,
                    null
            );
        }

        try {
            MatchLineupResponse.TeamLineupResponse home = findTeamLineup(
                    lineups, match.getHomeTeamId(), references);
            MatchLineupResponse.TeamLineupResponse away = findTeamLineup(
                    lineups, match.getAwayTeamId(), references);
            MatchLineupResponse.LineupStatus status = home != null && away != null
                    ? MatchLineupResponse.LineupStatus.AVAILABLE
                    : MatchLineupResponse.LineupStatus.NOT_AVAILABLE;

            return new MatchLineupResponse(match.getMatchId(), status, home, away);
        } catch (NullPointerException e) {
            log.error(">>> MatchLineupMapper --> API-Football fixture lineups response is invalid. matchId={}",
                    match.getMatchId(), e);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_LINEUP_INVALID_RESPONSE_ERROR);
        }
    }

    public static MatchLineupResponse toResponseFromEntities(
            Match match,
            List<MatchLineup> lineups,
            List<LineupPlayer> players,
            LineupReferenceData references
    ) {
        Map<String, MatchLineup> lineupByTeamType = lineups.stream()
                .collect(Collectors.toMap(MatchLineup::getTeamType, Function.identity(), (first, ignored) -> first));

        MatchLineup homeLineup = lineupByTeamType.get(HOME);
        MatchLineup awayLineup = lineupByTeamType.get(AWAY);
        if (homeLineup == null || awayLineup == null) {
            return new MatchLineupResponse(
                    match.getMatchId(),
                    MatchLineupResponse.LineupStatus.NOT_AVAILABLE,
                    null,
                    null
            );
        }

        Map<Long, List<LineupPlayer>> playersByLineupId = players.stream()
                .collect(Collectors.groupingBy(LineupPlayer::getLineupId));

        MatchLineupResponse.TeamLineupResponse home = toTeamLineupResponse(
                homeLineup,
                playersByLineupId.getOrDefault(homeLineup.getId(), List.of()),
                references
        );
        MatchLineupResponse.TeamLineupResponse away = toTeamLineupResponse(
                awayLineup,
                playersByLineupId.getOrDefault(awayLineup.getId(), List.of()),
                references
        );

        return new MatchLineupResponse(
                match.getMatchId(),
                MatchLineupResponse.LineupStatus.AVAILABLE,
                home,
                away
        );
    }

    private static MatchLineupResponse.TeamLineupResponse findTeamLineup(
            List<LineupApiResponse.Response> lineups,
            Long teamId,
            LineupReferenceData references
    ) {
        return lineups.stream()
                .filter(lineup -> teamId.equals(lineup.team().id()))
                .findFirst()
                .map(lineup -> toTeamLineupResponse(lineup, references))
                .orElse(null);
    }

    private static MatchLineupResponse.TeamLineupResponse toTeamLineupResponse(
            LineupApiResponse.Response lineup,
            LineupReferenceData references
    ) {
        LineupApiResponse.Team lineupTeam = lineup.team();
        Team team = references.teams().get(lineupTeam.id());

        return new MatchLineupResponse.TeamLineupResponse(
                lineupTeam.id(),
                team == null ? lineupTeam.name() : team.getName(),
                team == null ? null : team.getLogoUrl(),
                lineup.formation(),
                toCoachResponse(lineup.coach(), references),
                toPlayerResponses(lineup.startPlayers()),
                toPlayerResponses(lineup.substitutes())
        );
    }

    private static MatchLineupResponse.CoachResponse toCoachResponse(
            LineupApiResponse.Coach lineupCoach,
            LineupReferenceData references
    ) {
        if (lineupCoach == null) {
            return null;
        }

        Coach coach = references.coaches().get(lineupCoach.id());
        return new MatchLineupResponse.CoachResponse(
                lineupCoach.id(),
                coach == null ? lineupCoach.name() : coach.getName(),
                coach == null ? null : coach.getPhoto()
        );
    }

    private static List<MatchLineupResponse.PlayerResponse> toPlayerResponses(
            List<LineupApiResponse.LineupPlayer> lineupPlayers
    ) {
        if (lineupPlayers == null) {
            return List.of();
        }

        return lineupPlayers.stream()
                .map(LineupApiResponse.LineupPlayer::player)
                .map(player -> new MatchLineupResponse.PlayerResponse(
                        player.id(),
                        player.name(),
                        player.number(),
                        player.pos(),
                        player.grid()
                ))
                .toList();
    }

    private static MatchLineupResponse.TeamLineupResponse toTeamLineupResponse(
            MatchLineup lineup,
            List<LineupPlayer> players,
            LineupReferenceData references
    ) {
        Team team = references.teams().get(lineup.getTeamId());
        return new MatchLineupResponse.TeamLineupResponse(
                lineup.getTeamId(),
                team == null ? null : team.getName(),
                team == null ? null : team.getLogoUrl(),
                lineup.getFormation(),
                toCoachResponse(lineup.getCoachId(), references),
                toPlayerResponses(players, STARTER),
                toPlayerResponses(players, SUBSTITUTE)
        );
    }

    private static MatchLineupResponse.CoachResponse toCoachResponse(
            Long coachId,
            LineupReferenceData references
    ) {
        if (coachId == null) {
            return null;
        }

        Coach coach = references.coaches().get(coachId);
        if (coach == null) {
            return null;
        }

        return new MatchLineupResponse.CoachResponse(
                coach.getId(),
                coach.getName(),
                coach.getPhoto()
        );
    }

    private static List<MatchLineupResponse.PlayerResponse> toPlayerResponses(
            List<LineupPlayer> players,
            String playerType
    ) {
        return players.stream()
                .filter(player -> playerType.equals(player.getPlayerType()))
                .map(player -> new MatchLineupResponse.PlayerResponse(
                        player.getPlayerId(),
                        player.getPlayerName(),
                        player.getNumber(),
                        player.getPosition(),
                        player.getGrid()
                ))
                .toList();
    }
}
