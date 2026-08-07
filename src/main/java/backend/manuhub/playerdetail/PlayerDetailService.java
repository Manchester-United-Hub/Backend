package backend.manuhub.playerdetail;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.seasonplayer.SeasonPlayer;
import backend.manuhub.seasonplayer.SeasonPlayerId;
import backend.manuhub.seasonplayer.SeasonPlayerRepository;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
import backend.manuhub.playerdetail.dto.PlayerDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerDetailService {

    private static final Long PREMIER_LEAGUE_ID = 39L;

    private final SeasonPlayerRepository seasonPlayerRepository;
    private final PlayerDetailRepository playerDetailRepository;

    @Transactional(readOnly = true)
    public PlayerDetailResponse getPlayerDetail(Long playerId, Integer season) {
        SeasonPlayer seasonPlayer = seasonPlayerRepository.findById(new SeasonPlayerId(playerId, season))
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        List<PlayerDetail> details = playerDetailRepository.findAllBySeasonPlayerOrderByLeagueNameAsc(seasonPlayer)
                .stream()
                .filter(detail -> PREMIER_LEAGUE_ID.equals(detail.getLeagueId()))
                .toList();
        List<Integer> seasons = seasonPlayerRepository.findAllByPlayerIdOrderBySeasonAsc(playerId).stream()
                .map(SeasonPlayer::getSeason)
                .toList();

        return PlayerDetailResponse.of(SeasonPlayerResponse.from(seasonPlayer.getPlayer(), seasons), details);
    }
}
