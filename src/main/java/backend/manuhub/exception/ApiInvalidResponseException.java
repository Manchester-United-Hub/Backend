package backend.manuhub.exception;

public class ApiInvalidResponseException extends ManuHubException {

    public ApiInvalidResponseException(ErrorCode errorCode) {
        super(errorCode);
    }
}
