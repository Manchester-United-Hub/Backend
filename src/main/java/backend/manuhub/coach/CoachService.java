package backend.manuhub.coach;

import backend.manuhub.coach.dto.CoachResponse;
import backend.manuhub.external.coach.CoachApiResponse;
import backend.manuhub.external.coach.CoachClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachClient coachClient;

    @Transactional
    public Coach createCoach(Long teamId) {
        CoachResponse coach = fetchCoach(teamId);
        return saveCoach(coach);
    }

    public Optional<Coach> getCoachByTeamId(Long teamId) {
        return coachRepository.findByTeamId(teamId);
    }

    @Transactional
    public void updateCoach(Long teamId) {
        CoachResponse newCoach = fetchCoach(teamId);
        coachRepository.findByTeamId(teamId)
                .filter(existing -> !existing.getId().equals(newCoach.id()))
                .ifPresent(existing -> {
                    coachRepository.deleteByTeamId(teamId);
                    coachRepository.save(Coach.create(newCoach.id(), newCoach.teamId(), newCoach.name()));
                });
    }

    public List<Long> getAllTeamIds() {
        return coachRepository.findAllTeamIds();
    }

    private CoachResponse fetchCoach(Long teamId) {
        CoachApiResponse.CoachResponse response = coachClient.fetchCurrentCoach(teamId);
        return new CoachResponse(response.id(), response.team().id(), response.name());
    }

    private Coach saveCoach(CoachResponse c) {
        Coach coach = Coach.create(c.id(), c.teamId(), c.name());
        coachRepository.save(coach);
        return coach;
    }
}
