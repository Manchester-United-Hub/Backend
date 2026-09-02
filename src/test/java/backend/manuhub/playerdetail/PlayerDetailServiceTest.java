package backend.manuhub.playerdetail;

import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.Player;
import backend.manuhub.seasonplayer.SeasonPlayer;
import backend.manuhub.seasonplayer.SeasonPlayerId;
import backend.manuhub.seasonplayer.SeasonPlayerRepository;
import backend.manuhub.playerdetail.dto.PlayerDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PlayerDetailServiceTest {

    @Mock
    private SeasonPlayerRepository seasonPlayerRepository;

    @Mock
    private PlayerDetailRepository playerDetailRepository;

    @InjectMocks
    private PlayerDetailService playerDetailService;

    @Test
    @DisplayName("선수 ID와 시즌으로 선수 한 명의 대회별 상세 기록을 조회한다")
    void getsPlayerDetailByPlayerIdAndSeason() {
        SeasonPlayer seasonPlayer = mock(SeasonPlayer.class);
        SeasonPlayer latestSeasonPlayer = mock(SeasonPlayer.class);
        Player player = mock(Player.class);
        PlayerDetail premierLeagueDetail = mock(PlayerDetail.class);
        PlayerDetail faCupDetail = mock(PlayerDetail.class);
        when(seasonPlayer.getPlayer()).thenReturn(player);
        when(seasonPlayer.getSeason()).thenReturn(2024);
        when(latestSeasonPlayer.getSeason()).thenReturn(2025);
        when(latestSeasonPlayer.getNumber()).thenReturn(8);
        when(latestSeasonPlayer.getPosition()).thenReturn("Midfielder");
        when(player.getPlayerId()).thenReturn(1485L);
        when(player.getName()).thenReturn("Bruno Fernandes");
        when(premierLeagueDetail.getLeagueId()).thenReturn(39L);
        when(premierLeagueDetail.getLeagueName()).thenReturn("Premier League");
        when(premierLeagueDetail.getAppearances()).thenReturn(30);
        when(faCupDetail.getLeagueId()).thenReturn(45L);
        when(seasonPlayerRepository.findById(new SeasonPlayerId(1485L, 2024))).thenReturn(Optional.of(seasonPlayer));
        when(seasonPlayerRepository.findAllByPlayerIdOrderBySeasonAsc(1485L))
                .thenReturn(List.of(seasonPlayer, latestSeasonPlayer));
        when(playerDetailRepository.findAllBySeasonPlayerOrderByLeagueNameAsc(seasonPlayer))
                .thenReturn(List.of(faCupDetail, premierLeagueDetail));

        PlayerDetailResponse result = playerDetailService.getPlayerDetail(1485L, 2024);

        assertEquals(1485L, result.player().id());
        assertEquals(8, result.player().number());
        assertEquals("Midfielder", result.player().position());
        assertEquals(List.of(2024, 2025), result.player().seasons());
        assertEquals(1, result.statistics().size());
        assertEquals("Premier League", result.statistics().getFirst().leagueName());
        assertEquals(30, result.statistics().getFirst().appearances());
        verify(playerDetailRepository).findAllBySeasonPlayerOrderByLeagueNameAsc(seasonPlayer);
    }

    @Test
    @DisplayName("존재하지 않는 선수의 상세 기록을 조회하면 예외를 발생시킨다")
    void throwsExceptionWhenPlayerDoesNotExist() {
        when(seasonPlayerRepository.findById(new SeasonPlayerId(1485L, 2025))).thenReturn(Optional.empty());

        assertThrows(ManuHubException.class, () -> playerDetailService.getPlayerDetail(1485L, 2025));
    }
}
