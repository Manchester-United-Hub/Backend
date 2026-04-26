package backend.manuhub.exception.naver;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;

public class NaverApiServerException extends ManuHubException {
    public NaverApiServerException() {
        super(ErrorCode.NAVER_API_SERVER_ERROR);
    }
}
