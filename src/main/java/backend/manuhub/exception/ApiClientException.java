package backend.manuhub.exception;

public class ApiClientException extends ManuHubException{

    public ApiClientException(ErrorCode errorCode) {
        super(errorCode);
    }
}
