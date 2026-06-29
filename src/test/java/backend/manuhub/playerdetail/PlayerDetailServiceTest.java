package backend.manuhub.playerdetail;

import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
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
    private PlayerRepository playerRepository;

    @Mock
    private PlayerDetailRepository playerDetailRepository;

    @InjectMocks
    private PlayerDetailService playerDetailService;

    @Test
    @DisplayName("선수 ID와 시즌으로 선수 한 명의 대회별 상세 기록을 조회한다")
    void getsPlayerDetailByPlayerIdAndSeason() {
        Player player = mock(Player.class);
        PlayerDetail detail = mock(PlayerDetail.class);
        when(player.getPlayerId()).thenReturn(1485L);
        when(player.getName()).thenReturn("Bruno Fernandes");
        when(detail.getLeagueId()).thenReturn(39L);
        when(detail.getLeagueName()).thenReturn("Premier League");
        when(detail.getAppearances()).thenReturn(30);
        when(playerRepository.findByPlayerIdAndSeason(1485L, 2025)).thenReturn(Optional.of(player));
        when(playerDetailRepository.findAllByPlayerAndSeasonOrderByLeagueNameAsc(player, 2025))
                .thenReturn(List.of(detail));

        PlayerDetailResponse result = playerDetailService.getPlayerDetail(1485L, 2025);

        assertEquals(1485L, result.player().id());
        assertEquals(1, result.statistics().size());
        assertEquals("Premier League", result.statistics().getFirst().leagueName());
        assertEquals(30, result.statistics().getFirst().appearances());
        verify(playerDetailRepository).findAllByPlayerAndSeasonOrderByLeagueNameAsc(player, 2025);
    }

    @Test
    @DisplayName("존재하지 않는 선수의 상세 기록을 조회하면 예외를 발생시킨다")
    void throwsExceptionWhenPlayerDoesNotExist() {
        when(playerRepository.findByPlayerIdAndSeason(1485L, 2025)).thenReturn(Optional.empty());

        assertThrows(ManuHubException.class, () -> playerDetailService.getPlayerDetail(1485L, 2025));
    }
}
