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
            description = "matchId로 경기 정보와 저장된 선수별 사진·골·도움·카드·교체 기록이 포함된 라인업을 조회합니다. "
                    + "종료 경기는 누락된 라인업과 이벤트를 외부 API에서 보충할 수 있습니다. "
                    + "양 팀 라인업이 아직 없다면 lineupStatus는 NOT_AVAILABLE입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "경기 일정 단건 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class))
                    )
            }
    )
    @CommonErrorResponses
    // 상세 조회에서 라인업을 함께 제공하므로 이벤트 조회와 같은 예시 경기 ID를 사용한다.
    ResponseEntity<MatchResponse> getMatch(
            @Parameter(description = "경기 ID (상세 응답에 라인업 포함)", required = true,
                    example = "1378985", schema = @Schema(type = "integer", format = "int64", defaultValue = "1378985"))
            @PathVariable Long matchId);
}
