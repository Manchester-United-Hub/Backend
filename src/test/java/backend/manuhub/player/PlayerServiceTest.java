package backend.manuhub.player;

import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.dto.PlayerResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    @DisplayName("포지션 없이 시즌의 모든 선수를 조회한다")
    void getsPlayersBySeason() {
        Player player = mock(Player.class);
        when(player.getPlayerId()).thenReturn(1485L);
        when(player.getName()).thenReturn("Bruno Fernandes");
        when(playerRepository.findAllBySeasonOrderByNumberAscNameAsc(2025)).thenReturn(List.of(player));

        List<PlayerResponse> result = playerService.getPlayers(2025);

        assertEquals(1, result.size());
        assertEquals(1485L, result.getFirst().id());
        assertEquals("Bruno Fernandes", result.getFirst().name());
        verify(playerRepository).findAllBySeasonOrderByNumberAscNameAsc(2025);
    }

    @Test
    @DisplayName("선수 ID와 시즌으로 선수 한 명을 조회한다")
    void getsPlayerByPlayerIdAndSeason() {
        Player player = mock(Player.class);
        when(player.getPlayerId()).thenReturn(1485L);
        when(player.getName()).thenReturn("Bruno Fernandes");
        when(playerRepository.findByPlayerIdAndSeason(1485L, 2025)).thenReturn(Optional.of(player));

        PlayerResponse result = playerService.getPlayer(1485L, 2025);

        assertEquals(1485L, result.id());
        assertEquals("Bruno Fernandes", result.name());
        verify(playerRepository).findByPlayerIdAndSeason(1485L, 2025);
    }

    @Test
    @DisplayName("존재하지 않는 선수는 예외를 발생시킨다")
    void throwsExceptionWhenPlayerDoesNotExist() {
        when(playerRepository.findByPlayerIdAndSeason(1485L, 2025)).thenReturn(Optional.empty());

        assertThrows(ManuHubException.class, () -> playerService.getPlayer(1485L, 2025));
    }
}
