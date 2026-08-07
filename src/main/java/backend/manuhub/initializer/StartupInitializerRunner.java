package backend.manuhub.initializer;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.match.MatchInitializeService;
import backend.manuhub.season.SeasonInitializeService;
import backend.manuhub.seasonplayer.SeasonPlayerInitializeService;
import backend.manuhub.teamstatistics.TeamStatisticsInitializeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class StartupInitializerRunner implements CommandLineRunner {

    private static final Long PREMIER_LEAGUE_ID = 39L;
    private static final Long MANCHESTER_UNITED_TEAM_ID = 33L;
    private static final int SEASON_START_SEASON = 2025;
    private static final int MATCH_START_SEASON = 2025;
    private static final int PLAYER_START_SEASON = 2010;
    private static final int TEAM_STATISTICS_START_SEASON = 2010;
    private final SeasonInitializeService seasonInitializeService;
    private final MatchInitializeService matchInitializeService;
    private final SeasonPlayerInitializeService seasonPlayerInitializeService;
    private final TeamStatisticsInitializeService teamStatisticsInitializeService;

    @Override
    public void run(String... args) {
        int currentSeason = seasonInitializeService.saveSeasonsFrom(SEASON_START_SEASON);
        initializeMatches(currentSeason);
        initializeSeasonPlayers(currentSeason);
        initializeTeamStatistics(currentSeason);
    }

    private void initializeMatches(int currentSeason) {
        initializeSeasonRange("Match", MATCH_START_SEASON, currentSeason,
                season -> saveMatches(PREMIER_LEAGUE_ID, season, MANCHESTER_UNITED_TEAM_ID));
    }

    private void initializeSeasonPlayers(int currentSeason) {
        initializeSeasonRange("SeasonPlayer", PLAYER_START_SEASON, currentSeason, this::saveSeasonPlayers);
    }

    private void initializeTeamStatistics(int currentSeason) {
        initializeSeasonRange(
                "TeamStatistics", TEAM_STATISTICS_START_SEASON, currentSeason, this::saveTeamStatistics
        );
    }

    private void initializeSeasonRange(String targetName, int startSeason, int currentSeason,
                                       Consumer<Integer> initializer) {
        log.info("[StartupInitializer] {} initialization started. seasons={}~{}",
                targetName, startSeason, currentSeason);

        for (int season = startSeason; season <= currentSeason; season++) {
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

    private void saveSeasonPlayers(Integer season) {
        try {
            seasonPlayerInitializeService.saveSeasonPlayers(season);
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

}
