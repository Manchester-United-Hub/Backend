package backend.manuhub.seasonplayer;

import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SeasonPlayerControllerTest {

    @Mock
    private SeasonPlayerService seasonPlayerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new SeasonPlayerController(seasonPlayerService)).build();
    }

    @Test
    @DisplayName("season 파라미터로 해당 시즌 선수 목록을 조회한다")
    void getsPlayersBySeason() throws Exception {
        when(seasonPlayerService.getSeasonPlayers(2025)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/players").param("season", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1485))
                .andExpect(jsonPath("$[0].seasons[0]").value(2024))
                .andExpect(jsonPath("$[0].seasons[1]").value(2025));

        verify(seasonPlayerService).getSeasonPlayers(2025);
    }

    @Test
    @DisplayName("season 파라미터가 없으면 전체 선수 목록을 조회한다")
    void getsAllPlayersWhenSeasonIsMissing() throws Exception {
        when(seasonPlayerService.getSeasonPlayers(null)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bruno Fernandes"))
                .andExpect(jsonPath("$[0].seasons.length()").value(2));

        verify(seasonPlayerService).getSeasonPlayers(null);
    }

    private SeasonPlayerResponse response() {
        return new SeasonPlayerResponse(
                1485L,
                "Bruno Fernandes",
                "1994-09-08",
                "Portugal",
                "179 cm",
                "69 kg",
                "photo",
                List.of(2024, 2025)
        );
    }
}
