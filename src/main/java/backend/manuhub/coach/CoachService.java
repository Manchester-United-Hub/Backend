package backend.manuhub.coach;

import backend.manuhub.coach.dto.CoachGetResponse;
import backend.manuhub.coach.dto.CoachResponse;
import backend.manuhub.external.coach.CoachApiResponse;
import backend.manuhub.external.coach.CoachClient;
import backend.manuhub.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachClient coachClient;
    private final CoachPersistenceService coachPersistenceService;
    private final CoachImageService coachImageService;
    private final TeamRepository teamRepository;

    public Coach createCoach(Long teamId) {
        return syncCoach(teamId);
    }

    @Transactional(readOnly = true)
    public Optional<Coach> getCoachByTeamId(Long teamId) {
        return coachRepository.findByTeamId(teamId);
    }

    @Transactional
    public CoachGetResponse getCoach(Long teamId) {
        Coach coach = coachRepository.findByTeamId(teamId)
                .orElseGet(() -> createCoach(teamId));
        return CoachGetResponse.from(coach);
    }

    public void updateCoach(Long teamId) {
        syncCoach(teamId);
    }

    public List<Long> getAllTeamIds() {
        return teamRepository.findAllTeamIds();
    }

    private CoachResponse fetchCoach(Long teamId) {
        CoachApiResponse.CoachResponse response = coachClient.fetchCurrentCoach(teamId);
        List<CoachApiResponse.CareerInfo> careerInfos = response.career() == null
                ? List.of()
                : response.career();

        CoachApiResponse.CareerInfo currentCareer = careerInfos.stream()
                .filter(career -> career.team() != null && teamId.equals(career.team().id()))
                .max(Comparator.comparing(
                        CoachApiResponse.CareerInfo::start,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .orElse(null);

        List<CoachCareer> careers = careerInfos.stream()
                .filter(career -> career.team() != null)
                .map(career -> CoachCareer.create(
                        career.start(),
                        career.end(),
                        career.team().name()
                ))
                .toList();

        return new CoachResponse(
                response.id(),
                teamId,
                response.name(),
                response.photo(),
                response.birth() == null ? null : response.birth().date(),
                response.birth() == null ? null : response.birth().place(),
                currentCareer == null ? null : currentCareer.start(),
                currentCareer == null ? null : currentCareer.end(),
                response.nationality(),
                careers
        );
    }

    private Coach syncCoach(Long teamId) {
        CoachSyncResult result = coachPersistenceService.upsert(fetchCoach(teamId));
        coachImageService.uploadAndUpdate(result.imageTarget());
        return result.coach();
    }
}
