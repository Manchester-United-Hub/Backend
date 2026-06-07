package backend.manuhub.teamstatistics;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.teamstatistics.dto.TeamStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "팀 전적 관련 API")
public interface TeamStatisticsAPI {

    @Operation(
            summary = "팀 전적 목록 조회 API",
            description = "맨체스터 유나이티드의 시즌별 팀 전적 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팀 전적 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TeamStatisticsResponse.class),
                                    examples = @ExampleObject(value = """
                                            [
                                                {
                                                    "teamId": 33,
                                                    "teamName": "Manchester United",
                                                    "season": 2022,
                                                    "win": 23,
                                                    "draw": 6,
                                                    "lose": 9,
                                                    "goalsFor": 58,
                                                    "goalsAgainst": 43,
                                                    "points": 75,
                                                    "rank": 3
                                                },
                                                {
                                                    "teamId": 33,
                                                    "teamName": "Manchester United",
                                                    "season": 2023,
                                                    "win": 18,
                                                    "draw": 6,
                                                    "lose": 14,
                                                    "goalsFor": 57,
                                                    "goalsAgainst": 58,
                                                    "points": 60,
                                                    "rank": 8
                                                },
                                                {
                                                    "teamId": 33,
                                                    "teamName": "Manchester United",
                                                    "season": 2024,
                                                    "win": 11,
                                                    "draw": 9,
                                                    "lose": 18,
                                                    "goalsFor": 44,
                                                    "goalsAgainst": 54,
                                                    "points": 42,
                                                    "rank": 15
                                                }
                                            ]
                                            """)
                            )
                    )
            }
    )
    @CommonErrorResponses
    ResponseEntity<List<TeamStatisticsResponse>> getAllTeamStatistics();
}
