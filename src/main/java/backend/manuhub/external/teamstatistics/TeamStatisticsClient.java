package backend.manuhub.external.teamstatistics;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamStatisticsClient {

    private final RestClient restClient;
    private final static Long teamId = 33L;

    public TeamStatisticsApiResponse getTeamStatistics(Integer season) {

        String uri = UriComponentsBuilder
                .fromPath("/standings")
                .queryParam("league", 39)
                .queryParam("team", teamId)
                .queryParam("season", season)
                .toUriString();

        log.info("[API-Football] 요청 시작 uri={}, season={}",
                uri, season);

        TeamStatisticsApiResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                })
                .body(TeamStatisticsApiResponse.class);

        if (response == null || response.getResponse().isEmpty()) {
            log.warn("[API-Football] 응답 없음 season={}",
                    season);
            return null;
        }

        log.info("[API-Football] 요청 성공 season={}",
                season);

        return response;
    }
}
