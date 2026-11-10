package backend.manuhub.lineup;

import backend.manuhub.coach.Coach;
import backend.manuhub.coach.CoachImageService;
import backend.manuhub.coach.CoachImageTarget;
import backend.manuhub.coach.CoachRepository;
import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.external.player.PlayerClient;
import backend.manuhub.external.player.PlayerProfileApiResponse;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerImageTarget;
import backend.manuhub.player.PlayerImageService;
import backend.manuhub.player.PlayerProfilePersistenceService;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.team.Team;
import backend.manuhub.team.TeamRepository;
import backend.manuhub.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LineupReferenceSyncService {

    private static final Long MANCHESTER_UNITED_TEAM_ID = 33L;

    private final TeamRepository teamRepository;
    private final TeamService teamService;
    private final CoachRepository coachRepository;
    private final CoachImageService coachImageService;
    private final PlayerRepository playerRepository;
    private final PlayerClient playerClient;
    private final PlayerProfilePersistenceService playerProfilePersistenceService;
    private final PlayerImageService playerImageService;

    public LineupReferenceData sync(List<MatchLineup> lineups, List<LineupPlayer> lineupPlayers) {
        syncTeams(lineups);
        syncManchesterUnitedPlayers(lineups, lineupPlayers);
        return loadReferences(lineups);
    }

    public LineupReferenceData syncApiReferences(List<LineupApiResponse.Response> responses) {
        responses.stream()
                .filter(response -> response.team() != null)
                .forEach(response -> {
                    teamService.syncLineupTeam(response.team());
                    syncCoach(response.team().id(), response.coach());
                });

        Set<Long> teamIds = responses.stream()
                .filter(response -> response.team() != null)
                .map(response -> response.team().id())
                .collect(Collectors.toSet());
        Set<Long> coachIds = responses.stream()
                .filter(response -> response.coach() != null)
                .map(response -> response.coach().id())
                .collect(Collectors.toSet());

        return loadReferences(teamIds, coachIds);
    }

    private void syncTeams(List<MatchLineup> lineups) {
        Set<Long> teamIds = lineups.stream()
                .map(MatchLineup::getTeamId)
                .collect(Collectors.toSet());
        Set<Long> savedTeamIds = teamRepository.findAllById(teamIds).stream()
                .map(Team::getId)
                .collect(Collectors.toSet());

        teamIds.stream()
                .filter(teamId -> !savedTeamIds.contains(teamId))
                .forEach(teamService::createTeam);
    }

    private void syncCoach(Long teamId, LineupApiResponse.Coach lineupCoach) {
        if (lineupCoach == null) {
            return;
        }

        Coach coach = coachRepository.findById(lineupCoach.id())
                .orElseGet(() -> coachRepository.save(
                        Coach.create(lineupCoach.id(), teamId, lineupCoach.name())
                ));
        coachImageService.uploadAndUpdate(new CoachImageTarget(
                lineupCoach.id(),
                lineupCoach.photo(),
                coach.getPhoto()
        ));
    }

    private LineupReferenceData loadReferences(List<MatchLineup> lineups) {
        Set<Long> teamIds = lineups.stream()
                .map(MatchLineup::getTeamId)
                .collect(Collectors.toSet());
        Set<Long> coachIds = lineups.stream()
                .map(MatchLineup::getCoachId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        return loadReferences(teamIds, coachIds);
    }

    private LineupReferenceData loadReferences(Set<Long> teamIds, Set<Long> coachIds) {
        Map<Long, Team> teams = teamRepository.findAllById(teamIds).stream()
                .collect(Collectors.toMap(Team::getId, Function.identity()));
        Map<Long, Coach> coaches = coachRepository.findAllById(coachIds).stream()
                .collect(Collectors.toMap(Coach::getId, Function.identity()));
        return new LineupReferenceData(teams, coaches);
    }

    private void syncManchesterUnitedPlayers(List<MatchLineup> lineups, List<LineupPlayer> lineupPlayers) {
        Set<Long> manchesterUnitedLineupIds = lineups.stream()
                .filter(lineup -> MANCHESTER_UNITED_TEAM_ID.equals(lineup.getTeamId()))
                .map(MatchLineup::getId)
                .collect(Collectors.toSet());

        Set<Long> playerIds = lineupPlayers.stream()
                .filter(lineupPlayer -> manchesterUnitedLineupIds.contains(lineupPlayer.getLineupId()))
                .map(LineupPlayer::getPlayerId)
                .collect(Collectors.toSet());
        Set<Long> savedPlayerIds = playerRepository.findAllById(playerIds).stream()
                .map(Player::getPlayerId)
                .collect(Collectors.toCollection(HashSet::new));

        List<PlayerProfileApiResponse.Player> missingProfiles = playerIds.stream()
                .filter(playerId -> !savedPlayerIds.contains(playerId))
                .map(playerClient::getPlayerProfile)
                .toList();

        if (missingProfiles.isEmpty()) {
            return;
        }

        List<PlayerImageTarget> imageTargets = playerProfilePersistenceService.saveMissing(missingProfiles);
        playerImageService.uploadAndUpdate(imageTargets);
    }
}
