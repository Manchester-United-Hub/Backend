package backend.manuhub.match.dto;

import backend.manuhub.match.Match;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record MatchResponse(
        Long matchId,
        LocalDateTime date,
        // TBD: 경기 시간 미정, NS: 경기 시작 전, 1H: 전반전, HT: 하프타임,
        // 2H: 후반전, ET: 연장전, BT: 연장전 휴식, P: 승부차기 진행 중,
        // SUSP: 일시 중단, INT: 중단, FT: 정규시간 종료, AET: 연장전 종료,
        // PEN: 승부차기 종료, PST: 연기, CANC: 취소, ABD: 포기·중단,
        // AWD: 행정 판정 완료, WO: 부전승, LIVE: 진행 중
        String status,
        RoundResponse round,
        VenueResponse venue,
        TeamResponse homeTeam,
        TeamResponse awayTeam,
        ScoreResponse score
) {
    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getMatchId(),
                match.getDate(),
                match.getStatus(),
                RoundResponse.from(match.getRound()),
                new VenueResponse(match.getVenueName(), match.getVenueCity()),
                new TeamResponse(
                        match.getHomeTeamId(),
                        match.getHomeTeamName(),
                        match.getHomeTeamLogo(),
                        match.getHomeTeamWinner()
                ),
                new TeamResponse(
                        match.getAwayTeamId(),
                        match.getAwayTeamName(),
                        match.getAwayTeamLogo(),
                        match.getAwayTeamWinner()
                ),
                ScoreResponse.from(match)
        );
    }

    public record RoundResponse(
            String name,
            Integer number
    ) {
        private static final Pattern NUMBERED_ROUND_PATTERN = Pattern.compile(" - (\\d+)$");

        private static RoundResponse from(String round) {
            if (round == null) {
                return null;
            }

            Matcher matcher = NUMBERED_ROUND_PATTERN.matcher(round);
            Integer number = matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
            return new RoundResponse(round, number);
        }
    }

    public record VenueResponse(
            String name,
            String city
    ) {
    }

    public record TeamResponse(
            Long teamId,
            String name,
            String logo,
            Boolean winner
    ) {
    }

    public record ScoreResponse(
            Integer home,
            Integer away
    ) {
        private static ScoreResponse from(Match match) {
            if (match.getHomeScore() == null && match.getAwayScore() == null) {
                return null;
            }

            return new ScoreResponse(match.getHomeScore(), match.getAwayScore());
        }
    }
}
