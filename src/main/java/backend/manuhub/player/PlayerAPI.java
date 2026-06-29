package backend.manuhub.player;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.player.dto.PlayerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "선수 API")
public interface PlayerAPI {

    @Operation(
            summary = "선수 목록 조회 API",
            description = "시즌별 선수 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "season", description = "시즌 연도", required = true,
                            schema = @Schema(type = "integer", example = "2025"))
            },
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "선수 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlayerResponse.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "name": "Bruno Fernandes",
                                        "birthDate": "1994-09-08",
                                        "nationality": "Portugal",
                                        "height": "179 cm",
                                        "weight": "69 kg",
                                        "number": 8,
                                        "position": "Midfielder",
                                        "photo": "https://media.api-sports.io/football/players/1485.png"
                                      }
                                    ]
                                    """)
                    )
            )
    )
    @CommonErrorResponses
    ResponseEntity<List<PlayerResponse>> getPlayers(@RequestParam Integer season);

    @Operation(
            summary = "선수 상세 조회 API",
            description = "선수 ID와 시즌으로 선수 한 명의 프로필을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "선수 상세 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerResponse.class))
    )
    @CommonErrorResponses
    ResponseEntity<PlayerResponse> getPlayer(
            @PathVariable Long playerId,
            @RequestParam Integer season);
}
