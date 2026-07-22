package backend.manuhub.external.rank;

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

    public List<TeamRankApiResponse.RankInfo> fetchRank(int season) {

        TeamRankApiResponse response = restClient.get()
                .uri("/standings?league=39&season=" + season)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                })
                .body(TeamRankApiResponse.class);

        List<TeamRankApiResponse.LeagueWrapper> responses = response.response();
        if (responses == null || responses.isEmpty()) {
            throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
        }

        return responses.get(0).league().standings().get(0);
    }

    public List<PlayerRankApiResponse.PlayerRankInfo> fetchTopScorers(int season) {
        return fetchPlayerRank("/players/topscorers?league=39&season=" + season);
    }

    public List<PlayerRankApiResponse.PlayerRankInfo> fetchTopAssists(int season) {
        return fetchPlayerRank("/players/topassists?league=39&season=" + season);
    }

    private List<PlayerRankApiResponse.PlayerRankInfo> fetchPlayerRank(String uri) {
        PlayerRankApiResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                })
                .body(PlayerRankApiResponse.class);

        List<PlayerRankApiResponse.PlayerRankInfo> responses = response.response();
        return responses == null ? List.of() : responses;
    }
}
