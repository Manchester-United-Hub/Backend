package backend.manuhub.rank.dto;

public record TeamRankResponse(
        Integer rank,
        Long teamId,
        String teamName,
        String teamLogo,
        Integer points,
        Integer played,
        Integer win,
        Integer draw,
        Integer lose,
        Integer goalsFor,
        Integer goalsAgainst,
        Integer goalsDiff,
        String form
) {
    public static TeamRankResponse create(Integer rank, Long teamId, String teamName, String teamLogo,
                                          Integer points, Integer played, Integer win, Integer draw,
                                          Integer lose, Integer goalsFor, Integer goalsAgainst,
                                          Integer goalsDiff, String form) {
        return new TeamRankResponse(rank, teamId, teamName, teamLogo, points, played, win, draw,
                lose, goalsFor, goalsAgainst, goalsDiff, form);
    }
}
