package backend.manuhub.playerdetail;

import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.player.Player;
import backend.manuhub.seasonplayer.SeasonPlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PlayerDetailMapperTest {

    @Test
    @DisplayName("API player statistics are mapped with the player, season, and league composite key")
    void mapsApiResponseToPlayerDetailWithCompositeKey() {
        Player player = Player.create(
                1485L, "Bruno Fernandes", "1994-09-08", "Portugal", "179 cm", "69 kg", "photo"
        );
        SeasonPlayer seasonPlayer = SeasonPlayer.builder()
                .playerId(1485L)
                .season(2025)
                .player(player)
                .number(8)
                .position("Midfielder")
                .build();
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
                List.of(statistics())
        );

        PlayerDetail detail = PlayerDetailMapper.toEntities(
                2025,
                List.of(response),
                Map.of(1485L, seasonPlayer)
        ).getFirst();

        assertEquals(1485L, detail.getPlayerId());
        assertEquals(2025, detail.getSeason());
        assertEquals(39L, detail.getLeagueId());
        assertEquals("Premier League", detail.getLeagueName());
        assertSame(seasonPlayer, detail.getSeasonPlayer());
        assertEquals(30, detail.getAppearances());
        assertEquals(10, detail.getGoals());
        assertEquals(8, detail.getAssists());
    }

    private PlayerApiResponse.Statistics statistics() {
        return new PlayerApiResponse.Statistics(
                new PlayerApiResponse.Team(33L, "Manchester United"),
                new PlayerApiResponse.League(39L, "Premier League", 2025),
                new PlayerApiResponse.Games(30, 28, 2500, 8, "Midfielder", "7.4", false),
                new PlayerApiResponse.Substitutes(2, 4, 3),
                new PlayerApiResponse.Shots(50, 20),
                new PlayerApiResponse.Goals(10, 0, 8, 0),
                new PlayerApiResponse.Passes(1000, 50, 85),
                new PlayerApiResponse.Tackles(40, 5, 20),
                new PlayerApiResponse.Duels(100, 60),
                new PlayerApiResponse.Dribbles(40, 25, 10),
                new PlayerApiResponse.Fouls(30, 10),
                new PlayerApiResponse.Cards(3, 0, 0),
                new PlayerApiResponse.Penalty(2, 1, 0, 0)
        );
    }
}
