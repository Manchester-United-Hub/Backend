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

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamStatisticsClient {

    private final RestClient restClient;
    private final static Long teamId = 33L;

    public Optional<TeamStatisticsApiResponse> getTeamStatistics(Integer season) {

        log.info("[API-Football] 팀 통계 요청 season={}", season);

        Optional<TeamStatisticsApiResponse> result = Optional.ofNullable(
                        restClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path("/standings")
                                        .queryParam("league", 39)
                                        .queryParam("team", teamId)
                                        .queryParam("season", season)
                                        .build())
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                                    throw new ApiClientException(ErrorCode.API_FOOTBALL_CLIENT_ERROR);
                                })
                                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                                    throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
                                })
                                .body(TeamStatisticsApiResponse.class)
                )
                .filter(res -> !res.response().isEmpty());

        if (result.isEmpty()) {
            log.warn(">>> TeamStatisticsClient --> Exception. API response is empty. season={}", season);
            throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
        } else {
            log.info("[API-Football] 팀 통계 요청 성공 season={}", season);
        }

        return result;
    }
}
