package backend.manuhub.team;

import backend.manuhub.coach.Coach;
import backend.manuhub.coach.CoachRepository;
import backend.manuhub.external.coach.CoachApiResponse;
import backend.manuhub.external.coach.CoachClient;
import backend.manuhub.external.team.TeamApiResponse;
import backend.manuhub.external.team.TeamClient;
import backend.manuhub.team.dto.TeamGetResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("TeamDetailService 테스트")
@ActiveProfiles("test")
@SpringBootTest
public class TeamDetailServiceTest {

    @Autowired
    private TeamDetailService teamDetailService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CoachRepository coachRepository;

    @MockitoBean
    private TeamClient teamClient;

    @MockitoBean
    private CoachClient coachClient;

    @AfterEach
    void clear() {
        teamRepository.deleteAll();
        coachRepository.deleteAll();
    }

    private TeamApiResponse.TeamResponse mockTeamResponse() {
        TeamApiResponse.TeamInfo teamInfo = new TeamApiResponse.TeamInfo(33L, "Manchester United", "England", 1878, "https://logo.png");
        TeamApiResponse.VenueInfo venueInfo = new TeamApiResponse.VenueInfo("Old Trafford", "Manchester");
        return new TeamApiResponse.TeamResponse(teamInfo, venueInfo);
    }

    private CoachApiResponse.CoachResponse mockCoachResponse() {
        CoachApiResponse.TeamInfo teamInfo = new CoachApiResponse.TeamInfo(33L);
        return new CoachApiResponse.CoachResponse(1L, "Michael Carrick", teamInfo, List.of());
    }

    @Test
    @DisplayName("팀과 감독이 DB에 있으면 API 호출 없이 반환한다")
    void getTeamWithCoachNameFromDB() {
        teamRepository.save(Team.create(33L, "Manchester United", "https://logo.png", "Manchester", "England", "Old Trafford", 1878));
        coachRepository.save(Coach.create(1L, 33L, "Michael Carrick"));

        TeamGetResponse result = teamDetailService.getTeamWithCoachName(33L);

        assertThat(result.id()).isEqualTo(33L);
        assertThat(result.name()).isEqualTo("Manchester United");
        assertThat(result.coachName()).isEqualTo("Michael Carrick");

        then(teamClient).shouldHaveNoInteractions();
        then(coachClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("팀이 없으면 API를 호출해서 생성 후 반환한다")
    void getTeamWithCoachNameCreateTeam() {
        coachRepository.save(Coach.create(1L, 33L, "Michael Carrick"));
        given(teamClient.fetchTeam(33L)).willReturn(mockTeamResponse());

        TeamGetResponse result = teamDetailService.getTeamWithCoachName(33L);

        assertThat(result.id()).isEqualTo(33L);
        assertThat(result.name()).isEqualTo("Manchester United");
        then(teamClient).should().fetchTeam(33L);
    }

    @Test
    @DisplayName("감독이 없으면 API를 호출해서 생성 후 반환한다")
    void getTeamWithCoachNameCreateCoach() {
        teamRepository.save(Team.create(33L, "Manchester United", "https://logo.png", "Manchester", "England", "Old Trafford", 1878));
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse());

        TeamGetResponse result = teamDetailService.getTeamWithCoachName(33L);

        assertThat(result.coachName()).isEqualTo("Michael Carrick");
        then(coachClient).should().fetchCurrentCoach(33L);
    }

    @Test
    @DisplayName("팀과 감독 모두 없으면 API를 호출해서 생성 후 반환한다")
    void getTeamWithCoachNameCreateBoth() {
        given(teamClient.fetchTeam(33L)).willReturn(mockTeamResponse());
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse());

        TeamGetResponse result = teamDetailService.getTeamWithCoachName(33L);

        assertThat(result.id()).isEqualTo(33L);
        assertThat(result.coachName()).isEqualTo("Michael Carrick");
        then(teamClient).should().fetchTeam(33L);
        then(coachClient).should().fetchCurrentCoach(33L);
    }
}
