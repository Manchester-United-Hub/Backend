package backend.manuhub.seasonplayer;

import backend.manuhub.seasonplayer.dto.SeasonPlayerListResponse;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeasonPlayerController.class)
class SeasonPlayerControllerTest {

    @MockitoBean
    private SeasonPlayerService seasonPlayerService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("season 파라미터로 해당 시즌 선수 목록을 조회한다")
    void getsPlayersBySeason() throws Exception {
        when(seasonPlayerService.getSeasonPlayers(2025, 0, 20)).thenReturn(listResponse());

        mockMvc.perform(get("/api/players").param("season", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].id").value(1485))
                .andExpect(jsonPath("$.players[0].number").value(8))
                .andExpect(jsonPath("$.players[0].position").value("Midfielder"))
                .andExpect(jsonPath("$.players[0].seasons[0]").value(2024))
                .andExpect(jsonPath("$.players[0].seasons[1]").value(2025))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(seasonPlayerService).getSeasonPlayers(2025, 0, 20);
    }

    @Test
    @DisplayName("season 파라미터가 없으면 전체 선수 목록을 조회한다")
    void getsAllPlayersWhenSeasonIsMissing() throws Exception {
        when(seasonPlayerService.getSeasonPlayers(null, 0, 20)).thenReturn(listResponse());

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].name").value("Bruno Fernandes"))
                .andExpect(jsonPath("$.players[0].seasons.length()").value(2));

        verify(seasonPlayerService).getSeasonPlayers(null, 0, 20);
    }

    @Test
    @DisplayName("요청한 페이지 번호와 크기를 서비스에 전달한다")
    void getsPlayersWithRequestedPageAndSize() throws Exception {
        SeasonPlayerListResponse response = new SeasonPlayerListResponse(
                List.of(response()), 2, 5, 13, 3, false
        );
        when(seasonPlayerService.getSeasonPlayers(2025, 2, 5)).thenReturn(response);

        mockMvc.perform(get("/api/players")
                        .param("season", "2025")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(13))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(seasonPlayerService).getSeasonPlayers(2025, 2, 5);
    }

    @ParameterizedTest
    @CsvSource({
            "page, -1",
            "size, 0",
            "size, 101"
    })
    @DisplayName("허용 범위를 벗어난 페이지 요청은 거절한다")
    void rejectsInvalidPagination(String parameter, String value) throws Exception {
        mockMvc.perform(get("/api/players").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_ERROR"));

        verifyNoInteractions(seasonPlayerService);
    }

    private SeasonPlayerListResponse listResponse() {
        return new SeasonPlayerListResponse(List.of(response()), 0, 20, 1, 1, false);
    }

    private SeasonPlayerResponse response() {
        return new SeasonPlayerResponse(
                1485L,
                "Bruno Fernandes",
                "1994-09-08",
                "Portugal",
                "179 cm",
                "69 kg",
                8,
                "Midfielder",
                "photo",
                List.of(2024, 2025)
        );
    }
}
