package backend.manuhub.lineup;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchLineupPersistenceService {

    private static final String HOME = "home";
    private static final String AWAY = "away";
    private static final String STARTER = "starter";
    private static final String SUBSTITUTE = "substitute";

    private final MatchRepository matchRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final LineupPlayerRepository lineupPlayerRepository;

    /**
     * 외부 API와 R2 작업이 끝난 결과만 받아 라인업, 선수, 동기화 상태를 하나의 짧은
     * 트랜잭션으로 저장한다. 일부만 저장된 상태가 남지 않도록 이 책임을 별도 빈으로 분리했다.
     */
    @Transactional
    public PersistedLineupData replaceLineups(
            Long matchId,
            LineupApiResponse.Response home,
            LineupApiResponse.Response away
    ) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        List<MatchLineup> existingLineups = matchLineupRepository.findAllByMatchId(matchId);

        // 외부 작업 중 다른 요청이 먼저 저장했을 수 있으므로 저장 직전에 다시 확인한다.
        if (hasHomeAndAway(existingLineups)) {
            List<LineupPlayer> existingPlayers = loadPlayers(existingLineups);
            match.completeLineupSync();
            return new PersistedLineupData(match, existingLineups, existingPlayers);
        }

        removeIncompleteLineups(existingLineups);
        List<MatchLineup> persistedLineups = List.of(
                saveLineup(matchId, HOME, home),
                saveLineup(matchId, AWAY, away)
        );
        List<LineupPlayer> persistedPlayers = new ArrayList<>();
        persistedPlayers.addAll(savePlayers(persistedLineups.get(0).getId(), home));
        persistedPlayers.addAll(savePlayers(persistedLineups.get(1).getId(), away));
        match.completeLineupSync();

        return new PersistedLineupData(match, persistedLineups, persistedPlayers);
    }

    private boolean hasHomeAndAway(List<MatchLineup> lineups) {
        boolean hasHome = lineups.stream().anyMatch(lineup -> HOME.equals(lineup.getTeamType()));
        boolean hasAway = lineups.stream().anyMatch(lineup -> AWAY.equals(lineup.getTeamType()));
        return hasHome && hasAway;
    }

    private List<LineupPlayer> loadPlayers(List<MatchLineup> lineups) {
        List<Long> lineupIds = lineups.stream()
                .map(MatchLineup::getId)
                .toList();
        return lineupPlayerRepository.findAllByLineupIdInOrderByLineupIdAscDisplayOrderAsc(lineupIds);
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
