package backend.manuhub.playerdetail;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.playerdetail.dto.PlayerDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "선수 상세 정보 API")
public interface PlayerDetailAPI {

    @Operation(
            summary = "선수 상세 기록 조회 API",
            description = "선수 ID와 시즌으로 선수 한 명의 대회별 상세 기록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "선수 상세 기록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerDetailResponse.class))
    )
    @CommonErrorResponses
    ResponseEntity<PlayerDetailResponse> getPlayerDetail(
            @PathVariable Long playerId,
            @RequestParam Integer season);
}
