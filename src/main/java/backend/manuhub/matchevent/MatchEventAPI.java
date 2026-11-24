package backend.manuhub.matchevent;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.exception.ErrorResponse;
import backend.manuhub.matchevent.dto.MatchEventListGetResponse;
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

@Tag(name = "경기 중계 관련 API")
public interface MatchEventAPI {

    @Operation(
            summary = "경기 이벤트 조회",
            description = """
                    경기의 이벤트(골, 카드, 교체, VAR)를 시간순으로 조회합니다.
                    - 저장된 이벤트가 없으면 외부 API에서 가져와 저장한 뒤 반환합니다.
                    - 아직 진행되지 않은 경기는 events가 빈 배열인 동일한 형식으로 응답합니다.
                    - type: GOAL(골) / CARD(카드) / SUBSTITUTION(교체) / VAR
                    - SUBSTITUTION은 playerName이 교체 아웃, assistPlayerName이 교체 인 선수입니다.
                    - VAR로 취소된 골은 GOAL 이벤트 없이 VAR(Goal cancelled) 이벤트로만 내려옵니다.
                    - extraMinute는 추가시간입니다. (예: 90+3분이면 elapsedMinute=90, extraMinute=3)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = MatchEventListGetResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이벤트가 있는 경기",
                                            value = """
                                                    {
                                                      "matchId": 1378985,
                                                      "events": [
                                                        {
                                                          "elapsedMinute": 35,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 19220,
                                                          "playerName": "Mason Mount",
                                                          "assistPlayerId": null,
                                                          "assistPlayerName": null,
                                                          "type": "VAR",
                                                          "detail": "Penalty confirmed",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 39,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 747,
                                                          "playerName": "Casemiro",
                                                          "assistPlayerId": null,
                                                          "assistPlayerName": null,
                                                          "type": "CARD",
                                                          "detail": "Yellow Card",
                                                          "comments": "Simulation"
                                                        },
                                                        {
                                                          "elapsedMinute": 52,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 157997,
                                                          "playerName": "A. Diallo",
                                                          "assistPlayerId": 886,
                                                          "assistPlayerName": "D. Dalot",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 1",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 53,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 747,
                                                          "playerName": "Casemiro",
                                                          "assistPlayerId": 115589,
                                                          "assistPlayerName": "B. Sesko",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 2",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 58,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 195106,
                                                          "playerName": "Rodrigo Muniz",
                                                          "assistPlayerId": null,
                                                          "assistPlayerName": null,
                                                          "type": "GOAL",
                                                          "detail": "Own Goal",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 62,
                                                          "extraMinute": null,
                                                          "teamId": 36,
                                                          "teamName": "Fulham",
                                                          "playerId": 2920,
                                                          "playerName": "T. Castagne",
                                                          "assistPlayerId": 19221,
                                                          "assistPlayerName": "H. Wilson",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 1",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 62,
                                                          "extraMinute": null,
                                                          "teamId": 36,
                                                          "teamName": "Fulham",
                                                          "playerId": 19032,
                                                          "playerName": "R. Sessegnon",
                                                          "assistPlayerId": 19549,
                                                          "assistPlayerName": "A. Robinson",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 2",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 69,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 19220,
                                                          "playerName": "M. Mount",
                                                          "assistPlayerId": 51494,
                                                          "assistPlayerName": "M. Ugarte",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 3",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 71,
                                                          "extraMinute": null,
                                                          "teamId": 36,
                                                          "teamName": "Fulham",
                                                          "playerId": 195106,
                                                          "playerName": "Rodrigo Muniz",
                                                          "assistPlayerId": 2887,
                                                          "assistPlayerName": "R. Jimenez",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 3",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 71,
                                                          "extraMinute": null,
                                                          "teamId": 36,
                                                          "teamName": "Fulham",
                                                          "playerId": 389315,
                                                          "playerName": "J. King",
                                                          "assistPlayerId": 1161,
                                                          "assistPlayerName": "E. Smith Rowe",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 4",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 73,
                                                          "extraMinute": null,
                                                          "teamId": 36,
                                                          "teamName": "Fulham",
                                                          "playerId": 1161,
                                                          "playerName": "E. Smith Rowe",
                                                          "assistPlayerId": 1455,
                                                          "assistPlayerName": "A. Iwobi",
                                                          "type": "GOAL",
                                                          "detail": "Normal Goal",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 86,
                                                          "extraMinute": null,
                                                          "teamId": 36,
                                                          "teamName": "Fulham",
                                                          "playerId": 2823,
                                                          "playerName": "Saša Lukić",
                                                          "assistPlayerId": null,
                                                          "assistPlayerName": null,
                                                          "type": "CARD",
                                                          "detail": "Yellow Card",
                                                          "comments": "Foul"
                                                        },
                                                        {
                                                          "elapsedMinute": 87,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 891,
                                                          "playerName": "L. Shaw",
                                                          "assistPlayerId": 2935,
                                                          "assistPlayerName": "H. Maguire",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 4",
                                                          "comments": null
                                                        },
                                                        {
                                                          "elapsedMinute": 87,
                                                          "extraMinute": null,
                                                          "teamId": 33,
                                                          "teamName": "Manchester United",
                                                          "playerId": 342970,
                                                          "playerName": "L. Yoro",
                                                          "assistPlayerId": 402329,
                                                          "assistPlayerName": "A. Heaven",
                                                          "type": "SUBSTITUTION",
                                                          "detail": "Substitution 5",
                                                          "comments": null
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "아직 진행되지 않은 경기 (이벤트 없음)",
                                            value = """
                                                    {
                                                      "matchId": 1557450,
                                                      "events": []
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 경기를 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "해당 매치 아이디 DB에 존재x",
                                    value = """
                                {
                                  "code": "MATCH_NOT_FOUND_ERROR",
                                  "message": "해당 경기를 찾을 수 없습니다."
                                }
                                """
                            )
                    )
            )
    })
    @CommonErrorResponses
    ResponseEntity<MatchEventListGetResponse> getMatchEvents(
            @Parameter(description = "경기 ID", required = true, example = "1378985",
                    schema = @Schema(type = "integer", format = "int64", defaultValue = "1378985"))
            @PathVariable Long matchId
    );
}
