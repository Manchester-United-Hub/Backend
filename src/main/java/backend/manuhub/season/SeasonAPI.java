package backend.manuhub.season;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.season.dto.SeasonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "시즌 API")
public interface SeasonAPI {

    @Operation(
            summary = "현재 시즌 조회 API",
            description = "이전 시즌 종료 후에는 다음 시즌을 반환하며, 해당 시즌의 시작 여부를 함께 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "현재 시즌 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SeasonResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "season": 2026,
                                                "started": false
                                            }
                                            """)))
            }
    )
    @CommonErrorResponses
    ResponseEntity<SeasonResponse> getCurrentSeason();
}
