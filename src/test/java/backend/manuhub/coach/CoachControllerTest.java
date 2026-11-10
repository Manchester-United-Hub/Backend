package backend.manuhub.coach;

import backend.manuhub.coach.dto.CoachGetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CoachControllerTest {

    @Mock
    private CoachService coachService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new CoachController(coachService)).build();
    }

    @Test
    @DisplayName("팀 ID로 감독의 전체 정보를 JSON으로 반환한다")
    void getCoach() throws Exception {
        CoachGetResponse response = new CoachGetResponse(
                1L,
                33L,
                "Michael Carrick",
                "https://cdn.manuhub.com/coaches/1.png",
                "1981-07-28",
                "Wallsend",
                "2025-11-24",
                "2028-06-30",
                "England",
                List.of(new CoachGetResponse.CareerResponse(
                        "2025-11-24",
                        "2028-06-30",
                        "Manchester United"
                ))
        );
        when(coachService.getCoach(33L)).thenReturn(response);

        mockMvc.perform(get("/api/coaches/{teamId}", 33L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.teamId").value(33))
                .andExpect(jsonPath("$.name").value("Michael Carrick"))
                .andExpect(jsonPath("$.photo").value("https://cdn.manuhub.com/coaches/1.png"))
                .andExpect(jsonPath("$.birthDate").value("1981-07-28"))
                .andExpect(jsonPath("$.birthPlace").value("Wallsend"))
                .andExpect(jsonPath("$.appointmentDate").value("2025-11-24"))
                .andExpect(jsonPath("$.contractEndDate").value("2028-06-30"))
                .andExpect(jsonPath("$.nationality").value("England"))
                .andExpect(jsonPath("$.careers[0].startDate").value("2025-11-24"))
                .andExpect(jsonPath("$.careers[0].endDate").value("2028-06-30"))
                .andExpect(jsonPath("$.careers[0].team").value("Manchester United"));

        verify(coachService).getCoach(33L);
    }
}
