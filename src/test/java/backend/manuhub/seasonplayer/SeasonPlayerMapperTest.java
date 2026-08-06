package backend.manuhub.seasonplayer;

import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonPlayerMapperTest {

    @Test
    void usesPremierLeagueNumberWhenFirstStatisticsNumberIsNull() {
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
                List.of(
                        statistics(45L, "FA Cup", null),
                        statistics(39L, "Premier League", 8)
                )
        );

        Player player = Player.create(
                1485L, "Bruno Fernandes", "1994-09-08", "Portugal", "179 cm", "69 kg", "photo"
        );

        SeasonPlayer seasonPlayer = SeasonPlayerMapper
                .toEntities(2025, List.of(response), Map.of(1485L, player))
                .getFirst();

        assertEquals(1485L, seasonPlayer.getPlayerId());
        assertEquals(2025, seasonPlayer.getSeason());
        assertEquals(player, seasonPlayer.getPlayer());
        assertEquals(8, seasonPlayer.getNumber());
    }

    private PlayerApiResponse.Statistics statistics(Long leagueId, String leagueName, Integer number) {
        return new PlayerApiResponse.Statistics(
                new PlayerApiResponse.Team(33L, "Manchester United"),
                new PlayerApiResponse.League(leagueId, leagueName, 2025),
                new PlayerApiResponse.Games(1, 1, 90, number, "Midfielder", "7.0", false),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
