package backend.manuhub.news;

import backend.manuhub.annotation.CommonErrorResponses;
import backend.manuhub.exception.ErrorResponse;
import backend.manuhub.news.dto.NewsListGetResponse;
import backend.manuhub.news.dto.NewsRecentGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "뉴스 관련 API")
public interface NewsAPI {

    @Operation(
            summary = "뉴스 목록 조회 API",
            description = "커서 기반으로 뉴스를 목록으로 조회합니다.\n\n" +
                    "cursorAt과 cursorId는 둘 다 입력하거나 둘 다 입력하지 않아야 합니다.",
            parameters = {
                    @Parameter(
                            name = "cursorAt",
                            description = "이전 페이지의 마지막 뉴스 publishedAt 값 (yyyy-MM-ddTHH:mm)",
                            required = false,
                            schema = @Schema(type = "string", example = "2026-05-18T17:40")),
                    @Parameter(
                            name = "cursorId",
                            description = "이전 페이지의 마지막 뉴스 ID",
                            required = false,
                            schema = @Schema(type = "integer", format = "int64", example = "82")),
                    @Parameter(
                            name = "size",
                            description = "조회할 뉴스 갯수 (기본값 10)",
                            required = false,
                            schema = @Schema(type = "integer", example = "10"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "뉴스 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NewsListGetResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "newsList": [
                                                    {
                                                        "id": 79,
                                                        "title": "\\"한 경기 더 남아있으니 지켜보시죠\\"…PL 역사 쓴 맨체스터 유나이티드...",
                                                        "description": "\\"환상적인 업적이다.\\" 마이클 캐릭 맨체스터 유나이티드 임시 감독이 잉글랜드 프리미어리그(PL) 단일 시즌 최다 도움 타이 기록을 쓴 브루노 페르난데스(맨유)를 극찬했다. 맨유는 17일(한국시각) 영국 맨체스터의 올드... ",
                                                        "link": "https://m.sports.naver.com/wfootball/article/117/0004065635",
                                                        "originalLink": "https://www.mydaily.co.kr/page/view/2026051811181938218",
                                                        "publishedAt": "2026-05-18T18:04"
                                                    },
                                                    {
                                                        "id": 80,
                                                        "title": "'EPL 감독의 선수 커리어 TOP 20' … 캐릭 3위·펩 2위, '신입생'이 압도...",
                                                        "description": "3위는 맨체스터 유나이티드(맨유)의 상승을 이끈 마이클 캐릭 감독이다. 그는 1999년 웨스트햄 1군으로 올라선 후 2004년 토트넘을 거쳐 2006년 맨유로 이적했다. 캐릭의 전성기가 열렸다. 수비형 미드필더 캐릭은... ",
                                                        "link": "https://www.newdaily.co.kr/site/data/html/2026/05/18/2026051800195.html",
                                                        "originalLink": "https://www.newdaily.co.kr/site/data/html/2026/05/18/2026051800195.html",
                                                        "publishedAt": "2026-05-18T18:00"
                                                    },
                                                    {
                                                        "id": 81,
                                                        "title": "\\"맨체스터는 영원히 내 집\\" 맨유 떠나는 카세미루의 고백…\\"행복과 따뜻...",
                                                        "description": "맨체스터 유나이티드 유니폼을 입고 마지막 홈 경기를 치른 카세미루가 팬들에게 감사 인사를 전했다. 맨유는 17일 오후 8시 30분(한국시간) 영국 맨체스터에 위치한 올드 트래포드에서 열린 2025-26시즌 프리미어리그... ",
                                                        "link": "https://m.sports.naver.com/wfootball/article/413/0000218225",
                                                        "originalLink": "https://www.interfootball.co.kr/news/articleView.html?idxno=686727",
                                                        "publishedAt": "2026-05-18T17:45"
                                                    },
                                                    {
                                                        "id": 82,
                                                        "title": "\\"슬롯, 이기적인 살라 고별전에서 명단 제외해야… 나도 퍼거슨 감독과...",
                                                        "description": "맨체스터유나이티드 전설 웨인 루니는 반대로 \\"살라가 리버풀에서 이뤄낸 업적을 생각하면 안타깝다. 그가 슬롯 감독을 또다시 비난하는 건 적절하지 않다\\"라며 \\"살라는 헤비메탈 축구, 클롭 감독의 축구를 원한다. 하지만... ",
                                                        "link": "https://m.sports.naver.com/wfootball/article/436/0000109806",
                                                        "originalLink": "http://www.footballist.co.kr/news/articleView.html?idxno=210264",
                                                        "publishedAt": "2026-05-18T17:40"
                                                    }
                                                ],
                                                "nextCursorId": 82,
                                                "nextCursorAt": "2026-05-18T17:40"
                                            }
                                            """)))})
    @CommonErrorResponses
    ResponseEntity<NewsListGetResponse> getNewsList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size);


    @Operation(
            summary = "최신 뉴스 5개 조회 API",
            description = "최신 뉴스 5개를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "최신 뉴스 5개 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NewsRecentGetResponse.class),
                                    examples = @ExampleObject(value = """
                                            [
                                                {
                                                    "id": 73,
                                                    "title": "15경기 지휘했는데... 마이클 캐릭, 올해의 감독상 받나?",
                                                    "link": "https://www.ggilbo.com/news/articleView.html?idxno=1158349"
                                                },
                                                {
                                                    "id": 74,
                                                    "title": "\\"호날두 맨유 2기 때와 비슷\\"…리버풀 고별전 앞둔 살라의 SNS 폭탄, 루니...",
                                                    "link": "https://m.sports.naver.com/wfootball/article/413/0000218229"
                                                },
                                                {
                                                    "id": 75,
                                                    "title": "\\"스페셜 원이 돌아온다!\\" 63세 무리뉴, 13년 만에 레알 마드리드 복귀 임...",
                                                    "link": "https://m.sports.naver.com/wfootball/article/139/0002247424"
                                                },
                                                {
                                                    "id": 76,
                                                    "title": "[속보] Here We Go 떴다! 무리뉴, 레알 마드리드 복귀 구두 합의→2년 계...",
                                                    "link": "https://m.sports.naver.com/wfootball/article/413/0000218226"
                                                },
                                                {
                                                    "id": 77,
                                                    "title": "\\"안필드 고별전 명단 제외시켜\\" 퍼거슨에게 당했던 맨유 전설, 다리 끝난...",
                                                    "link": "https://m.sports.naver.com/wfootball/article/109/0005536353"
                                                }
                                            ]
                                            """)))})
    @CommonErrorResponses
    ResponseEntity<List<NewsRecentGetResponse>> getRecentNewsList();
}
