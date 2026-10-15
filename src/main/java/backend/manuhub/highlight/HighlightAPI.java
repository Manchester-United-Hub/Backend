package backend.manuhub.highlight;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.highlight.dto.HighlightListGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "하이라이트 관련 API")
public interface HighlightAPI {

    @Operation(
            summary = "하이라이트 영상 목록 조회 API",
            description = "시즌별 하이라이트 영상 목록을 조회합니다. 최신순 또는 조회수순으로 정렬할 수 있습니다. 일부 영상은 YouTube에서 조회수를 비공개로 설정해 viewCount가 0으로 내려올 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "하이라이트 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HighlightListGetResponse.class),
                                    examples = @ExampleObject(value = """
                                        {
                                            "season": 2026,
                                            "highlights": [
                                                {
                                                    "videoId": "ZnAyNQG3gRg",
                                                    "title": "Atletico de Madrid 2-0 Man Utd | Highlights",
                                                    "thumbnailUrl": "https://pub-8dfe7ca8163c400aac6a83640c67edb4.r2.dev/highlights/ZnAyNQG3gRg.png",
                                                    "embedUrl": "https://www.youtube.com/embed/ZnAyNQG3gRg",
                                                    "publishedAt": "2026-10-13T21:45:27",
                                                    "viewCount": 965848
                                                },
                                                {
                                                    "videoId": "F4vNF4rPk_I",
                                                    "title": "Man Utd 1-1 Spurs | Extended Highlights",
                                                    "thumbnailUrl": "https://pub-8dfe7ca8163c400aac6a83640c67edb4.r2.dev/highlights/F4vNF4rPk_I.png",
                                                    "embedUrl": "https://www.youtube.com/embed/F4vNF4rPk_I",
                                                    "publishedAt": "2026-10-10T21:08:25",
                                                    "viewCount": 565735
                                                }
                                            ],
                                            "page": 0,
                                            "size": 20,
                                            "totalElements": 30,
                                            "totalPages": 2,
                                            "hasNext": true
                                        }
                                        """)))}
    )
    @CommonErrorResponses
    ResponseEntity<HighlightListGetResponse> getHighlights(
            @Parameter(description = "조회할 시즌 (예: 2026)", required = true,
                    schema = @Schema(type = "integer", example = "2026"))
            @RequestParam int season,
            @Parameter(description = "정렬 기준 (LATEST: 최신순, VIEW_COUNT: 조회순)", required = false,
                    schema = @Schema(type = "string", allowableValues = {"LATEST", "VIEW_COUNT"}, defaultValue = "LATEST"))
            @RequestParam(defaultValue = "LATEST") HighlightSort sort,
            @Parameter(description = "페이지 번호(0부터 시작)", required = false,
                    schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(최대 100)", required = false,
                    schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100"))
            @RequestParam(defaultValue = "20") int size
    );
}
