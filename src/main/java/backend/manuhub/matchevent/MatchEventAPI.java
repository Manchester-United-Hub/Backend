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
                                                        "matchId": 1557450,
                                                        "events": [
                                                            {
                                                                "elapsedMinute": 34,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 1165,
                                                                "playerName": "Matheus Cunha",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "VAR",
                                                                "detail": "Goal cancelled",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 50,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 19170,
                                                                "playerName": "Morgan Rogers",
                                                                "assistPlayerId": 282125,
                                                                "assistPlayerName": "Romeo Lavia",
                                                                "type": "GOAL",
                                                                "detail": "Normal Goal",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 58,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 356041,
                                                                "playerName": "Carlos Baleba",
                                                                "assistPlayerId": 284322,
                                                                "assistPlayerName": "Kobbie Mainoo",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 1",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 59,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 2467,
                                                                "playerName": "Lisandro Martínez",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "CARD",
                                                                "detail": "Yellow Card",
                                                                "comments": "Foul"
                                                            },
                                                            {
                                                                "elapsedMinute": 61,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 22094,
                                                                "playerName": "Wesley Fofana",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "VAR",
                                                                "detail": "Penalty confirmed",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 63,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 2467,
                                                                "playerName": "Lisandro Martínez",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "CARD",
                                                                "detail": "Red Card",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 64,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 152982,
                                                                "playerName": "Cole Palmer",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "GOAL",
                                                                "detail": "Penalty",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 65,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 2935,
                                                                "playerName": "Harry Maguire",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "CARD",
                                                                "detail": "Yellow Card",
                                                                "comments": "Foul"
                                                            },
                                                            {
                                                                "elapsedMinute": 66,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 1165,
                                                                "playerName": "Matheus Cunha",
                                                                "assistPlayerId": 19220,
                                                                "assistPlayerName": "Mason Mount",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 2",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 66,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 305834,
                                                                "playerName": "Andrey Santos",
                                                                "assistPlayerId": 402329,
                                                                "assistPlayerName": "Ayden Heaven",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 3",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 70,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 284322,
                                                                "playerName": "Kobbie Mainoo",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "CARD",
                                                                "detail": "Yellow Card",
                                                                "comments": "Foul"
                                                            },
                                                            {
                                                                "elapsedMinute": 71,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 425733,
                                                                "playerName": "Estêvão Willian Almeida de Oliveira Gonçalves",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "VAR",
                                                                "detail": "Goal cancelled",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 74,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 383018,
                                                                "playerName": "Marco Palestra",
                                                                "assistPlayerId": 181734,
                                                                "assistPlayerName": "Pep Chavarria",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 1",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 77,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 157997,
                                                                "playerName": "Amad Diallo",
                                                                "assistPlayerId": 70100,
                                                                "assistPlayerName": "Joshua Zirkzee",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 4",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 81,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 20589,
                                                                "playerName": "Bryan Mbeumo",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "CARD",
                                                                "detail": "Yellow Card",
                                                                "comments": "Not Retreating"
                                                            },
                                                            {
                                                                "elapsedMinute": 86,
                                                                "extraMinute": null,
                                                                "teamId": 33,
                                                                "teamName": "Manchester United",
                                                                "playerId": 70100,
                                                                "playerName": "Joshua Zirkzee",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "CARD",
                                                                "detail": "Yellow Card",
                                                                "comments": "Foul"
                                                            },
                                                            {
                                                                "elapsedMinute": 87,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 116117,
                                                                "playerName": "Moises Caicedo",
                                                                "assistPlayerId": 1469,
                                                                "assistPlayerName": "Danny Welbeck",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 2",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 87,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 1864,
                                                                "playerName": "Pedro Neto",
                                                                "assistPlayerId": 319572,
                                                                "assistPlayerName": "Valentin Barco",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 3",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 87,
                                                                "extraMinute": null,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 425733,
                                                                "playerName": "Estêvão Willian Almeida de Oliveira Gonçalves",
                                                                "assistPlayerId": 161907,
                                                                "assistPlayerName": "Malo Gusto",
                                                                "type": "SUBSTITUTION",
                                                                "detail": "Substitution 4",
                                                                "comments": null
                                                            },
                                                            {
                                                                "elapsedMinute": 90,
                                                                "extraMinute": 3,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 340151,
                                                                "playerName": "Mike Penders",
                                                                "assistPlayerId": null,
                                                                "assistPlayerName": null,
                                                                "type": "CARD",
                                                                "detail": "Yellow Card",
                                                                "comments": "Time Wasting"
                                                            },
                                                            {
                                                                "elapsedMinute": 90,
                                                                "extraMinute": 7,
                                                                "teamId": 49,
                                                                "teamName": "Chelsea",
                                                                "playerId": 152982,
                                                                "playerName": "Cole Palmer",
                                                                "assistPlayerId": 419582,
                                                                "assistPlayerName": "Geovany Quenda",
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
            @Parameter(description = "경기 ID", example = "1557450")
            @PathVariable Long matchId
    );
}
