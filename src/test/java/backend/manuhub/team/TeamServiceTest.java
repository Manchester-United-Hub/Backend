package backend.manuhub.team;

import backend.manuhub.external.team.TeamApiResponse;
import backend.manuhub.external.team.TeamClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@DisplayName("TeamService 테스트")
@ActiveProfiles("test")
@SpringBootTest
public class TeamServiceTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @MockitoBean
    private TeamClient teamClient;

    @AfterEach
    void clear() {
        teamRepository.deleteAll();
    }

    private TeamApiResponse.TeamResponse mockTeamResponse(Long id, String name, String logo, String city, String country, String stadium, Integer founded) {
        TeamApiResponse.TeamInfo teamInfo = new TeamApiResponse.TeamInfo(id, name, country, founded, logo);
        TeamApiResponse.VenueInfo venueInfo = new TeamApiResponse.VenueInfo(stadium, city);
        return new TeamApiResponse.TeamResponse(teamInfo, venueInfo);
    }

    @Test
    @DisplayName("팀을 생성한다")
    void createTeam() {
        given(teamClient.fetchTeam(33L)).willReturn(mockTeamResponse(33L, "Manchester United", "https://logo.png", "Manchester", "England", "Old Trafford", 1878));

        Team team = teamService.createTeam(33L);

        assertThat(team.getId()).isEqualTo(33L);
        assertThat(team.getName()).isEqualTo("Manchester United");
        assertThat(team.getStadium()).isEqualTo("Old Trafford");
        assertThat(team.getCity()).isEqualTo("Manchester");
        assertThat(team.getCountry()).isEqualTo("England");
        assertThat(team.getFounded()).isEqualTo(1878);
    }

    @Test
    @DisplayName("팀 ID로 팀을 조회한다")
    void getTeamById() {
        teamRepository.save(Team.create(33L, "Manchester United", "https://logo.png", "Manchester", "England", "Old Trafford", 1878));

        Optional<Team> result = teamService.getTeamById(33L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Manchester United");
    }

    @Test
    @DisplayName("존재하지 않는 팀 ID로 조회하면 빈 Optional을 반환한다")
    void getTeamByIdNotFound() {
        Optional<Team> result = teamService.getTeamById(33L);

        assertThat(result).isEmpty();
    }
}
