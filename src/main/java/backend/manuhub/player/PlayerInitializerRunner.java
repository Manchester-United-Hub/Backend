package backend.manuhub.player;

import backend.manuhub.exception.ApiInvalidResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class PlayerInitializerRunner implements CommandLineRunner {

    private static final List<Integer> TARGET_SEASONS = List.of(2023, 2024);

    private final PlayerInitializeService playerInitializeService;

    @Override
    public void run(String... args) {
        TARGET_SEASONS.forEach(this::savePlayers);
    }

    private void savePlayers(Integer season) {
        try {
            playerInitializeService.savePlayers(season);
        } catch (ApiInvalidResponseException e) {
            log.error(">>> PlayerInitializerRunner --> API-Football 선수 응답 구조가 올바르지 않습니다. season={}", season, e);
        }
    }
}
