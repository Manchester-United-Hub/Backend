package backend.manuhub.seasonplayer;

import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.external.player.PlayerClient;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.playerdetail.PlayerDetailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SeasonPlayerInitializeServiceTest {

    @Mock
    private SeasonPlayerRepository seasonPlayerRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerClient playerClient;

    @Mock
    private PlayerDetailRepository playerDetailRepository;

    @InjectMocks
    private SeasonPlayerInitializeService seasonPlayerInitializeService;

    @Test
    @DisplayName("선수 데이터가 없는 시즌은 맨체스터 유나이티드 선수 전체를 저장한다")
    void savesManchesterUnitedPlayersForSeason() {
        PlayerApiResponse.Response response = new PlayerApiResponse.Response(
                new PlayerApiResponse.Player(1485L, "Bruno Fernandes", new PlayerApiResponse.Birth("1994-09-08"),
                        "Portugal", "179 cm", "69 kg", "photo"),
                List.of(statistics())
        );
        when(seasonPlayerRepository.existsBySeason(2023)).thenReturn(false);
        when(playerClient.getManchesterUnitedPlayers(2023)).thenReturn(List.of(response));
        when(playerRepository.saveAll(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(seasonPlayerRepository.saveAll(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        seasonPlayerInitializeService.saveSeasonPlayers(2023);

        verify(seasonPlayerRepository).saveAll(argThat(players -> {
            var iterator = players.iterator();
            if (!iterator.hasNext()) {
                return false;
            }

            SeasonPlayer savedPlayer = iterator.next();
            return !iterator.hasNext()
                    && savedPlayer.getPlayerId().equals(1485L)
                    && savedPlayer.getSeason().equals(2023)
                    && savedPlayer.getPlayer().getName().equals("Bruno Fernandes");
        }));
        verify(playerRepository).saveAll(argThat(players -> {
            var iterator = players.iterator();
            return iterator.hasNext()
                    && iterator.next().getPlayerId().equals(1485L)
                    && !iterator.hasNext();
        }));
        verify(playerDetailRepository).saveAll(argThat(details -> {
            var iterator = details.iterator();
            if (!iterator.hasNext()) {
                return false;
            }

            var detail = iterator.next();
            return detail.getPlayerId().equals(1485L)
                    && detail.getSeason().equals(2023)
                    && detail.getLeagueId().equals(39L)
                    && detail.getLeagueName().equals("Premier League")
                    && !iterator.hasNext();
        }));
    }

    @Test
    @DisplayName("이미 선수가 저장된 시즌은 외부 API 호출과 저장을 건너뛴다")
    void skipsSeasonWhenPlayersAlreadyExist() {
        when(seasonPlayerRepository.existsBySeason(2024)).thenReturn(true);

        seasonPlayerInitializeService.saveSeasonPlayers(2024);

        verify(playerClient, never()).getManchesterUnitedPlayers(2024);
        verify(playerRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(seasonPlayerRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(playerDetailRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    private PlayerApiResponse.Statistics statistics() {
        return new PlayerApiResponse.Statistics(
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
        );
    }
}
