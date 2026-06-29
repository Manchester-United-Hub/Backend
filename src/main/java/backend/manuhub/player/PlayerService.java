package backend.manuhub.player;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.dto.PlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public List<PlayerResponse> getPlayers(Integer season) {
        List<Player> players = playerRepository.findAllBySeasonOrderByNumberAscNameAsc(season);

        return players.stream()
                .map(PlayerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse getPlayer(Long playerId, Integer season) {
        return playerRepository.findByPlayerIdAndSeason(playerId, season)
                .map(PlayerResponse::from)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
    }
}
