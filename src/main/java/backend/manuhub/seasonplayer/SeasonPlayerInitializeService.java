package backend.manuhub.seasonplayer;

import backend.manuhub.exception.ApiServerException;
import backend.manuhub.external.player.PlayerClient;
import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.player.PlayerImageService;
import backend.manuhub.player.PlayerImageTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonPlayerInitializeService {

    private final SeasonPlayerRepository seasonPlayerRepository;
    private final PlayerClient playerClient;
    private final SeasonPlayerPersistenceService seasonPlayerPersistenceService;
    private final PlayerImageService playerImageService;

    @Retryable(
            maxAttemptsExpression = "${retry.player.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.player.delay:1000}", multiplier = 2),
            retryFor = ApiServerException.class
    )
    public void saveSeasonPlayers(Integer season) {
        if (seasonPlayerRepository.existsBySeason(season)) {
            log.info("[SeasonPlayer] Season players already exist. season={}", season);
            return;
        }

        upsertSeasonPlayers(season);
    }

    @Retryable(
            maxAttemptsExpression = "${retry.player.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.player.delay:1000}", multiplier = 2),
            retryFor = ApiServerException.class
    )
    public void syncSeasonPlayers(Integer season) {
        log.info("[SeasonPlayer] Current season player synchronization started. season={}", season);
        upsertSeasonPlayers(season);
        log.info("[SeasonPlayer] Current season player synchronization completed. season={}", season);
    }

    private void upsertSeasonPlayers(Integer season) {
        List<PlayerApiResponse.Response> responses = playerClient.getManchesterUnitedPlayers(season);
        List<PlayerImageTarget> imageTargets = seasonPlayerPersistenceService.save(season, responses);
        playerImageService.uploadAndUpdate(imageTargets);
    }

    @Recover
    public void recover(ApiServerException e, Integer season) {
        log.warn(">>> SeasonPlayerInitializeService --> API-Football 선수 초기화 재시도에 모두 실패했습니다. season={}, code={}",
                season, e.getErrorCode().getCode(), e);
    }
}
