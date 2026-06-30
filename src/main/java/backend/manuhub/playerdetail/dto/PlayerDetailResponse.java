package backend.manuhub.playerdetail.dto;

import backend.manuhub.player.dto.PlayerResponse;
import backend.manuhub.playerdetail.PlayerDetail;

import java.util.List;

public record PlayerDetailResponse(
        PlayerResponse player,
        List<LeagueStatisticsResponse> statistics
) {
    public static PlayerDetailResponse of(PlayerResponse player, List<PlayerDetail> details) {
        return new PlayerDetailResponse(
                player,
                details.stream().map(LeagueStatisticsResponse::from).toList()
        );
    }

    public record LeagueStatisticsResponse(
            Long leagueId,
            String leagueName,
            Integer appearances,
            Integer lineups,
            Integer minutes,
            String rating,
            Boolean captain,
            Integer substitutesIn,
            Integer substitutesOut,
            Integer substitutesBench,
            Integer shotsTotal,
            Integer shotsOn,
            Integer goals,
            Integer assists,
            Integer dribblesAttempts,
            Integer dribblesSuccess,
            Integer dribblesPast,
            Integer penaltiesWon,
            Integer penaltiesScored,
            Integer penaltiesMissed,
            Integer passesTotal,
            Integer passesKey,
            String passesAccuracy,
            Integer tacklesTotal,
            Integer tacklesBlocks,
            Integer tacklesInterceptions,
            Integer duelsTotal,
            Integer duelsWon,
            Integer foulsDrawn,
            Integer foulsCommitted,
            Integer goalsConceded,
            Integer saves,
            Integer penaltiesSaved,
            Integer yellowCards,
            Integer yellowRedCards,
            Integer redCards
    ) {
        public static LeagueStatisticsResponse from(PlayerDetail detail) {
            return new LeagueStatisticsResponse(
                    detail.getLeagueId(), detail.getLeagueName(), detail.getAppearances(), detail.getLineups(),
                    detail.getMinutes(), detail.getRating(), detail.getCaptain(), detail.getSubstitutesIn(),
                    detail.getSubstitutesOut(), detail.getSubstitutesBench(), detail.getShotsTotal(), detail.getShotsOn(),
                    detail.getGoals(), detail.getAssists(), detail.getDribblesAttempts(), detail.getDribblesSuccess(),
                    detail.getDribblesPast(), detail.getPenaltiesWon(), detail.getPenaltiesScored(), detail.getPenaltiesMissed(),
                    detail.getPassesTotal(), detail.getPassesKey(), detail.getPassesAccuracy(), detail.getTacklesTotal(),
                    detail.getTacklesBlocks(), detail.getTacklesInterceptions(), detail.getDuelsTotal(), detail.getDuelsWon(),
                    detail.getFoulsDrawn(), detail.getFoulsCommitted(), detail.getGoalsConceded(), detail.getSaves(),
                    detail.getPenaltiesSaved(), detail.getYellowCards(), detail.getYellowRedCards(), detail.getRedCards()
            );
        }
    }
}
