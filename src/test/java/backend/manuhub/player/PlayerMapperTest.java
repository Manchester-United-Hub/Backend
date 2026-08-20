package backend.manuhub.player;

import backend.manuhub.external.player.PlayerApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerMapperTest {

    @Test
    @DisplayName("API 선수 응답을 시즌과 무관한 Player 기본정보로 변환한다")
    void mapsApiResponseToPlayer() {
        PlayerApiResponse.Response response = new PlayerApiResponse.Response(
                new PlayerApiResponse.Player(
                        1485L,
                        "Bruno Fernandes",
                        new PlayerApiResponse.Birth("1994-09-08"),
                        "Portugal",
                        "179 cm",
                        "69 kg",
                        "photo"
                ),
                List.of()
        );

        Player player = PlayerMapper.toEntities(List.of(response)).getFirst();

        assertEquals(1485L, player.getPlayerId());
        assertEquals("Bruno Fernandes", player.getName());
        assertEquals("1994-09-08", player.getBirthDate());
        assertEquals("photo", player.getPhoto());
    }
}
