package backend.manuhub.lineup;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.external.lineup.LineupClient;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchLineupService {

    private static final String HOME = "home";
    private static final String AWAY = "away";
    private static final String STARTER = "starter";
    private static final String SUBSTITUTE = "substitute";

    private final MatchRepository matchRepository;
    private final LineupClient lineupClient;
    private final MatchLineupRepository matchLineupRepository;
    private final LineupPlayerRepository lineupPlayerRepository;
    private final LineupReferenceSyncService lineupReferenceSyncService;

    @Transactional
    public MatchLineupResponse getLineup(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));

        List<MatchLineup> savedLineups = matchLineupRepository.findAllByMatchId(matchId);
        if (hasHomeAndAway(savedLineups)) {
            List<Long> lineupIds = savedLineups.stream()
                    .map(MatchLineup::getId)
                    .toList();
            List<LineupPlayer> savedPlayers =
                    lineupPlayerRepository.findAllByLineupIdInOrderByLineupIdAscDisplayOrderAsc(lineupIds);

            match.completeLineupSync();
            LineupReferenceData references = lineupReferenceSyncService.sync(savedLineups, savedPlayers);
            return MatchLineupMapper.toResponseFromEntities(match, savedLineups, savedPlayers, references);
        }

        List<LineupApiResponse.Response> lineups = lineupClient.getLineups(matchId);
        LineupReferenceData references = lineupReferenceSyncService.syncApiReferences(lineups);
        MatchLineupResponse apiResponse = MatchLineupMapper.toResponse(match, lineups, references);
        LineupApiResponse.Response home = findTeamLineup(lineups, match.getHomeTeamId());
        LineupApiResponse.Response away = findTeamLineup(lineups, match.getAwayTeamId());

        if (home == null || away == null) {
            return apiResponse;
        }

        removeIncompleteLineups(savedLineups);
        List<MatchLineup> persistedLineups = List.of(
                saveLineup(matchId, HOME, home),
                saveLineup(matchId, AWAY, away)
        );
        List<LineupPlayer> persistedPlayers = new ArrayList<>();
        persistedPlayers.addAll(savePlayers(persistedLineups.get(0).getId(), home));
        persistedPlayers.addAll(savePlayers(persistedLineups.get(1).getId(), away));

        match.completeLineupSync();
        references = lineupReferenceSyncService.sync(persistedLineups, persistedPlayers);
        return MatchLineupMapper.toResponseFromEntities(match, persistedLineups, persistedPlayers, references);
    }

    @Transactional
    public void backfillLineup(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));

        List<MatchLineup> savedLineups = matchLineupRepository.findAllByMatchId(matchId);
        if (hasHomeAndAway(savedLineups)) {
            match.completeLineupSync();
            return;
        }

        List<LineupApiResponse.Response> lineups = lineupClient.getLineups(matchId);
        lineupReferenceSyncService.syncApiReferences(lineups);
        LineupApiResponse.Response home = findTeamLineup(lineups, match.getHomeTeamId());
        LineupApiResponse.Response away = findTeamLineup(lineups, match.getAwayTeamId());

        if (home == null || away == null) {
            match.failLineupSync();
            return;
        }

        removeIncompleteLineups(savedLineups);
        MatchLineup homeLineup = saveLineup(matchId, HOME, home);
        MatchLineup awayLineup = saveLineup(matchId, AWAY, away);
        savePlayers(homeLineup.getId(), home);
        savePlayers(awayLineup.getId(), away);
        match.completeLineupSync();
    }

    private boolean hasHomeAndAway(List<MatchLineup> lineups) {
        boolean hasHome = lineups.stream().anyMatch(lineup -> HOME.equals(lineup.getTeamType()));
        boolean hasAway = lineups.stream().anyMatch(lineup -> AWAY.equals(lineup.getTeamType()));
        return hasHome && hasAway;
    }

    private LineupApiResponse.Response findTeamLineup(
            List<LineupApiResponse.Response> lineups,
            Long teamId
    ) {
        return lineups.stream()
                .filter(lineup -> lineup.team() != null && teamId.equals(lineup.team().id()))
                .findFirst()
                .orElse(null);
    }

    private void removeIncompleteLineups(List<MatchLineup> lineups) {
        if (lineups.isEmpty()) {
            return;
        }

        List<Long> lineupIds = lineups.stream()
                .map(MatchLineup::getId)
                .toList();
        lineupPlayerRepository.deleteAllByLineupIdIn(lineupIds);
        matchLineupRepository.deleteAll(lineups);
    }

    private MatchLineup saveLineup(
            Long matchId,
            String teamType,
            LineupApiResponse.Response response
    ) {
        LineupApiResponse.Coach coach = response.coach();
        MatchLineup lineup = MatchLineup.create(
                matchId,
                teamType,
                response.team().id(),
                response.formation(),
                coach == null ? null : coach.id()
        );
        return matchLineupRepository.save(lineup);
    }

    private List<LineupPlayer> savePlayers(
            Long lineupId,
            LineupApiResponse.Response response
    ) {
        List<LineupPlayer> players = new ArrayList<>();
        addPlayers(players, lineupId, response.startPlayers(), STARTER);
        addPlayers(players, lineupId, response.substitutes(), SUBSTITUTE);
        return lineupPlayerRepository.saveAll(players);
    }

    private void addPlayers(
            List<LineupPlayer> target,
            Long lineupId,
            List<LineupApiResponse.LineupPlayer> source,
            String playerType
    ) {
        if (source == null) {
            return;
        }

        for (int index = 0; index < source.size(); index++) {
            LineupApiResponse.Player player = source.get(index).player();
            target.add(LineupPlayer.create(
                    lineupId,
                    player.id(),
                    player.name(),
                    player.number(),
                    player.pos(),
                    player.grid(),
                    playerType,
                    index
            ));
        }
    }
}
