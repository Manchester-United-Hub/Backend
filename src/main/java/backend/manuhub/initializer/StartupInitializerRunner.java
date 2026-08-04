package backend.manuhub.initializer;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.match.MatchInitializeService;
import backend.manuhub.player.PlayerInitializeService;
import backend.manuhub.teamstatistics.TeamStatisticsInitializeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class StartupInitializerRunner implements CommandLineRunner {

    private static final Long PREMIER_LEAGUE_ID = 39L;
    private static final Long MANCHESTER_UNITED_TEAM_ID = 33L;
    private static final int START_SEASON = 2020;
    private static final int SEASON_START_MONTH = 6;
    private static final int SEASON_START_DAY = 1;

    private final MatchInitializeService matchInitializeService;
    private final PlayerInitializeService playerInitializeService;
    private final TeamStatisticsInitializeService teamStatisticsInitializeService;

    @Override
    public void run(String... args) {
        initializeMatches();
        initializePlayers();
        initializeTeamStatistics();
    }

    private void initializeMatches() {
        initializeSeasons("Match",
                season -> saveMatches(PREMIER_LEAGUE_ID, season, MANCHESTER_UNITED_TEAM_ID));
    }

    private void initializePlayers() {
        initializeSeasons("Player", this::savePlayers);
    }

    private void initializeTeamStatistics() {
        initializeSeasons("TeamStatistics", this::saveTeamStatistics);
    }

    private void initializeSeasons(String targetName, Consumer<Integer> initializer) {
        int currentSeason = getCurrentSeason();
        log.info("[StartupInitializer] {} initialization started. seasons={}~{}",
                targetName, START_SEASON, currentSeason);

        for (int season = START_SEASON; season <= currentSeason; season++) {
            initializer.accept(season);
        }
    }

    private void saveMatches(Long league, Integer season, Long teamId) {
        try {
            matchInitializeService.saveMatches(league, season, teamId);
        } catch (ApiInvalidResponseException e) {
            log.error(">>> StartupInitializerRunner --> API-Football fixtures response is invalid. league={}, season={}, teamId={}",
                    league, season, teamId, e);
        }
    }

    private void savePlayers(Integer season) {
        try {
            playerInitializeService.savePlayers(season);
        } catch (ApiInvalidResponseException e) {
            log.error(">>> StartupInitializerRunner --> API-Football players response is invalid. season={}", season, e);
        }
    }

    private void saveTeamStatistics(Integer season) {
        try {
            teamStatisticsInitializeService.saveTeamStatistics(season);
        } catch (ApiInvalidResponseException e) {
            log.error(">>> StartupInitializerRunner --> API-Football team statistics response is invalid. season={}", season, e);
        }
    }

    private int getCurrentSeason() {
        LocalDate now = LocalDate.now();
        LocalDate seasonStartDate = LocalDate.of(now.getYear(), SEASON_START_MONTH, SEASON_START_DAY);

        if (now.isBefore(seasonStartDate)) {
            return now.getYear() - 1;
        }
        return now.getYear();
    }
}
