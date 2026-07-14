package backend.manuhub.external.rank;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RankClient {

    @Qualifier("apiFootballRestClient")
    private final RestClient restClient;

    public RankClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<RankApiResponse.RankInfo> fetchRank(int season) {

        RankApiResponse response = restClient.get()
                .uri("/standings?league=39&season=" + season)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                })
                .body(RankApiResponse.class);

        List<RankApiResponse.LeagueWrapper> responses = response.response();
        if (responses == null || responses.isEmpty()) {
            throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
        }

        return responses.get(0).league().standings().get(0);
    }
}
