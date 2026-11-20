package backend.manuhub.team;

import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.external.team.TeamApiResponse;
import backend.manuhub.external.team.TeamClient;
import backend.manuhub.image.ImageService;
import backend.manuhub.team.dto.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    private final TeamRepository teamRepository;
    private final TeamClient teamClient;
    private final ImageService imageService;

    // 외부 API와 R2 업로드가 DB 트랜잭션을 오래 점유하지 않도록 저장 호출만 Repository 트랜잭션으로 실행한다.
    public Team createTeam(Long teamId) {
        TeamResponse response = fetchTeamByTeamId(teamId);
        return saveTeam(response);
    }

    @Transactional(readOnly = true)
    public Optional<Team> getTeamById(Long teamId) {
        return teamRepository.findById(teamId);
    }

    public Team syncLineupTeam(LineupApiResponse.Team lineupTeam) {
        return teamRepository.findById(lineupTeam.id())
                .map(team -> updateLineupLogo(team, lineupTeam.logo()))
                .orElseGet(() -> saveLineupTeam(lineupTeam));
    }

    private TeamResponse fetchTeamByTeamId(Long teamId) {
        TeamApiResponse.TeamResponse t = teamClient.fetchTeam(teamId);
        return TeamResponse.create(t.team().id(), t.team().name(), t.team().logo(), t.venue().city(), t.team().country(), t.venue().name(), t.team().founded());
    }

    private Team saveTeam(TeamResponse t) {
        String r2LogoUrl = imageService.uploadFromUrl(t.logoUrl(), "teams/" + t.id() + ".png");
        return teamRepository.save(Team.create(t.id(), t.name(), r2LogoUrl, t.city(), t.country(), t.stadium(), t.founded()));
    }

    private Team saveLineupTeam(LineupApiResponse.Team team) {
        String logoUrl = imageService.uploadFromUrl(team.logo(), "teams/" + team.id() + ".png");
        return teamRepository.save(Team.create(team.id(), team.name(), logoUrl, null, null, null, null));
    }

    private Team updateLineupLogo(Team team, String sourceLogoUrl) {
        if (team.getLogoUrl() != null && team.getLogoUrl().startsWith(publicUrl + "/")) {
            return team;
        }
        if (sourceLogoUrl == null || sourceLogoUrl.isBlank()) {
            return team;
        }

        String r2LogoUrl = imageService.uploadFromUrl(sourceLogoUrl, "teams/" + team.getId() + ".png");
        team.updateLogoUrl(r2LogoUrl);
        return teamRepository.save(team);
    }
}
