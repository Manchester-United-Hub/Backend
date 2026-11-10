package backend.manuhub.coach;

import backend.manuhub.coach.dto.CoachResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoachPersistenceService {

    private final CoachRepository coachRepository;

    @Transactional
    public CoachSyncResult upsert(CoachResponse response) {
        Coach receivedCoach = toEntity(response);
        Coach savedCoach = coachRepository.findByTeamId(response.teamId())
                .map(existing -> updateExisting(existing, receivedCoach))
                .orElseGet(() -> saveOrUpdateTransferredCoach(receivedCoach));

        return new CoachSyncResult(
                savedCoach,
                new CoachImageTarget(response.id(), response.photo(), savedCoach.getPhoto())
        );
    }

    private Coach updateExisting(Coach existing, Coach receivedCoach) {
        if (existing.getId().equals(receivedCoach.getId())) {
            existing.updateProfile(receivedCoach);
            return existing;
        }

        coachRepository.delete(existing);
        coachRepository.flush();
        return saveOrUpdateTransferredCoach(receivedCoach);
    }

    private Coach saveOrUpdateTransferredCoach(Coach receivedCoach) {
        return coachRepository.findById(receivedCoach.getId())
                .map(existing -> {
                    existing.updateProfile(receivedCoach);
                    return existing;
                })
                .orElseGet(() -> coachRepository.save(receivedCoach));
    }

    private Coach toEntity(CoachResponse response) {
        return Coach.create(
                response.id(),
                response.teamId(),
                response.name(),
                response.photo(),
                response.birthDate(),
                response.birthPlace(),
                response.appointmentDate(),
                response.contractEndDate(),
                response.nationality(),
                response.careers()
        );
    }
}
