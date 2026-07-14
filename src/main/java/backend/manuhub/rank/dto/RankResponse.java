package backend.manuhub.rank.dto;

public record RankResponse(
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
    public static RankResponse create(Integer rank, Long teamId, String teamName, String teamLogo,
                                      Integer points, Integer played, Integer win, Integer draw,
                                      Integer lose, Integer goalsFor, Integer goalsAgainst,
                                      Integer goalsDiff, String form) {
        return new RankResponse(rank, teamId, teamName, teamLogo, points, played, win, draw,
                lose, goalsFor, goalsAgainst, goalsDiff, form);
    }
}
