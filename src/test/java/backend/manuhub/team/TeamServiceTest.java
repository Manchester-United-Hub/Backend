package backend.manuhub.team;

import backend.manuhub.external.team.TeamApiResponse;
import backend.manuhub.external.team.TeamClient;
import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.image.ImageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;

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

    @MockitoBean
    private ImageService imageService;

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
        given(imageService.uploadFromUrl("https://logo.png", "teams/33.png"))
                .willReturn("https://r2.test/teams/33.png");

        Team team = teamService.createTeam(33L);

        assertThat(team.getId()).isEqualTo(33L);
        assertThat(team.getName()).isEqualTo("Manchester United");
        assertThat(team.getStadium()).isEqualTo("Old Trafford");
        assertThat(team.getCity()).isEqualTo("Manchester");
        assertThat(team.getCountry()).isEqualTo("England");
        assertThat(team.getFounded()).isEqualTo(1878);
        assertThat(team.getLogoUrl()).isEqualTo("https://r2.test/teams/33.png");
    }

    @Test
    @DisplayName("라인업 팀 로고 업로드는 DB 트랜잭션 밖에서 실행하고 결과를 저장한다")
    void syncLineupTeamUploadsOutsideTransaction() {
        teamRepository.save(Team.create(
                33L, "Manchester United", "https://source.test/old.png",
                null, null, null, null
        ));
        AtomicBoolean transactionActiveDuringUpload = new AtomicBoolean(true);
        given(imageService.uploadFromUrl(anyString(), anyString())).willAnswer(invocation -> {
            transactionActiveDuringUpload.set(TransactionSynchronizationManager.isActualTransactionActive());
            return "https://test/teams/33.png";
        });

        Team result = teamService.syncLineupTeam(
                new LineupApiResponse.Team(33L, "Manchester United", "https://source.test/new.png")
        );

        assertThat(transactionActiveDuringUpload).isFalse();
        assertThat(result.getLogoUrl()).isEqualTo("https://test/teams/33.png");
        assertThat(teamRepository.findById(33L).orElseThrow().getLogoUrl())
                .isEqualTo("https://test/teams/33.png");
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
