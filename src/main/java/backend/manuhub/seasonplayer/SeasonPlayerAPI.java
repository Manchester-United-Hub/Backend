package backend.manuhub.seasonplayer;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.seasonplayer.dto.SeasonPlayerListResponse;
import backend.manuhub.seasonplayer.dto.SeasonPlayerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "선수 API")
public interface SeasonPlayerAPI {

    @Operation(
            summary = "선수 목록 조회 API",
            description = "season을 전달하면 해당 시즌에 뛴 선수를 조회하고, 생략하면 전체 선수를 조회합니다. 선수 이름 오름차순으로 페이징합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "선수 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SeasonPlayerListResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "players": [
                                        {
                                          "id": 1485,
                                          "name": "Bruno Fernandes",
                                          "birthDate": "1994-09-08",
                                          "nationality": "Portugal",
                                          "height": "179 cm",
                                          "weight": "69 kg",
                                          "number": 8,
                                          "position": "Midfielder",
                                          "photo": "https://media.api-sports.io/football/players/1485.png",
                                          "seasons": [2020, 2021, 2022, 2023, 2024, 2025]
                                        }
                                      ],
                                      "page": 0,
                                      "size": 20,
                                      "totalElements": 35,
                                      "totalPages": 2,
                                      "hasNext": true
                                    }
                                    """)))
    )
    @CommonErrorResponses
    ResponseEntity<SeasonPlayerListResponse> getSeasonPlayers(
            @Parameter(description = "시즌 시작 연도", required = false,
                    schema = @Schema(type = "integer", example = "2025"))
            @RequestParam(required = false) Integer season,
            @Parameter(description = "페이지 번호(0부터 시작)", required = false,
                    schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기(최대 100)", required = false,
                    schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

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
