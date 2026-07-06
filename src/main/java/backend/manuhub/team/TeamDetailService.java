package backend.manuhub.team;

import backend.manuhub.coach.Coach;
import backend.manuhub.coach.CoachService;
import backend.manuhub.team.dto.TeamGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class TeamDetailService {

    private final TeamService teamService;
    private final CoachService coachService;

    public TeamGetResponse getTeamWithCoachName(Long teamId) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<Team> teamFuture = CompletableFuture.supplyAsync(() -> getOrCreateTeam(teamId), executor);
            CompletableFuture<Coach> coachFuture = CompletableFuture.supplyAsync(() -> getOrCreateCoach(teamId), executor);

            return teamFuture.thenCombine(coachFuture,
                            (team, coach) -> TeamGetResponse.from(team, coach.getName()))
                    .join();
        }
    }

    private Team getOrCreateTeam(Long teamId) {
        return teamService.getTeamById(teamId)
                .orElseGet(() -> teamService.createTeam(teamId));
    }

    private Coach getOrCreateCoach(Long teamId) {
        return coachService.getCoachByTeamId(teamId)
                .orElseGet(() -> coachService.createCoach(teamId));
    }
}
