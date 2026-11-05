package backend.manuhub.exception;

public class MatchNotFoundException extends ManuHubException{
    public MatchNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
