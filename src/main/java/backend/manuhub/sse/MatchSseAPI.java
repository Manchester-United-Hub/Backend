package backend.manuhub.sse;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "경기 실시간 중계 관련 API")
public interface MatchSseAPI {

    @Operation(
            summary = "경기 SSE 연결",
            description = """
                    경기 상세 페이지에 진입하면 이 엔드포인트로 SSE(Server-Sent Events) 연결을 맺습니다.
                    브라우저의 `EventSource`로 연결하며, Swagger UI의 Try it out으로는 스트림을 확인할 수 없습니다.
                    - 연결이 성공하면 `connect` 이벤트(data: connected)를 먼저 받습니다. 이때 `onopen`이 호출됩니다.
                    - 이미 종료된 경기(FT/AET/PEN)는 연결하지 않고 204를 응답합니다. `EventSource`는 `onerror`가 호출되고 연결이 종료(readyState=2)되므로, 이 경우 기존 조회 API로 전체 데이터를 한 번 조회하세요.
                    - 연결은 2시간 후 서버가 닫으며, 브라우저가 자동으로 재연결합니다. 연결이 끊긴 동안의 변경은 전달되지 않으므로, `onopen`이 호출될 때마다 기존 조회 API로 현재 상태를 한 번 가져온 뒤 이후 전달되는 데이터로 갱신하세요.
                    - 경기 페이지를 벗어날 때는 `EventSource.close()`를 호출하세요.
                    - 현재는 연결/해제만 제공하며, 경기 데이터 전송은 추후 추가됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "연결 성공 (text/event-stream)",
                    content = @Content(
                            mediaType = "text/event-stream",
                            examples = @ExampleObject(value = """
                                    event: connect
                                    data: connected
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "이미 종료된 경기라 연결하지 않음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "경기를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "MATCH_NOT_FOUND_ERROR",
                                      "message": "해당 경기를 찾을 수 없습니다."
                                    }
                                    """)
                    )
            )
    })
    @CommonErrorResponses
    ResponseEntity<SseEmitter> connect(
            @Parameter(description = "경기 ID", example = "1557473")
            @PathVariable Long matchId
    );
}
