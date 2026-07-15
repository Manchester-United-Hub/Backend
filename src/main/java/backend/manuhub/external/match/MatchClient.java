package backend.manuhub.external.match;

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
public class MatchClient {

    private final RestClient restClient;

    public MatchClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<MatchApiResponse.Response> getMatches(Long league, Integer season, Long teamId) {
        log.info("[API-Football] Fixtures request. league={}, season={}, teamId={}", league, season, teamId);

        MatchApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("league", league)
                        .queryParam("season", season)
                        .queryParam("team", teamId)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (req, res) -> { throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR); })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (req, res) -> { throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR); })
                .body(MatchApiResponse.class);

        if (response == null || response.response() == null || response.response().isEmpty()) {
            log.warn(">>> MatchClient --> API-Football fixtures response is empty. league={}, season={}, teamId={}",
                    league, season, teamId);
            throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
        }

        log.info("[API-Football] Fixtures request succeeded. league={}, season={}, teamId={}, count={}",
                league, season, teamId, response.response().size());
        return response.response();
    }
}
