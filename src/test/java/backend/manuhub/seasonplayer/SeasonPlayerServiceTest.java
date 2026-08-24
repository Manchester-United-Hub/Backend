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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonPlayerServiceTest {

    @Mock
    private SeasonPlayerRepository seasonPlayerRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private SeasonPlayerService seasonPlayerService;

    @Test
    @DisplayName("season이 있으면 해당 시즌에 뛴 선수와 전체 활동 시즌을 반환한다")
    void getsPlayersBySeasonWithAllPlayedSeasons() {
        Player player = player(1485L, "Bruno Fernandes");
        SeasonPlayer season2024 = seasonPlayer(player, 2024, 8);
        SeasonPlayer season2025 = seasonPlayer(player, 2025, 8);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(seasonPlayerRepository.findAllBySeasonWithPlayer(2025, pageRequest))
                .thenReturn(new PageImpl<>(List.of(season2025), pageRequest, 1));
        when(seasonPlayerRepository.findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(List.of(1485L)))
                .thenReturn(List.of(season2024, season2025));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(2025, 0, 20);

        assertEquals(1, result.players().size());
        assertEquals(1485L, result.players().getFirst().id());
        assertEquals("Bruno Fernandes", result.players().getFirst().name());
        assertEquals(List.of(2024, 2025), result.players().getFirst().seasons());
        assertEquals(1, result.totalElements());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalPages());
        assertEquals(false, result.hasNext());
        verify(playerRepository, never()).findAllByOrderByNameAscPlayerIdAsc(pageRequest);
    }

    @Test
    @DisplayName("season이 없으면 전체 선수와 각 선수의 전체 활동 시즌을 반환한다")
    void getsAllPlayersWithAllPlayedSeasonsWhenSeasonIsNull() {
        Player bruno = player(1485L, "Bruno Fernandes");
        Player mount = player(152982L, "Mason Mount");
        List<Long> playerIds = List.of(1485L, 152982L);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(playerRepository.findAllByOrderByNameAscPlayerIdAsc(pageRequest))
                .thenReturn(new PageImpl<>(List.of(bruno, mount), pageRequest, 2));
        when(seasonPlayerRepository.findAllByPlayerIdInOrderByPlayerIdAscSeasonAsc(playerIds))
                .thenReturn(List.of(
                        seasonPlayer(bruno, 2024, 8),
                        seasonPlayer(bruno, 2025, 8),
                        seasonPlayer(mount, 2025, 7)
                ));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(null, 0, 20);

        assertEquals(2, result.players().size());
        assertEquals(List.of(2024, 2025), result.players().getFirst().seasons());
        assertEquals(List.of(2025), result.players().get(1).seasons());
        verify(seasonPlayerRepository, never()).findAllBySeasonWithPlayer(2025, pageRequest);
    }

    @Test
    @DisplayName("조회된 페이지가 비어 있으면 활동 시즌을 추가 조회하지 않는다")
    void doesNotLoadPlayedSeasonsForEmptyPage() {
        PageRequest pageRequest = PageRequest.of(3, 20);
        when(playerRepository.findAllByOrderByNameAscPlayerIdAsc(pageRequest))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 10));

        SeasonPlayerListResponse result = seasonPlayerService.getSeasonPlayers(null, 3, 20);

        assertEquals(List.of(), result.players());
        assertEquals(3, result.page());
        assertEquals(10, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(false, result.hasNext());
        verifyNoInteractions(seasonPlayerRepository);
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
        assertEquals(List.of(2024, 2025), result.seasons());
        verify(seasonPlayerRepository).findById(id);
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
