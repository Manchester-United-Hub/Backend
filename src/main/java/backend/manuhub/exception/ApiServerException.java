package backend.manuhub.exception;

public class ApiServerException extends ManuHubException {
    public ApiServerException(ErrorCode errorCode) {
        super(errorCode);
    }
}
