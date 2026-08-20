package backend.manuhub.seasonplayer;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
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
public interface SeasonPlayerAPI {

    @Operation(
            summary = "선수 목록 조회 API",
            description = "season을 전달하면 해당 시즌에 뛴 선수들을 조회하고, 생략하면 전체 선수를 조회합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "선수 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SeasonPlayerResponse.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1485,
                                        "name": "Bruno Fernandes",
                                        "birthDate": "1994-09-08",
                                        "nationality": "Portugal",
                                        "height": "179 cm",
                                        "weight": "69 kg",
                                        "photo": "https://media.api-sports.io/football/players/1485.png",
                                        "seasons": [2020, 2021, 2022, 2023, 2024, 2025]
                                      }
                                    ]
                                    """)))
    )
    @CommonErrorResponses
    ResponseEntity<List<SeasonPlayerResponse>> getSeasonPlayers(
            @Parameter(description = "시즌 시작 연도", required = false,
                    schema = @Schema(type = "integer", example = "2025"))
            @RequestParam(required = false) Integer season);

    @Operation(
            summary = "선수 상세 조회 API",
            description = "선수 ID로 프로필을 조회합니다. season을 전달하면 해당 시즌에 뛴 선수인지 확인합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "선수 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SeasonPlayerResponse.class)))
    )
    @CommonErrorResponses
    ResponseEntity<SeasonPlayerResponse> getSeasonPlayer(
            @Parameter(description = "API-Football 선수 ID", required = true,
                    schema = @Schema(type = "integer", example = "1485"))
            @PathVariable Long playerId,
            @Parameter(description = "시즌 시작 연도", required = false,
                    schema = @Schema(type = "integer", example = "2025"))
            @RequestParam(required = false) Integer season);
}
