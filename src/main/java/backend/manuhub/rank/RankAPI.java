package backend.manuhub.rank;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.rank.dto.RankGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "순위 관련 API")
public interface RankAPI {

    @Operation(
            summary = "순위 관련 조회 API",
            description = "프리미어리그의 순위를 목록으로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "순위 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RankGetResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "season": "2025-26",
                                                "ranks": [
                                                    {
                                                        "rank": 1,
                                                        "teamId": 42,
                                                        "teamName": "Arsenal",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/42.png",
                                                        "points": 85,
                                                        "played": 38,
                                                        "win": 26,
                                                        "draw": 7,
                                                        "lose": 5,
                                                        "goalsFor": 71,
                                                        "goalsAgainst": 27,
                                                        "goalsDiff": 44,
                                                        "form": "WWWWW"
                                                    },
                                                    {
                                                        "rank": 2,
                                                        "teamId": 50,
                                                        "teamName": "Manchester City",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/50.png",
                                                        "points": 78,
                                                        "played": 38,
                                                        "win": 23,
                                                        "draw": 9,
                                                        "lose": 6,
                                                        "goalsFor": 77,
                                                        "goalsAgainst": 35,
                                                        "goalsDiff": 42,
                                                        "form": "LDWWD"
                                                    },
                                                    {
                                                        "rank": 3,
                                                        "teamId": 33,
                                                        "teamName": "Manchester United",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/33.png",
                                                        "points": 71,
                                                        "played": 38,
                                                        "win": 20,
                                                        "draw": 11,
                                                        "lose": 7,
                                                        "goalsFor": 69,
                                                        "goalsAgainst": 50,
                                                        "goalsDiff": 19,
                                                        "form": "WWDWW"
                                                    },
                                                    {
                                                        "rank": 4,
                                                        "teamId": 66,
                                                        "teamName": "Aston Villa",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/66.png",
                                                        "points": 65,
                                                        "played": 38,
                                                        "win": 19,
                                                        "draw": 8,
                                                        "lose": 11,
                                                        "goalsFor": 56,
                                                        "goalsAgainst": 49,
                                                        "goalsDiff": 7,
                                                        "form": "WWDLL"
                                                    },
                                                    {
                                                        "rank": 5,
                                                        "teamId": 40,
                                                        "teamName": "Liverpool",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/40.png",
                                                        "points": 60,
                                                        "played": 38,
                                                        "win": 17,
                                                        "draw": 9,
                                                        "lose": 12,
                                                        "goalsFor": 63,
                                                        "goalsAgainst": 53,
                                                        "goalsDiff": 10,
                                                        "form": "DLDLW"
                                                    },
                                                    {
                                                        "rank": 6,
                                                        "teamId": 35,
                                                        "teamName": "Bournemouth",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/35.png",
                                                        "points": 57,
                                                        "played": 38,
                                                        "win": 13,
                                                        "draw": 18,
                                                        "lose": 7,
                                                        "goalsFor": 58,
                                                        "goalsAgainst": 54,
                                                        "goalsDiff": 4,
                                                        "form": "DDWWD"
                                                    },
                                                    {
                                                        "rank": 7,
                                                        "teamId": 746,
                                                        "teamName": "Sunderland",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/746.png",
                                                        "points": 54,
                                                        "played": 38,
                                                        "win": 14,
                                                        "draw": 12,
                                                        "lose": 12,
                                                        "goalsFor": 42,
                                                        "goalsAgainst": 48,
                                                        "goalsDiff": -6,
                                                        "form": "WWDDL"
                                                    },
                                                    {
                                                        "rank": 8,
                                                        "teamId": 51,
                                                        "teamName": "Brighton",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/51.png",
                                                        "points": 53,
                                                        "played": 38,
                                                        "win": 14,
                                                        "draw": 11,
                                                        "lose": 13,
                                                        "goalsFor": 52,
                                                        "goalsAgainst": 46,
                                                        "goalsDiff": 6,
                                                        "form": "LLWLW"
                                                    },
                                                    {
                                                        "rank": 9,
                                                        "teamId": 55,
                                                        "teamName": "Brentford",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/55.png",
                                                        "points": 53,
                                                        "played": 38,
                                                        "win": 14,
                                                        "draw": 11,
                                                        "lose": 13,
                                                        "goalsFor": 55,
                                                        "goalsAgainst": 52,
                                                        "goalsDiff": 3,
                                                        "form": "DDLWL"
                                                    },
                                                    {
                                                        "rank": 10,
                                                        "teamId": 49,
                                                        "teamName": "Chelsea",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/49.png",
                                                        "points": 52,
                                                        "played": 38,
                                                        "win": 14,
                                                        "draw": 10,
                                                        "lose": 14,
                                                        "goalsFor": 58,
                                                        "goalsAgainst": 52,
                                                        "goalsDiff": 6,
                                                        "form": "LWDLL"
                                                    },
                                                    {
                                                        "rank": 11,
                                                        "teamId": 36,
                                                        "teamName": "Fulham",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/36.png",
                                                        "points": 52,
                                                        "played": 38,
                                                        "win": 15,
                                                        "draw": 7,
                                                        "lose": 16,
                                                        "goalsFor": 47,
                                                        "goalsAgainst": 51,
                                                        "goalsDiff": -4,
                                                        "form": "WDLLW"
                                                    },
                                                    {
                                                        "rank": 12,
                                                        "teamId": 34,
                                                        "teamName": "Newcastle",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/34.png",
                                                        "points": 49,
                                                        "played": 38,
                                                        "win": 14,
                                                        "draw": 7,
                                                        "lose": 17,
                                                        "goalsFor": 53,
                                                        "goalsAgainst": 55,
                                                        "goalsDiff": -2,
                                                        "form": "LWDWL"
                                                    },
                                                    {
                                                        "rank": 13,
                                                        "teamId": 45,
                                                        "teamName": "Everton",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/45.png",
                                                        "points": 49,
                                                        "played": 38,
                                                        "win": 13,
                                                        "draw": 10,
                                                        "lose": 15,
                                                        "goalsFor": 47,
                                                        "goalsAgainst": 50,
                                                        "goalsDiff": -3,
                                                        "form": "LLDDL"
                                                    },
                                                    {
                                                        "rank": 14,
                                                        "teamId": 63,
                                                        "teamName": "Leeds",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/63.png",
                                                        "points": 47,
                                                        "played": 38,
                                                        "win": 11,
                                                        "draw": 14,
                                                        "lose": 13,
                                                        "goalsFor": 49,
                                                        "goalsAgainst": 56,
                                                        "goalsDiff": -7,
                                                        "form": "LWDWD"
                                                    },
                                                    {
                                                        "rank": 15,
                                                        "teamId": 52,
                                                        "teamName": "Crystal Palace",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/52.png",
                                                        "points": 45,
                                                        "played": 38,
                                                        "win": 11,
                                                        "draw": 12,
                                                        "lose": 15,
                                                        "goalsFor": 41,
                                                        "goalsAgainst": 51,
                                                        "goalsDiff": -10,
                                                        "form": "LDLDL"
                                                    },
                                                    {
                                                        "rank": 16,
                                                        "teamId": 65,
                                                        "teamName": "Nottingham Forest",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/65.png",
                                                        "points": 44,
                                                        "played": 38,
                                                        "win": 11,
                                                        "draw": 11,
                                                        "lose": 16,
                                                        "goalsFor": 48,
                                                        "goalsAgainst": 51,
                                                        "goalsDiff": -3,
                                                        "form": "DLDWW"
                                                    },
                                                    {
                                                        "rank": 17,
                                                        "teamId": 47,
                                                        "teamName": "Tottenham",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/47.png",
                                                        "points": 41,
                                                        "played": 38,
                                                        "win": 10,
                                                        "draw": 11,
                                                        "lose": 17,
                                                        "goalsFor": 48,
                                                        "goalsAgainst": 57,
                                                        "goalsDiff": -9,
                                                        "form": "WLDWW"
                                                    },
                                                    {
                                                        "rank": 18,
                                                        "teamId": 48,
                                                        "teamName": "West Ham",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/48.png",
                                                        "points": 39,
                                                        "played": 38,
                                                        "win": 10,
                                                        "draw": 9,
                                                        "lose": 19,
                                                        "goalsFor": 46,
                                                        "goalsAgainst": 65,
                                                        "goalsDiff": -19,
                                                        "form": "WLLLW"
                                                    },
                                                    {
                                                        "rank": 19,
                                                        "teamId": 44,
                                                        "teamName": "Burnley",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/44.png",
                                                        "points": 22,
                                                        "played": 38,
                                                        "win": 4,
                                                        "draw": 10,
                                                        "lose": 24,
                                                        "goalsFor": 38,
                                                        "goalsAgainst": 75,
                                                        "goalsDiff": -37,
                                                        "form": "DLDLL"
                                                    },
                                                    {
                                                        "rank": 20,
                                                        "teamId": 39,
                                                        "teamName": "Wolves",
                                                        "teamLogo": "https://media.api-sports.io/football/teams/39.png",
                                                        "points": 20,
                                                        "played": 38,
                                                        "win": 3,
                                                        "draw": 11,
                                                        "lose": 24,
                                                        "goalsFor": 27,
                                                        "goalsAgainst": 68,
                                                        "goalsDiff": -41,
                                                        "form": "DDLDL"
                                                    }
                                                ]
                                            }
                                            """)
                            )
                    )
            }
    )
    @CommonErrorResponses
    public ResponseEntity<RankGetResponse> getPLRank();
}
