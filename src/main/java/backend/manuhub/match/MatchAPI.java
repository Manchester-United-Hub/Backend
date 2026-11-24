package backend.manuhub.match;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.match.dto.MatchListResponse;
import backend.manuhub.match.dto.MatchResponse;
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

@Tag(name = "경기 일정 관련 API")
public interface MatchAPI {

    @Operation(
            summary = "경기 일정 목록 조회 API",
            description = "저장된 경기 일정을 경기 날짜 오름차순으로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "경기 일정 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MatchListResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "pastMatches": [
                                                    {
                                                        "matchId": 239625,
                                                        "date": "2020-02-06T23:00:00",
                                                        "status": "FT",
                                                        "round": {
                                                            "name": "Regular Season - 24",
                                                            "number": 24
                                                        },
                                                        "venue": {
                                                            "name": "Stade Municipal",
                                                            "city": "Oued Zem"
                                                        },
                                                        "homeTeam": {
                                                            "teamId": 967,
                                                            "name": "Rapide Oued ZEM",
                                                            "logo": "https://media.api-sports.io/football/teams/967.png",
                                                            "winner": false
                                                        },
                                                        "awayTeam": {
                                                            "teamId": 968,
                                                            "name": "Wydad AC",
                                                            "logo": "https://media.api-sports.io/football/teams/968.png",
                                                            "winner": true
                                                        },
                                                        "score": {
                                                            "home": 0,
                                                            "away": 1
                                                        }
                                                    }
                                                ],
                                                "upcomingMatches": []
                                            }
                                            """)
                            )
                    )
            }
    )
    @CommonErrorResponses
    ResponseEntity<MatchListResponse> getMatches(
            @Parameter(description = "Season year", required = false,
                    schema = @Schema(type = "integer", example = "2026"))
            @RequestParam(required = false) Integer season);

    @Operation(
            summary = "경기 일정 단건 조회 API",
            description = """
                    기본 예시 경기 ID는 1378985이며, /api/matches/1378985/events에서 개별 이벤트 원본을 함께 확인할 수 있습니다.
                    matchId로 경기 기본 정보와 선수별 기록을 포함한 lineup을 조회합니다.
                    종료 경기(FT/AET/PEN)는 저장된 라인업과 이벤트를 우선 사용하고, 누락된 데이터만 외부 API에서 가져와 저장합니다.
                    이벤트 수집을 완료한 종료 경기는 이벤트 목록이 비어 있어도 다시 호출하지 않습니다.
                    lineup은 선수별 photo, goals, assists, cards, substitutions를 포함하며 선발·후보 배치는 최초 명단을 유지합니다.
                    종료 전 경기는 외부 보충 없이 현재 저장된 라인업과 이벤트로 응답합니다.
                    양 팀 라인업을 확보하지 못하면 lineup.lineupStatus는 NOT_AVAILABLE입니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "경기 일정 단건 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class))
                    )
            }
    )
    @CommonErrorResponses
    // 이벤트 조회와 같은 경기 ID를 제공해 상세 응답을 비교하기 쉽게 한다.
    ResponseEntity<MatchResponse> getMatch(
            @Parameter(description = "경기 ID", required = true, example = "1378985",
                    schema = @Schema(type = "integer", format = "int64", defaultValue = "1378985"))
            @PathVariable Long matchId);
}
