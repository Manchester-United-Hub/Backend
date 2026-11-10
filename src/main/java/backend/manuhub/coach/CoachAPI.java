package backend.manuhub.coach;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.coach.dto.CoachGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "감독 정보 API")
public interface CoachAPI {

    @Operation(
            summary = "감독 정보 조회 API",
            description = "팀 ID로 현재 감독의 상세 정보와 경력을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "감독 정보 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CoachGetResponse.class))
    )
    @CommonErrorResponses
    ResponseEntity<CoachGetResponse> getCoach(
            @Parameter(description = "Team ID", required = true,
                    schema = @Schema(type = "integer", example = "33"))
            @PathVariable Long teamId);
}
