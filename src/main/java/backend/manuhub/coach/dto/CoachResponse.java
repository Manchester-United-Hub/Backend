package backend.manuhub.coach.dto;

import backend.manuhub.coach.CoachCareer;

import java.util.List;

public record CoachResponse(
        Long id,
        Long teamId,
        String name,
        String photo,
        String birthDate,
        String birthPlace,
        String appointmentDate,
        String contractEndDate,
        String nationality,
        List<CoachCareer> careers
) {

    public CoachResponse(Long id, Long teamId, String name) {
        this(id, teamId, name, null, null, null, null, null, null, List.of());
    }
}
