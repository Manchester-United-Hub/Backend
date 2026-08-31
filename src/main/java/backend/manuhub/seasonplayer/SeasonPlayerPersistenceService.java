package backend.manuhub.seasonplayer;

import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerImageTarget;
import backend.manuhub.player.PlayerMapper;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.playerdetail.PlayerDetail;
import backend.manuhub.playerdetail.PlayerDetailMapper;
import backend.manuhub.playerdetail.PlayerDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonPlayerPersistenceService {

    private final SeasonPlayerRepository seasonPlayerRepository;
    private final PlayerRepository playerRepository;
    private final PlayerDetailRepository playerDetailRepository;

    @Transactional
    public List<PlayerImageTarget> save(Integer season, List<PlayerApiResponse.Response> responses) {
        List<Player> receivedPlayers = PlayerMapper.toEntities(responses);
        Map<Long, Player> playersByPlayerId = playerRepository.findAllById(
                        receivedPlayers.stream().map(Player::getPlayerId).toList()
                ).stream()
                .collect(toMap(Player::getPlayerId, identity()));

        List<Player> newPlayers = new ArrayList<>();
        for (Player receivedPlayer : receivedPlayers) {
            Player savedPlayer = playersByPlayerId.get(receivedPlayer.getPlayerId());
            if (savedPlayer == null) {
                newPlayers.add(receivedPlayer);
                playersByPlayerId.put(receivedPlayer.getPlayerId(), receivedPlayer);
                continue;
            }

            savedPlayer.updateProfile(receivedPlayer);
        }
        if (!newPlayers.isEmpty()) {
            playerRepository.saveAll(newPlayers);
        }

        List<SeasonPlayer> savedSeasonPlayers = seasonPlayerRepository.saveAll(
                SeasonPlayerMapper.toEntities(season, responses, playersByPlayerId)
        );
        Map<Long, SeasonPlayer> seasonPlayersByPlayerId = savedSeasonPlayers.stream()
                .collect(toMap(SeasonPlayer::getPlayerId, identity()));
        List<PlayerDetail> playerDetails = PlayerDetailMapper.toEntities(season, responses, seasonPlayersByPlayerId);
        playerDetailRepository.saveAll(playerDetails);

        log.info("[SeasonPlayer] Manchester United season players and details saved. season={}, playerCount={}, detailCount={}",
                season, savedSeasonPlayers.size(), playerDetails.size());

        return responses.stream()
                .map(response -> new PlayerImageTarget(
                        response.player().id(),
                        response.player().photo(),
                        playersByPlayerId.get(response.player().id()).getPhoto()
                ))
                .toList();
    }
}
