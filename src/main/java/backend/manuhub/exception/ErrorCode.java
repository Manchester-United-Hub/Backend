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

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR","서버 내부 오류입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
