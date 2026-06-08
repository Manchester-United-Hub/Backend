package backend.manuhub.external.team;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TeamClient {

    @Qualifier("apiFootballRestClient")
    private final RestClient restClient;

    public TeamClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public TeamApiResponse.TeamResponse fetchTeam(Long teamId) {

        TeamApiResponse response = restClient.get()
                .uri("/teams?id=" + teamId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                })
                .body(TeamApiResponse.class);

        List<TeamApiResponse.TeamResponse> responses = response.response();
        if (responses == null || responses.isEmpty()) {
            throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
        }
        return responses.get(0);
    }
}
