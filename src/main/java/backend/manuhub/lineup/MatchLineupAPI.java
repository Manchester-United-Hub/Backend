package backend.manuhub.lineup;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "경기 라인업 관련 API")
public interface MatchLineupAPI {

    @Operation(
            summary = "경기 라인업 조회 API",
            description = "경기 ID로 양 팀의 포메이션, 선발 선수, 교체 선수를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "경기 라인업 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MatchLineupResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "matchId": 12345,
                                                "lineupStatus": "AVAILABLE",
                                                "home": {
                                                    "teamId": 33,
                                                    "name": "Manchester United",
                                                    "logo": "https://media.api-sports.io/football/teams/33.png",
                                                    "formation": "4-2-3-1",
                                                    "coach": {
                                                        "coachId": 19,
                                                        "name": "Ruben Amorim",
                                                        "photo": "https://media.api-sports.io/football/coachs/19.png"
                                                    },
                                                    "startPlayers": [
                                                        {
                                                            "playerId": 882,
                                                            "name": "A. Onana",
                                                            "number": 24,
                                                            "position": "G",
                                                            "grid": "1:1"
                                                        }
                                                    ],
                                                    "substitutes": []
                                                },
                                                "away": {
                                                    "teamId": 40,
                                                    "name": "Liverpool",
                                                    "logo": "https://media.api-sports.io/football/teams/40.png",
                                                    "formation": "4-3-3",
                                                    "coach": {
                                                        "coachId": 10,
                                                        "name": "Arne Slot",
                                                        "photo": "https://media.api-sports.io/football/coachs/10.png"
                                                    },
                                                    "startPlayers": [],
                                                    "substitutes": []
                                                }
                                            }
                                            """)
                            )
                    )
            }
    )
    @CommonErrorResponses
    ResponseEntity<MatchLineupResponse> getLineup(
            @Parameter(description = "Match ID", required = true,
                    schema = @Schema(type = "integer", example = "12345"))
            @PathVariable Long matchId
    );
}
