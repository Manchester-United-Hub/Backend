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

    public static List<Player> toEntities(List<PlayerApiResponse.Response> responses) {
        try {
            return responses.stream()
                    .map(PlayerMapper::toEntity)
                    .toList();
        } catch (NullPointerException | NoSuchElementException e) {
            log.error(">>> PlayerMapper --> API-Football player response is invalid.", e);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_PLAYER_INVALID_RESPONSE_ERROR);
        }
    }

    private static Player toEntity(PlayerApiResponse.Response response) {
        PlayerApiResponse.Player player = response.player();
        return Player.create(
                player.id(),
                player.name(),
                player.birth().date(),
                player.nationality(),
                player.height(),
                player.weight(),
                player.photo()
        );
    }
}
