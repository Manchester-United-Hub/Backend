package backend.manuhub.rank.dto;

public record PlayerRankResponse(
        Integer rank,
        Long playerId,
        String playerName,
        String playerPhoto,
        Long teamId,
        String teamName,
        String teamLogo,
        Integer goals,
        Integer assists,
        Integer appearences,
        Integer minutes,
        Integer shots,
        Integer shotsOnTarget,
        Integer keyPasses
) {
    public static PlayerRankResponse create(Integer rank, Long playerId, String playerName, String playerPhoto,
                                            Long teamId, String teamName, String teamLogo,
                                            Integer goals, Integer assists, Integer appearences,
                                            Integer minutes, Integer shots, Integer shotsOnTarget,
                                            Integer keyPasses) {
        return new PlayerRankResponse(rank, playerId, playerName, playerPhoto, teamId, teamName, teamLogo,
                goals, assists, appearences, minutes, shots, shotsOnTarget, keyPasses);
    }
}
