package backend.manuhub.exception;

public class InvalidRequestException extends ManuHubException{
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
