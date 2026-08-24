package backend.manuhub.seasonplayer;

import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class SeasonPlayerPaginationRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SeasonPlayerRepository seasonPlayerRepository;

    @Test
    @DisplayName("전체 선수를 이름과 선수 ID 오름차순으로 정렬해 페이지 단위로 조회한다")
    void findsAllPlayersOrderedByNameAndPlayerId() {
        playerRepository.saveAll(List.of(
                player(40L, "Mason Mount"),
                player(30L, "Bruno Fernandes"),
                player(10L, "Amad Diallo"),
                player(20L, "Bruno Fernandes")
        ));

        Page<Player> firstPage = playerRepository.findAllByOrderByNameAscPlayerIdAsc(PageRequest.of(0, 2));
        Page<Player> secondPage = playerRepository.findAllByOrderByNameAscPlayerIdAsc(PageRequest.of(1, 2));

        assertEquals(List.of(10L, 20L), playerIds(firstPage));
        assertEquals(List.of(30L, 40L), playerIds(secondPage));
        assertEquals(4, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(true, firstPage.hasNext());
        assertEquals(false, secondPage.hasNext());
    }

    @Test
    @DisplayName("특정 시즌 선수만 이름 오름차순으로 정렬해 페이지 단위로 조회한다")
    void findsSeasonPlayersOrderedByPlayerName() {
        Player mount = player(40L, "Mason Mount");
        Player bruno = player(30L, "Bruno Fernandes");
        Player amad = player(10L, "Amad Diallo");
        Player casemiro = player(50L, "Casemiro");
        playerRepository.saveAll(List.of(mount, bruno, amad, casemiro));
        seasonPlayerRepository.saveAll(List.of(
                seasonPlayer(mount, 2025),
                seasonPlayer(bruno, 2025),
                seasonPlayer(amad, 2025),
                seasonPlayer(casemiro, 2024)
        ));

        Page<SeasonPlayer> firstPage = seasonPlayerRepository
                .findAllBySeasonWithPlayer(2025, PageRequest.of(0, 2));
        Page<SeasonPlayer> secondPage = seasonPlayerRepository
                .findAllBySeasonWithPlayer(2025, PageRequest.of(1, 2));

        assertEquals(List.of("Amad Diallo", "Bruno Fernandes"), playerNames(firstPage));
        assertEquals(List.of("Mason Mount"), playerNames(secondPage));
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(true, firstPage.hasNext());
        assertEquals(false, secondPage.hasNext());
    }

    private List<Long> playerIds(Page<Player> page) {
        return page.getContent().stream().map(Player::getPlayerId).toList();
    }

    private List<String> playerNames(Page<SeasonPlayer> page) {
        return page.getContent().stream().map(SeasonPlayer::getPlayer).map(Player::getName).toList();
    }

    private Player player(Long id, String name) {
        return Player.create(id, name, null, null, null, null, null);
    }

    private SeasonPlayer seasonPlayer(Player player, Integer season) {
        return SeasonPlayer.builder()
                .playerId(player.getPlayerId())
                .season(season)
                .player(player)
                .build();
    }
}
