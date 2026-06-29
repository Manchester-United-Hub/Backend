package backend.manuhub.playerdetail;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.player.dto.PlayerResponse;
import backend.manuhub.playerdetail.dto.PlayerDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerDetailService {

    private final PlayerRepository playerRepository;
    private final PlayerDetailRepository playerDetailRepository;

    @Transactional(readOnly = true)
    public PlayerDetailResponse getPlayerDetail(Long playerId, Integer season) {
        Player player = playerRepository.findByPlayerIdAndSeason(playerId, season)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        List<PlayerDetail> details = playerDetailRepository.findAllByPlayerAndSeasonOrderByLeagueNameAsc(player, season);

        return PlayerDetailResponse.of(PlayerResponse.from(player), details);
    }
}
