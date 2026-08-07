package backend.manuhub.seasonplayer;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.player.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@Slf4j
public class SeasonPlayerMapper {

    private static final Long PREMIER_LEAGUE_ID = 39L;

    public static List<SeasonPlayer> toEntities(Integer season, List<PlayerApiResponse.Response> responses,
                                                Map<Long, Player> playersByPlayerId) {
        try {
            return responses.stream()
                    .map(response -> toEntity(season, response, playersByPlayerId))
                    .toList();
        } catch (NullPointerException | IndexOutOfBoundsException | NoSuchElementException e) {
            log.error(">>> SeasonPlayerMapper --> API-Football 선수 응답 구조가 올바르지 않습니다. season={}", season, e);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_PLAYER_INVALID_RESPONSE_ERROR);
        }
    }

    private static SeasonPlayer toEntity(Integer season, PlayerApiResponse.Response response,
                                         Map<Long, Player> playersByPlayerId) {
        PlayerApiResponse.Player apiPlayer = response.player();
        Player player = playersByPlayerId.get(apiPlayer.id());
        PlayerApiResponse.Statistics premierLeagueStatistics = response.statistics().stream()
                .filter(statistics -> statistics.league() != null
                        && PREMIER_LEAGUE_ID.equals(statistics.league().id()))
                .findFirst()
                .orElse(null);
        PlayerApiResponse.Games games = premierLeagueStatistics == null
                ? null
                : premierLeagueStatistics.games();

        return SeasonPlayer.builder()
                .playerId(apiPlayer.id())
                .season(season)
                .player(player)
                .number(games == null ? null : games.number())
                .position(games == null ? null : games.position())
                .build();
    }
}
