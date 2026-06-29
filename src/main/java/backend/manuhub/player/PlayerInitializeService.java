package backend.manuhub.player;

import backend.manuhub.exception.ApiServerException;
import backend.manuhub.external.player.PlayerClient;
import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.playerdetail.PlayerDetail;
import backend.manuhub.playerdetail.PlayerDetailMapper;
import backend.manuhub.playerdetail.PlayerDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerInitializeService {

    private final PlayerRepository playerRepository;
    private final PlayerDetailRepository playerDetailRepository;
    private final PlayerClient playerClient;

    @Retryable(
            maxAttemptsExpression = "${retry.player.max-attempts:11}",
            backoff = @Backoff(delayExpression = "${retry.player.delay:60000}", multiplier = 2),
            retryFor = ApiServerException.class
    )
    @Transactional
    public void savePlayers(Integer season) {
        if (playerRepository.existsBySeason(season)) {
            log.info("[Player] Players already exist. season={}", season);
            return;
        }

        List<PlayerApiResponse.Response> responses = playerClient.getManchesterUnitedPlayers(season);
        List<Player> savedPlayers = playerRepository.saveAll(PlayerMapper.toEntities(season, responses));
        Map<Long, Player> playersByPlayerId = savedPlayers.stream()
                .collect(toMap(Player::getPlayerId, identity()));
        List<PlayerDetail> playerDetails = PlayerDetailMapper.toEntities(season, responses, playersByPlayerId);
        playerDetailRepository.saveAll(playerDetails);

        log.info("[Player] Manchester United players and details saved. season={}, playerCount={}, detailCount={}",
                season, savedPlayers.size(), playerDetails.size());
    }

    @Recover
    public void recover(ApiServerException e, Integer season) {
        log.warn(">>> PlayerInitializeService --> API-Football 선수 초기화 재시도에 모두 실패했습니다. season={}, code={}",
                season, e.getErrorCode().getCode(), e);
    }
}
