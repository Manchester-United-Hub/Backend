package backend.manuhub.coach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("CoachScheduler 테스트")
@ActiveProfiles("test")
@SpringBootTest
public class CoachSchedulerTest {

    @Autowired
    private CoachScheduler coachScheduler;

    @MockitoBean
    private CoachService coachService;

    @Test
    @DisplayName("모든 팀의 감독을 업데이트한다")
    void updateCoach() {
        given(coachService.getAllTeamIds()).willReturn(List.of(33L, 34L, 35L));

        coachScheduler.updateCoach();

        then(coachService).should().updateCoach(33L);
        then(coachService).should().updateCoach(34L);
        then(coachService).should().updateCoach(35L);
    }

    @Test
    @DisplayName("팀이 없으면 업데이트하지 않는다")
    void updateCoachWhenNoTeams() {
        given(coachService.getAllTeamIds()).willReturn(List.of());

        coachScheduler.updateCoach();

        then(coachService).should(never()).updateCoach(any());
    }
}
