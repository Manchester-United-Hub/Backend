package backend.manuhub.coach.dto;

import backend.manuhub.coach.Coach;
import backend.manuhub.coach.CoachCareer;

import java.util.List;

public record CoachGetResponse(
        Long id,
        Long teamId,
        String name,
        String photo,
        String birthDate,
        String birthPlace,
        String appointmentDate,
        String contractEndDate,
        String nationality,
        List<CareerResponse> careers
) {

    public static CoachGetResponse from(Coach coach) {
        return new CoachGetResponse(
                coach.getId(),
                coach.getTeamId(),
                coach.getName(),
                coach.getPhoto(),
                coach.getBirthDate(),
                coach.getBirthPlace(),
                coach.getAppointmentDate(),
                coach.getContractEndDate(),
                coach.getNationality(),
                coach.getCareers().stream()
                        .map(CareerResponse::from)
                        .toList()
        );
    }

    public record CareerResponse(
            String startDate,
            String endDate,
            String team
    ) {
        public static CareerResponse from(CoachCareer career) {
            return new CareerResponse(
                    career.getStartDate(),
                    career.getEndDate(),
                    career.getTeam()
            );
        }
    }
}
