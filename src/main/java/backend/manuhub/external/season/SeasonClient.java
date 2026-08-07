package backend.manuhub.external.season;

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
public class SeasonClient {

    private static final Long PREMIER_LEAGUE_ID = 39L;

    private final RestClient restClient;

    public SeasonClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SeasonApiResponse.SeasonPeriod> getPremierLeagueSeasons() {
        log.info("[API-Football] Premier League seasons request. league={}", PREMIER_LEAGUE_ID);

        SeasonApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/leagues")
                        .queryParam("id", PREMIER_LEAGUE_ID)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (req, res) -> { throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR); })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (req, res) -> { throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR); })
                .body(SeasonApiResponse.class);

        if (response == null || response.response() == null || response.response().isEmpty()
                || response.response().getFirst().seasons() == null
                || response.response().getFirst().seasons().isEmpty()) {
            log.warn(">>> SeasonClient --> API-Football season response is empty. league={}", PREMIER_LEAGUE_ID);
            throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
        }

        List<SeasonApiResponse.SeasonPeriod> seasons = response.response().getFirst().seasons();
        log.info("[API-Football] Premier League seasons request succeeded. league={}, count={}",
                PREMIER_LEAGUE_ID, seasons.size());
        return seasons;
    }
}
