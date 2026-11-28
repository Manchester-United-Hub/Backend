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
                    - 연결되면 `connect`(data: connected) 다음 `match-lineup.updated`로 현재 라인업 전체를 받습니다. 미공개 상태라면 lineupStatus는 NOT_AVAILABLE입니다.
                    - 이미 종료된 경기(FT/AET/PEN)는 연결하지 않고 204를 응답합니다. `EventSource`는 `onerror`가 호출되고 연결이 종료(readyState=2)되므로, 이 경우 기존 조회 API로 전체 데이터를 한 번 조회하세요.
                    - 최초 연결과 재연결 시마다 현재 라인업 전체를 다시 전송합니다. 라인업 또는 이벤트 저장이 커밋되면 `match-lineup.updated`로 선수별 사진·골·도움·카드·교체 기록을 포함한 새 스냅샷을 전송합니다.
                    - `EventSource.addEventListener('match-lineup.updated', ...)`로 받아 기존 라인업을 전체 교체하세요. 변경이 없을 때는 데이터 이벤트를 보내지 않습니다.
                    - 25초 간격의 `heartbeat`(data: ping)는 연결 유지용이며 화면 갱신에 사용하지 않습니다. 연결은 2시간 후 서버가 닫고 브라우저가 재연결할 수 있습니다.
                    - 경기 페이지를 벗어날 때는 `EventSource.close()`를 호출하세요.
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

                                    event: match-lineup.updated
                                    data: {"matchId":1557484,"lineupStatus":"NOT_AVAILABLE","home":null,"away":null}
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
