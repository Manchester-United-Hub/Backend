package backend.manuhub.coach.dto;

public record CoachResponse(Long id, Long teamId, String name) {

    public static CoachResponse create(Long id, Long teamId, String name) {
        return new CoachResponse(id, teamId, name);
    }
}
