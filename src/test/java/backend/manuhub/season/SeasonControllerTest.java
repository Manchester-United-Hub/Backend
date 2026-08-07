package backend.manuhub.season;

import backend.manuhub.season.dto.SeasonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SeasonControllerTest {

    @Mock
    private SeasonService seasonService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new SeasonController(seasonService)).build();
    }

    @ParameterizedTest(name = "season={0}, started={1}")
    @MethodSource("seasonResponses")
    @DisplayName("현재 시즌과 시작 여부를 JSON으로 반환한다")
    void returnsCurrentSeasonResponse(Integer season, boolean started) throws Exception {
        when(seasonService.getCurrentSeason()).thenReturn(new SeasonResponse(season, started));

        mockMvc.perform(get("/api/seasons/current"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.season").value(season))
                .andExpect(jsonPath("$.started").value(started));
    }

    private static Stream<Arguments> seasonResponses() {
        return Stream.of(
                Arguments.of(2025, true),
                Arguments.of(2026, false),
                Arguments.of(2026, true)
        );
    }
}
