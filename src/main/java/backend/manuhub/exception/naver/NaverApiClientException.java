package backend.manuhub.exception.naver;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;

public class NaverApiClientException extends ManuHubException {
    public NaverApiClientException() {
        super(ErrorCode.NAVER_API_CLIENT_ERROR);
    }
}
