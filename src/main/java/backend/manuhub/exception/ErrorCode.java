package backend.manuhub.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    API_FOOTBALL_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "API_FOOTBALL_CLIENT_ERROR", "API FOOTBALL 요청 오류입니다."),
    API_FOOTBALL_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "API_FOOTBALL_SERVER_ERROR", "API FOOTBALL 서버 오류입니다."),

    API_FOOTBALL_TEAM_STATISTICS_INVALID_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, "API_FOOTBALL_TEAM_STATISTICS_INVALID_RESPONSE_ERROR", "API FOOTBALL TEAM STATISTICS 응답 구조가 올바르지 않습니다."),

    API_FOOTBALL_PLAYER_INVALID_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, "API_FOOTBALL_PLAYER_INVALID_RESPONSE_ERROR", "API FOOTBALL PLAYER 응답 구조가 올바르지 않습니다."),

    API_FOOTBALL_MATCH_INVALID_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, "API_FOOTBALL_MATCH_INVALID_RESPONSE_ERROR", "API FOOTBALL MATCH response structure is invalid."),

    /**
     * naver api
     */
    NAVER_API_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "NAVER_API_CLIENT_ERROR", "네이버 API 요청 오류입니다."),
    NAVER_API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "NAVER_API_SERVER_ERROR", "네이버 API 서버 오류입니다."),

    /**
     * common
     */
    NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "NOT_FOUND_ERROR", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED_ERROR(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED_ERROR", "허용되지 않는 HTTP 메서드입니다."),
    INVALID_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_ERROR", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR","서버 내부 오류입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
