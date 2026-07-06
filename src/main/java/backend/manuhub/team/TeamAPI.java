package backend.manuhub.team;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.team.dto.TeamGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "팀 관련 API")
public interface TeamAPI {

    @Operation(
            summary = "팀 정보 조회 API",
            description = "팀 ID로 팀 기본 정보와 감독 이름을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "팀 정보 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TeamGetResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "id": 33,
                                                "name": "Manchester United",
                                                "photo": "https://media.api-sports.io/football/teams/33.png",
                                                "founded": 1878,
                                                "location": "Manchester, England",
                                                "stadium": "Old Trafford",
                                                "coachName": "Michael Carrick"
                                            }
                                            """)))})
    @CommonErrorResponses
    ResponseEntity<TeamGetResponse> getTeam(@PathVariable Long teamId);
}
