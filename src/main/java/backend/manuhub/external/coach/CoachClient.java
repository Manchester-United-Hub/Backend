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
                .filter(coach -> coach.career() != null)
                .filter(coach -> coach.career().stream()
                        .anyMatch(career -> career.team() != null && teamId.equals(career.team().id())))
                .max(Comparator.comparing(coach -> latestStartForTeam(coach, teamId)))
                .orElseThrow(() -> new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR));
    }

    private String latestStartForTeam(CoachApiResponse.CoachResponse coach, Long teamId) {
        return coach.career().stream()
                .filter(career -> career.team() != null && teamId.equals(career.team().id()))
                .map(CoachApiResponse.CareerInfo::start)
                .filter(start -> start != null)
                .max(String::compareTo)
                .orElse("");
    }
}
