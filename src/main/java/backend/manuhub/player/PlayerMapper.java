package backend.manuhub.player;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.player.PlayerApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@Slf4j
public class PlayerMapper {

    public static List<Player> toEntities(Integer season, List<PlayerApiResponse.Response> responses) {
        try {
            return responses.stream()
                    .map(response -> toEntity(season, response))
                    .toList();
        } catch (NullPointerException | IndexOutOfBoundsException | NoSuchElementException e) {
            log.error(">>> PlayerMapper --> API-Football 선수 응답 구조가 올바르지 않습니다. season={}", season, e);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_PLAYER_INVALID_RESPONSE_ERROR);
        }
    }

    private static Player toEntity(Integer season, PlayerApiResponse.Response response) {
        PlayerApiResponse.Player player = response.player();
        PlayerApiResponse.Statistics statistics = response.statistics().getFirst();
        PlayerApiResponse.Games games = statistics.games();

        return Player.builder()
                .playerId(player.id())
                .season(season)
                .name(player.name())
                .birthDate(player.birth().date())
                .nationality(player.nationality())
                .height(player.height())
                .weight(player.weight())
                .number(games.number())
                .position(games.position())
                .photo(player.photo())
                .build();
    }
}
