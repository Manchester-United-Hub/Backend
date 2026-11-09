package backend.manuhub.external.matchevent;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MatchEventClient {

    @Qualifier("apiFootballRestClient")
    private final RestClient restClient;

    public MatchEventClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<MatchEventApiResponse.Event> fetchEvents(Long matchId) {
        MatchEventApiResponse response = restClient.get()
                .uri("/fixtures/events?fixture=" + matchId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                })
                .body(MatchEventApiResponse.class);

        List<MatchEventApiResponse.Event> events = response.response();
        return events == null ? List.of() : events;
    }
}
