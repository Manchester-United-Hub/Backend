package backend.manuhub.seasonplayer;

import backend.manuhub.exception.ManuHubException;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import backend.manuhub.seasonplayer.dto.SeasonPlayerListResponse;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeasonPlayerServiceTest {

    @Mock
    private SeasonPlayerRepository seasonPlayerRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private SeasonPlayerService seasonPlayerService;

    @Test
    @DisplayName("season으로 필터링하면 해당 시즌에 뛴 선수와 전체 활동 시즌을 반환한다")
    void getsPlayersBySeason() {
        Player player = player(1485L, "Bruno Fernandes");
        SeasonPlayer season2024 = seasonPlayer(player, 2024, 8);
        SeasonPlayer season2025 = seasonPlayer(player, 2025, 8);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(seasonPlayerRepository.search(2025, null, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(season2025), pageRequest, 1));
        when(seasonPlayerRepository.findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(List.of(1485L)))
                .thenReturn(List.of(season2024, season2025));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(2025, null, null, 0, 20);

        assertEquals(1, result.players().size());
        assertEquals(1485L, result.players().getFirst().id());
        assertEquals("Bruno Fernandes", result.players().getFirst().name());
        assertEquals(8, result.players().getFirst().number());
        assertEquals("Midfielder", result.players().getFirst().position());
        assertEquals(List.of(2024, 2025), result.players().getFirst().seasons());
        assertEquals(1, result.totalElements());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalPages());
        assertEquals(false, result.hasNext());
    }

    @Test
    @DisplayName("position으로 필터링하면 해당 포지션 선수만 반환한다")
    void getsPlayersByPosition() {
        Player player = player(1485L, "Bruno Fernandes");
        SeasonPlayer seasonPlayer = seasonPlayer(player, 2025, 8);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(seasonPlayerRepository.search(null, "Midfielder", null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(seasonPlayer), pageRequest, 1));
        when(seasonPlayerRepository.findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(List.of(1485L)))
                .thenReturn(List.of(seasonPlayer));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(null, "Midfielder", null, 0, 20);

        assertEquals(1, result.players().size());
        assertEquals("Midfielder", result.players().getFirst().position());
        verify(seasonPlayerRepository).search(null, "Midfielder", null, pageRequest);
    }

    @Test
    @DisplayName("name으로 검색하면 이름에 해당 문자열이 포함된 선수만 반환한다")
    void getsPlayersByName() {
        Player player = player(1485L, "Bruno Fernandes");
        SeasonPlayer seasonPlayer = seasonPlayer(player, 2025, 8);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(seasonPlayerRepository.search(null, null, "Bruno", pageRequest))
                .thenReturn(new PageImpl<>(List.of(seasonPlayer), pageRequest, 1));
        when(seasonPlayerRepository.findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(List.of(1485L)))
                .thenReturn(List.of(seasonPlayer));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(null, null, "Bruno", 0, 20);

        assertEquals(1, result.players().size());
        assertEquals("Bruno Fernandes", result.players().getFirst().name());
        verify(seasonPlayerRepository).search(null, null, "Bruno", pageRequest);
    }

    @Test
    @DisplayName("season, position, name 파라미터가 모두 없으면 전체 선수를 반환한다")
    void getsAllPlayersWhenNoFilter() {
        Player bruno = player(1485L, "Bruno Fernandes");
        Player mount = player(152982L, "Mason Mount");
        PageRequest pageRequest = PageRequest.of(0, 20);
        SeasonPlayer brunoSp2024 = seasonPlayer(bruno, 2024, 18);
        SeasonPlayer brunoSp2025 = seasonPlayer(bruno, 2025, 8);
        SeasonPlayer mountSp2025 = seasonPlayer(mount, 2025, 7);

        when(seasonPlayerRepository.search(null, null, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(brunoSp2025, mountSp2025), pageRequest, 2));
        when(seasonPlayerRepository.findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(List.of(1485L, 152982L)))
                .thenReturn(List.of(brunoSp2024, brunoSp2025, mountSp2025));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(null, null, null, 0, 20);

        assertEquals(2, result.players().size());
        assertEquals(8, result.players().getFirst().number());
        assertEquals(List.of(2024, 2025), result.players().getFirst().seasons());
        assertEquals(7, result.players().get(1).number());
        assertEquals(List.of(2025), result.players().get(1).seasons());
    }

    @Test
    @DisplayName("조회된 페이지가 비어 있으면 활동 시즌을 추가 조회하지 않는다")
    void doesNotLoadPlayedSeasonsForEmptyPage() {
        PageRequest pageRequest = PageRequest.of(3, 20);
        when(seasonPlayerRepository.search(null, null, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 10));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(null, null, null, 3, 20);

        assertEquals(List.of(), result.players());
        assertEquals(3, result.page());
        assertEquals(10, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(false, result.hasNext());
        verify(seasonPlayerRepository, never()).findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(any());
    }

    @Test
    @DisplayName("선수 ID와 시즌으로 선수 한 명과 전체 활동 시즌을 반환한다")
    void getsPlayerByPlayerIdAndSeason() {
        Player player = player(1485L, "Bruno Fernandes");
        SeasonPlayer season2024 = seasonPlayer(player, 2024, 8);
        SeasonPlayer season2025 = seasonPlayer(player, 2025, 8);
        SeasonPlayerId id = new SeasonPlayerId(1485L, 2025);
        when(seasonPlayerRepository.findById(id)).thenReturn(Optional.of(season2025));
        when(seasonPlayerRepository.findAllByPlayerIdOrderBySeasonAsc(1485L))
                .thenReturn(List.of(season2024, season2025));

        SeasonPlayerResponse result = seasonPlayerService.getSeasonPlayer(1485L, 2025);

        assertEquals(1485L, result.id());
        assertEquals(8, result.number());
        assertEquals("Midfielder", result.position());
        assertEquals(List.of(2024, 2025), result.seasons());
        verify(seasonPlayerRepository).findById(id);
    }

    @Test
    @DisplayName("season이 없으면 선수의 가장 최신 시즌 등번호와 포지션을 반환한다")
    void getsPlayerWithLatestSeasonDataWhenSeasonIsNull() {
        Player player = player(1485L, "Bruno Fernandes");
        when(playerRepository.findById(1485L)).thenReturn(Optional.of(player));
        when(seasonPlayerRepository.findAllByPlayerIdOrderBySeasonAsc(1485L))
                .thenReturn(List.of(
                        seasonPlayer(player, 2024, 18),
                        seasonPlayer(player, 2025, 8)
                ));

        SeasonPlayerResponse result = seasonPlayerService.getSeasonPlayer(1485L, null);

        assertEquals(8, result.number());
        assertEquals("Midfielder", result.position());
        assertEquals(List.of(2024, 2025), result.seasons());
    }

    @Test
    @DisplayName("해당 시즌의 선수가 없으면 예외를 발생시킨다")
    void throwsExceptionWhenPlayerDoesNotExist() {
        when(seasonPlayerRepository.findById(new SeasonPlayerId(1485L, 2025)))
                .thenReturn(Optional.empty());

        assertThrows(ManuHubException.class,
                () -> seasonPlayerService.getSeasonPlayer(1485L, 2025));
    }

    private Player player(Long playerId, String name) {
        return Player.create(playerId, name, "1994-09-08", "Portugal", "179 cm", "69 kg", "photo");
    }

    private SeasonPlayer seasonPlayer(Player player, Integer season, Integer number) {
        return SeasonPlayer.builder()
                .playerId(player.getPlayerId())
                .season(season)
                .player(player)
                .number(number)
                .position("Midfielder")
                .build();
    }
}
