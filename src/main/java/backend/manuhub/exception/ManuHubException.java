package backend.manuhub.exception;

import lombok.Getter;

@Getter
public class ManuHubException extends RuntimeException {

    private final ErrorCode errorCode;

    public ManuHubException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
