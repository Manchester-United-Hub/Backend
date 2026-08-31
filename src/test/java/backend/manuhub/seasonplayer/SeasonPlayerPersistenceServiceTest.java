package backend.manuhub.seasonplayer;

import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerImageTarget;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.playerdetail.PlayerDetailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonPlayerPersistenceServiceTest {

    @Mock
    private SeasonPlayerRepository seasonPlayerRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerDetailRepository playerDetailRepository;

    @InjectMocks
    private SeasonPlayerPersistenceService seasonPlayerPersistenceService;

    @Test
    @DisplayName("신규 선수와 시즌 선수 및 상세 정보를 먼저 DB에 저장한다")
    void savesNewPlayerSeasonPlayerAndDetails() {
        PlayerApiResponse.Response response = response("Bruno Fernandes", "api-photo");
        when(playerRepository.findAllById(List.of(1485L))).thenReturn(List.of());
        when(playerRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(seasonPlayerRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<PlayerImageTarget> targets = seasonPlayerPersistenceService.save(2023, List.of(response));

        verify(playerRepository).saveAll(argThat(players -> {
            var iterator = players.iterator();
            if (!iterator.hasNext()) {
                return false;
            }
            Player player = iterator.next();
            return !iterator.hasNext()
                    && player.getPlayerId().equals(1485L)
                    && player.getPhoto().equals("api-photo");
        }));
        verify(seasonPlayerRepository).saveAll(argThat(players -> {
            var iterator = players.iterator();
            if (!iterator.hasNext()) {
                return false;
            }
            SeasonPlayer player = iterator.next();
            return !iterator.hasNext()
                    && player.getPlayerId().equals(1485L)
                    && player.getSeason().equals(2023)
                    && player.getNumber().equals(8)
                    && player.getPosition().equals("Midfielder");
        }));
        verify(playerDetailRepository).saveAll(argThat(details -> details.iterator().hasNext()));
        assertEquals(List.of(new PlayerImageTarget(1485L, "api-photo", "api-photo")), targets);
    }

    @Test
    @DisplayName("기존 선수 프로필을 갱신할 때 저장된 R2 사진 URL을 유지한다")
    void preservesExistingR2PhotoWhenUpdatingPlayerProfile() {
        Player savedPlayer = Player.create(1485L, "Old Name", "1994-09-08",
                "Portugal", "178 cm", "68 kg", "https://r2.dev/players/1485.png");
        PlayerApiResponse.Response response = response("Bruno Fernandes", "api-photo");
        when(playerRepository.findAllById(List.of(1485L))).thenReturn(List.of(savedPlayer));
        when(seasonPlayerRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<PlayerImageTarget> targets = seasonPlayerPersistenceService.save(2023, List.of(response));

        verify(playerRepository, never()).saveAll(anyList());
        assertEquals("Bruno Fernandes", savedPlayer.getName());
        assertEquals("179 cm", savedPlayer.getHeight());
        assertEquals("https://r2.dev/players/1485.png", savedPlayer.getPhoto());
        assertEquals(
                List.of(new PlayerImageTarget(1485L, "api-photo", "https://r2.dev/players/1485.png")),
                targets
        );
    }

    private PlayerApiResponse.Response response(String name, String photo) {
        return new PlayerApiResponse.Response(
                new PlayerApiResponse.Player(
                        1485L,
                        name,
                        new PlayerApiResponse.Birth("1994-09-08"),
                        "Portugal",
                        "179 cm",
                        "69 kg",
                        photo
                ),
                List.of(new PlayerApiResponse.Statistics(
                        new PlayerApiResponse.Team(33L, "Manchester United"),
                        new PlayerApiResponse.League(39L, "Premier League", 2023),
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
                ))
        );
    }
}
