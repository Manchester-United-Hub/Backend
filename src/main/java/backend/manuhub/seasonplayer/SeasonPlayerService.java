package backend.manuhub.seasonplayer;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class SeasonPlayerService {

    private final SeasonPlayerRepository seasonPlayerRepository;
    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public List<SeasonPlayerResponse> getSeasonPlayers(Integer season) {
        List<Player> players = season == null
                ? playerRepository.findAllByOrderByNameAsc()
                : seasonPlayerRepository.findAllBySeasonWithPlayer(season).stream()
                        .map(SeasonPlayer::getPlayer)
                        .toList();

        return toResponses(players);
    }

    @Transactional(readOnly = true)
    public SeasonPlayerResponse getSeasonPlayer(Long playerId, Integer season) {
        Player player = season == null
                ? playerRepository.findById(playerId)
                        .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR))
                : seasonPlayerRepository.findById(new SeasonPlayerId(playerId, season))
                        .map(SeasonPlayer::getPlayer)
                        .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));

        List<Integer> seasons = seasonPlayerRepository.findAllByPlayerIdOrderBySeasonAsc(playerId).stream()
                .map(SeasonPlayer::getSeason)
                .toList();
        return SeasonPlayerResponse.from(player, seasons);
    }

    private List<SeasonPlayerResponse> toResponses(List<Player> players) {
        if (players.isEmpty()) {
            return List.of();
        }

        Collection<Long> playerIds = players.stream()
                .map(Player::getPlayerId)
                .toList();
        Map<Long, List<Integer>> seasonsByPlayerId = seasonPlayerRepository
                .findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(playerIds)
                .stream()
                .collect(groupingBy(
                        SeasonPlayer::getPlayerId,
                        mapping(SeasonPlayer::getSeason, toList())
                ));

        return players.stream()
                .map(player -> SeasonPlayerResponse.from(
                        player,
                        seasonsByPlayerId.getOrDefault(player.getPlayerId(), List.of())
                ))
                .toList();
    }
}
