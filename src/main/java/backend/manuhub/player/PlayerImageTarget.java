package backend.manuhub.player;

public record PlayerImageTarget(
        Long playerId,
        String sourceUrl,
        String currentUrl
) {
}
