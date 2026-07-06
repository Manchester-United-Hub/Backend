package backend.manuhub.team;

import backend.manuhub.external.team.TeamApiResponse;
import backend.manuhub.external.team.TeamClient;
import backend.manuhub.team.dto.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamClient teamClient;

    @Transactional
    public Team createTeam(Long teamId) {
        TeamResponse response = fetchTeamByTeamId(teamId);
        return saveTeam(response);
    }

    public Optional<Team> getTeamById(Long teamId) {
        return teamRepository.findById(teamId);
    }

    private TeamResponse fetchTeamByTeamId(Long teamId) {
        TeamApiResponse.TeamResponse t = teamClient.fetchTeam(teamId);
        return TeamResponse.create(t.team().id(), t.team().name(), t.team().logo(), t.venue().city(), t.team().country(), t.venue().name(), t.team().founded());
    }

    private Team saveTeam(TeamResponse t) {
        return teamRepository.save(Team.create(t.id(), t.name(), t.logoUrl(), t.city(), t.country(), t.stadium(), t.founded()));
    }
}
