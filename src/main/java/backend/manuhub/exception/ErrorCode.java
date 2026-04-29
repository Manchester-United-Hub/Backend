package backend.manuhub.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    /**
     * naver api
     */
    NAVER_API_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "NAVER_API_CLIENT_ERROR", "네이버 API 요청 오류입니다."),
    NAVER_API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "NAVER_API_SERVER_ERROR", "네이버 API 서버 오류입니다."),

    /**
     * common
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR","서버 내부 오류입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
