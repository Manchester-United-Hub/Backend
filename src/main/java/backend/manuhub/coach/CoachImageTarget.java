package backend.manuhub.coach;

public record CoachImageTarget(
        Long coachId,
        String sourceUrl,
        String currentUrl
) {
}
