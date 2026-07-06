package backend.manuhub.external.coach;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Component
public class CoachClient {

    @Qualifier("apiFootballRestClient")
    private final RestClient restClient;

    public CoachClient(@Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public CoachApiResponse.CoachResponse fetchCurrentCoach(Long teamId) {

        CoachApiResponse response = restClient.get()
                .uri("/coachs?team=" + teamId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                })
                .body(CoachApiResponse.class);

        List<CoachApiResponse.CoachResponse> responses = response.response();
        if (responses == null || responses.isEmpty()) {
            throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
        }
        return responses.stream()
                .filter(coach -> coach.career().stream()
                        .anyMatch(c -> c.team().id().equals(teamId)))
                .max(Comparator.comparing(coach -> coach.career().stream()
                        .filter(c -> c.team().id().equals(teamId))
                        .map(CoachApiResponse.CareerInfo::start)
                        .findFirst()
                        .orElse("")))
                .orElseThrow(() -> new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR));
    }
}
