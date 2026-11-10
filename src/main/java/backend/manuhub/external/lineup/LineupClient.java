package backend.manuhub.external.lineup;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiInvalidResponseException;
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
public class LineupClient {

    private final RestClient restClient;

    public LineupClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<LineupApiResponse.Response> getLineups(Long matchId) {
        log.info("[API-Football] Fixture lineups request. matchId={}", matchId);

        LineupApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures/lineups")
                        .queryParam("fixture", matchId)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (req, res) -> { throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR); })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (req, res) -> { throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR); })
                .body(LineupApiResponse.class);

        if (response == null) {
            log.info("[API-Football] Fixture lineups are not available. matchId={}", matchId);
            return List.of();
        }

        if (response.response() == null) {
            log.warn(">>> LineupClient --> API-Football fixture lineups response is invalid. matchId={}", matchId);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_LINEUP_INVALID_RESPONSE_ERROR);
        }

        log.info("[API-Football] Fixture lineups request succeeded. matchId={}, count={}",
                matchId, response.response().size());
        return response.response();
    }
}
