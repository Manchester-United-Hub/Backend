package backend.manuhub.external.player;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class PlayerClient {

    private static final Long MANCHESTER_UNITED_TEAM_ID = 33L;

    private final RestClient restClient;

    public PlayerClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<PlayerApiResponse.Response> getManchesterUnitedPlayers(Integer season) {
        log.info("[API-Football] Manchester United players request. season={}", season);

        PlayerApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players")
                        .queryParam("team", MANCHESTER_UNITED_TEAM_ID)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (req, res) -> { throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR); })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (req, res) -> { throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR); })
                .body(PlayerApiResponse.class);

        if (response == null || response.response() == null || response.response().isEmpty()) {
            log.warn(">>> PlayerClient --> API-Football player response is empty. season={}", season);
            throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
        }

        log.info("[API-Football] Manchester United players request succeeded. season={}, count={}",
                season, response.response().size());
        return response.response();
    }
}
