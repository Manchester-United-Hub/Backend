package backend.manuhub.playerdetail;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.player.PlayerApiResponse;
import backend.manuhub.player.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@Slf4j
public class PlayerDetailMapper {

    public static List<PlayerDetail> toEntities(Integer season, List<PlayerApiResponse.Response> responses,
                                                 Map<Long, Player> playersByPlayerId) {
        try {
            return responses.stream()
                    .flatMap(response -> response.statistics().stream()
                            .map(statistics -> toEntity(season, response.player().id(), statistics, playersByPlayerId)))
                    .toList();
        } catch (NullPointerException | NoSuchElementException e) {
            log.error(">>> PlayerDetailMapper --> API-Football player detail response is invalid. season={}", season, e);
            throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_PLAYER_INVALID_RESPONSE_ERROR);
        }
    }

    private static PlayerDetail toEntity(Integer season, Long playerId, PlayerApiResponse.Statistics statistics,
                                         Map<Long, Player> playersByPlayerId) {
        Player player = playersByPlayerId.get(playerId);
        PlayerApiResponse.League league = statistics.league();
        PlayerApiResponse.Games games = statistics.games();
        PlayerApiResponse.Substitutes substitutes = statistics.substitutes();
        PlayerApiResponse.Shots shots = statistics.shots();
        PlayerApiResponse.Goals goals = statistics.goals();
        PlayerApiResponse.Passes passes = statistics.passes();
        PlayerApiResponse.Tackles tackles = statistics.tackles();
        PlayerApiResponse.Duels duels = statistics.duels();
        PlayerApiResponse.Dribbles dribbles = statistics.dribbles();
        PlayerApiResponse.Fouls fouls = statistics.fouls();
        PlayerApiResponse.Cards cards = statistics.cards();
        PlayerApiResponse.Penalty penalty = statistics.penalty();

        return PlayerDetail.builder()
                .player(player).season(season).leagueId(league.id()).leagueName(league.name())
                .appearances(games.appearences()).lineups(games.lineups()).minutes(games.minutes())
                .rating(games.rating()).captain(games.captain())
                .substitutesIn(substitutes.in()).substitutesOut(substitutes.out()).substitutesBench(substitutes.bench())
                .shotsTotal(shots.total()).shotsOn(shots.on()).goals(goals.total()).assists(goals.assists())
                .dribblesAttempts(dribbles.attempts()).dribblesSuccess(dribbles.success()).dribblesPast(dribbles.past())
                .penaltiesWon(penalty.won()).penaltiesScored(penalty.scored()).penaltiesMissed(penalty.missed())
                .passesTotal(passes.total()).passesKey(passes.key())
                .passesAccuracy(passes.accuracy() == null ? null : String.valueOf(passes.accuracy()))
                .tacklesTotal(tackles.total()).tacklesBlocks(tackles.blocks()).tacklesInterceptions(tackles.interceptions())
                .duelsTotal(duels.total()).duelsWon(duels.won()).foulsDrawn(fouls.drawn()).foulsCommitted(fouls.committed())
                .goalsConceded(goals.conceded()).saves(goals.saves()).penaltiesSaved(penalty.saved())
                .yellowCards(cards.yellow()).yellowRedCards(cards.yellowred()).redCards(cards.red())
                .build();
    }
}
