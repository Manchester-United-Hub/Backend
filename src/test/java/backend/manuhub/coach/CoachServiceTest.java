package backend.manuhub.coach;

import backend.manuhub.coach.dto.CoachGetResponse;
import backend.manuhub.external.coach.CoachApiResponse;
import backend.manuhub.external.coach.CoachClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

    @MockitoBean
    private CoachImageService coachImageService;

    @AfterEach
    void clear() {
        coachRepository.deleteAll();
    }

    private CoachApiResponse.CoachResponse mockCoachResponse(Long id, Long teamId, String name) {
        CoachApiResponse.TeamInfo teamInfo = new CoachApiResponse.TeamInfo(teamId);
        return new CoachApiResponse.CoachResponse(id, name, teamInfo, List.of());
    }

    private CoachApiResponse.CoachResponse mockCoachResponse(
            Long id,
            Long teamId,
            String name,
            String birthDate,
            String birthPlace,
            String nationality,
            String photo,
            String appointmentDate,
            String contractEndDate,
            String careerTeam
    ) {
        CoachApiResponse.TeamInfo teamInfo = new CoachApiResponse.TeamInfo(teamId, careerTeam);
        CoachApiResponse.BirthInfo birthInfo = new CoachApiResponse.BirthInfo(birthDate, birthPlace);
        CoachApiResponse.CareerInfo careerInfo = new CoachApiResponse.CareerInfo(
                teamInfo,
                appointmentDate,
                contractEndDate
        );
        return new CoachApiResponse.CoachResponse(
                id,
                name,
                birthInfo,
                nationality,
                photo,
                teamInfo,
                List.of(careerInfo)
        );
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
    @DisplayName("팀 ID로 감독의 전체 정보를 조회한다")
    void getCoach() {
        coachRepository.save(Coach.create(
                1L,
                33L,
                "Michael Carrick",
                "https://cdn.manuhub.com/coaches/1.png",
                "1981-07-28",
                "Wallsend",
                "2025-11-24",
                "2028-06-30",
                "England",
                List.of(CoachCareer.create("2025-11-24", "2028-06-30", "Manchester United"))
        ));

        CoachGetResponse result = coachService.getCoach(33L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.teamId()).isEqualTo(33L);
        assertThat(result.name()).isEqualTo("Michael Carrick");
        assertThat(result.photo()).isEqualTo("https://cdn.manuhub.com/coaches/1.png");
        assertThat(result.birthDate()).isEqualTo("1981-07-28");
        assertThat(result.birthPlace()).isEqualTo("Wallsend");
        assertThat(result.appointmentDate()).isEqualTo("2025-11-24");
        assertThat(result.contractEndDate()).isEqualTo("2028-06-30");
        assertThat(result.nationality()).isEqualTo("England");
        assertThat(result.careers()).singleElement().satisfies(career -> {
            assertThat(career.startDate()).isEqualTo("2025-11-24");
            assertThat(career.endDate()).isEqualTo("2028-06-30");
            assertThat(career.team()).isEqualTo("Manchester United");
        });
        then(coachClient).shouldHaveNoInteractions();
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
    @DisplayName("API 프로필 값이 비어 있으면 기존 수동 입력값을 유지한다")
    void updateCoachWhenIdSame() {
        coachRepository.save(Coach.create(
                1L,
                33L,
                "E. Howe",
                "https://r2.dev/coaches/1.png",
                "1977-11-29",
                "Watford",
                "2025-07-01",
                "2028-06-30",
                "England",
                List.of(CoachCareer.create("2021-11-08", "2024-11-19", "Newcastle"))
        ));
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(1L, 33L, "E. Howe"));

        coachService.updateCoach(33L);

        Coach coach = coachRepository.findByTeamId(33L).orElseThrow();
        assertThat(coach.getId()).isEqualTo(1L);
        assertThat(coach.getBirthDate()).isEqualTo("1977-11-29");
        assertThat(coach.getBirthPlace()).isEqualTo("Watford");
        assertThat(coach.getAppointmentDate()).isEqualTo("2025-07-01");
        assertThat(coach.getContractEndDate()).isEqualTo("2028-06-30");
        assertThat(coach.getNationality()).isEqualTo("England");
    }

    @Test
    @DisplayName("기존 감독이 없으면 새 감독을 저장한다")
    void updateCoachWhenNotExists() {
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(1L, 33L, "E. Howe"));

        coachService.updateCoach(33L);

        Coach coach = coachRepository.findByTeamId(33L).orElseThrow();
        assertThat(coach.getId()).isEqualTo(1L);
        assertThat(coach.getName()).isEqualTo("E. Howe");
    }

    @Test
    @DisplayName("API 감독 정보 전체를 신규 저장하고 이미지 업로드 대상으로 전달한다")
    void syncNewCoachProfile() {
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(
                1L,
                33L,
                "Michael Carrick",
                "1981-07-28",
                "Wallsend",
                "England",
                "https://media.api-sports.io/football/coachs/1.png",
                "2025-11-24",
                "2028-06-30",
                "Manchester United"
        ));

        Coach coach = coachService.createCoach(33L);

        assertThat(coach.getName()).isEqualTo("Michael Carrick");
        assertThat(coach.getBirthDate()).isEqualTo("1981-07-28");
        assertThat(coach.getBirthPlace()).isEqualTo("Wallsend");
        assertThat(coach.getNationality()).isEqualTo("England");
        assertThat(coach.getAppointmentDate()).isEqualTo("2025-11-24");
        assertThat(coach.getContractEndDate()).isEqualTo("2028-06-30");
        assertThat(coach.getCareers()).singleElement().satisfies(career -> {
            assertThat(career.getStartDate()).isEqualTo("2025-11-24");
            assertThat(career.getEndDate()).isEqualTo("2028-06-30");
            assertThat(career.getTeam()).isEqualTo("Manchester United");
        });

        ArgumentCaptor<CoachImageTarget> imageTargetCaptor = ArgumentCaptor.forClass(CoachImageTarget.class);
        then(coachImageService).should().uploadAndUpdate(imageTargetCaptor.capture());
        assertThat(imageTargetCaptor.getValue().sourceUrl())
                .isEqualTo("https://media.api-sports.io/football/coachs/1.png");
    }

    @Test
    @Transactional
    @DisplayName("같은 감독의 API 값이 달라도 기존 수동 입력값을 유지한다")
    void preserveManuallyUpdatedProfile() {
        coachRepository.save(Coach.create(
                1L,
                33L,
                "수동 이름",
                "https://r2.dev/coaches/1.png",
                "수동 생년월일",
                "수동 출생지",
                "수동 부임일",
                "수동 계약 종료일",
                "수동 국적",
                List.of(CoachCareer.create("수동 시작일", "수동 종료일", "수동 팀"))
        ));
        given(coachClient.fetchCurrentCoach(33L)).willReturn(mockCoachResponse(
                1L,
                33L,
                "API 이름",
                "API 생년월일",
                "API 출생지",
                "API 국적",
                "https://media.api-sports.io/football/coachs/1.png",
                "API 부임일",
                "API 계약 종료일",
                "API 팀"
        ));

        coachService.updateCoach(33L);

        Coach coach = coachRepository.findByTeamId(33L).orElseThrow();
        assertThat(coach.getName()).isEqualTo("수동 이름");
        assertThat(coach.getBirthDate()).isEqualTo("수동 생년월일");
        assertThat(coach.getBirthPlace()).isEqualTo("수동 출생지");
        assertThat(coach.getAppointmentDate()).isEqualTo("수동 부임일");
        assertThat(coach.getContractEndDate()).isEqualTo("수동 계약 종료일");
        assertThat(coach.getNationality()).isEqualTo("수동 국적");
        assertThat(coach.getPhoto()).isEqualTo("https://r2.dev/coaches/1.png");
        assertThat(coach.getCareers()).singleElement().satisfies(career -> {
            assertThat(career.getStartDate()).isEqualTo("수동 시작일");
            assertThat(career.getEndDate()).isEqualTo("수동 종료일");
            assertThat(career.getTeam()).isEqualTo("수동 팀");
        });

        then(coachImageService).should().uploadAndUpdate(any(CoachImageTarget.class));
    }
}
