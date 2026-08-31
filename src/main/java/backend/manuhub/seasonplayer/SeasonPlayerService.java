package backend.manuhub.seasonplayer;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.seasonplayer.dto.SeasonPlayerListResponse;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public SeasonPlayerListResponse getSeasonPlayers(Integer season, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if (season == null) {
            Page<Player> playerPage = playerRepository.findAllByOrderByNameAscPlayerIdAsc(pageRequest);
            return SeasonPlayerListResponse.of(toResponses(playerPage.getContent()), playerPage);
        }

        Page<SeasonPlayer> seasonPlayerPage = seasonPlayerRepository.findAllBySeasonWithPlayer(season, pageRequest);
        return SeasonPlayerListResponse.of(toSeasonResponses(seasonPlayerPage.getContent()), seasonPlayerPage);
    }

    @Transactional(readOnly = true)
    public SeasonPlayerResponse getSeasonPlayer(Long playerId, Integer season) {
        if (season == null) {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
            List<SeasonPlayer> seasonPlayers = findSeasonPlayers(playerId);
            SeasonPlayer latestSeasonPlayer = seasonPlayers.isEmpty() ? null : seasonPlayers.getLast();
            return SeasonPlayerResponse.from(player, latestSeasonPlayer, toSeasons(seasonPlayers));
        }

        SeasonPlayer seasonPlayer = seasonPlayerRepository.findById(new SeasonPlayerId(playerId, season))
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        return SeasonPlayerResponse.from(seasonPlayer, toSeasons(findSeasonPlayers(playerId)));
    }

    private List<SeasonPlayerResponse> toResponses(List<Player> players) {
        if (players.isEmpty()) {
            return List.of();
        }

        Collection<Long> playerIds = players.stream()
                .map(Player::getPlayerId)
                .toList();
        Map<Long, List<SeasonPlayer>> seasonPlayersByPlayerId = seasonPlayerRepository
                .findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(playerIds)
                .stream()
                .collect(groupingBy(SeasonPlayer::getPlayerId));

        return players.stream()
                .map(player -> {
                    List<SeasonPlayer> seasonPlayers = seasonPlayersByPlayerId.getOrDefault(
                            player.getPlayerId(), List.of()
                    );
                    SeasonPlayer latestSeasonPlayer = seasonPlayers.isEmpty() ? null : seasonPlayers.getLast();
                    return SeasonPlayerResponse.from(
                            player,
                            latestSeasonPlayer,
                            toSeasons(seasonPlayers)
                    );
                })
                .toList();
    }

    private List<SeasonPlayerResponse> toSeasonResponses(List<SeasonPlayer> seasonPlayers) {
        if (seasonPlayers.isEmpty()) {
            return List.of();
        }

        Collection<Long> playerIds = seasonPlayers.stream()
                .map(SeasonPlayer::getPlayerId)
                .toList();
        Map<Long, List<Integer>> seasonsByPlayerId = seasonPlayerRepository
                .findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(playerIds)
                .stream()
                .collect(groupingBy(
                        SeasonPlayer::getPlayerId,
                        mapping(SeasonPlayer::getSeason, toList())
                ));

        return seasonPlayers.stream()
                .map(seasonPlayer -> SeasonPlayerResponse.from(
                        seasonPlayer,
                        seasonsByPlayerId.getOrDefault(seasonPlayer.getPlayerId(), List.of())
                ))
                .toList();
    }

    private List<SeasonPlayer> findSeasonPlayers(Long playerId) {
        return seasonPlayerRepository.findAllByPlayerIdOrderBySeasonAsc(playerId);
    }

    private List<Integer> toSeasons(List<SeasonPlayer> seasonPlayers) {
        return seasonPlayers.stream()
                .map(SeasonPlayer::getSeason)
                .toList();
    }
}
