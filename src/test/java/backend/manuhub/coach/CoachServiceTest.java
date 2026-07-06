package backend.manuhub.coach;

import backend.manuhub.external.coach.CoachApiResponse;
import backend.manuhub.external.coach.CoachClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@DisplayName("CoachService 테스트")
@ActiveProfiles("test")
@SpringBootTest
public class CoachServiceTest {

    @Autowired
    private CoachService coachService;

    @Autowired
    private CoachRepository coachRepository;

    @MockitoBean
    private CoachClient coachClient;

    @AfterEach
    void clear() {
        coachRepository.deleteAll();
    }

    private CoachApiResponse.CoachResponse mockCoachResponse(Long id, Long teamId, String name) {
        CoachApiResponse.TeamInfo teamInfo = new CoachApiResponse.TeamInfo(teamId);
        return new CoachApiResponse.CoachResponse(id, name, teamInfo, List.of());
    }

    @Test
    @DisplayName("감독을 생성한다")
    void createCoach() {
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(1L, 33L, "E. Howe"));

        Coach coach = coachService.createCoach(33L);

        assertThat(coach.getId()).isEqualTo(1L);
        assertThat(coach.getName()).isEqualTo("E. Howe");
        assertThat(coach.getTeamId()).isEqualTo(33L);
    }

    @Test
    @DisplayName("팀 ID로 감독을 조회한다")
    void getCoachByTeamId() {
        coachRepository.save(Coach.create(1L, 33L, "E. Howe"));

        Optional<Coach> result = coachService.getCoachByTeamId(33L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("E. Howe");
    }

    @Test
    @DisplayName("존재하지 않는 팀 ID로 조회하면 빈 Optional을 반환한다")
    void getCoachByTeamIdNotFound() {
        Optional<Coach> result = coachService.getCoachByTeamId(33L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("기존 감독과 id가 다르면 삭제 후 새로 저장한다")
    void updateCoachWhenIdDifferent() {
        coachRepository.save(Coach.create(99999L, 33L, "Old Coach"));
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(1L, 33L, "E. Howe"));

        coachService.updateCoach(33L);

        Coach updated = coachRepository.findByTeamId(33L).orElseThrow();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getName()).isEqualTo("E. Howe");
    }

    @Test
    @DisplayName("기존 감독과 id가 같으면 업데이트하지 않는다")
    void updateCoachWhenIdSame() {
        coachRepository.save(Coach.create(1L, 33L, "E. Howe"));
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(1L, 33L, "E. Howe"));

        coachService.updateCoach(33L);

        Coach coach = coachRepository.findByTeamId(33L).orElseThrow();
        assertThat(coach.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("기존 감독이 없으면 업데이트하지 않는다")
    void updateCoachWhenNotExists() {
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(1L, 33L, "E. Howe"));

        coachService.updateCoach(33L);

        assertThat(coachRepository.findByTeamId(33L)).isEmpty();
    }
}
