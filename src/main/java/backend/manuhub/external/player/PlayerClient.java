package backend.manuhub.external.player;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
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

        PlayerApiResponse firstPage = getManchesterUnitedPlayers(season, 1);
        List<PlayerApiResponse.Response> players = new ArrayList<>(firstPage.response());
        int totalPages = firstPage.paging() == null || firstPage.paging().total() == null
                ? 1
                : firstPage.paging().total();

        for (int page = 2; page <= totalPages; page++) {
            PlayerApiResponse response = getManchesterUnitedPlayers(season, page);
            if (response.response() == null || response.response().isEmpty()) {
                log.warn(">>> PlayerClient --> API-Football player page is empty. stop paging. season={}, page={}, totalPages={}",
                        season, page, totalPages);
                break;
            }
            players.addAll(response.response());
        }

        log.info("[API-Football] Manchester United players request succeeded. season={}, pages={}, count={}",
                season, totalPages, players.size());
        return players;
    }

    private PlayerApiResponse getManchesterUnitedPlayers(Integer season, Integer page) {
        PlayerApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players")
                        .queryParam("team", MANCHESTER_UNITED_TEAM_ID)
                        .queryParam("season", season)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (req, res) -> { throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR); })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (req, res) -> { throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR); })
                .body(PlayerApiResponse.class);

        if (response == null || response.response() == null || (page == 1 && response.response().isEmpty())) {
            log.warn(">>> PlayerClient --> API-Football player response is empty. season={}, page={}", season, page);
            throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
        }

        log.info("[API-Football] Manchester United players page request succeeded. season={}, page={}, totalPages={}, count={}",
                season, page, response.paging() == null ? null : response.paging().total(),
                response.response() == null ? null : response.response().size());
        return response;
    }
}
