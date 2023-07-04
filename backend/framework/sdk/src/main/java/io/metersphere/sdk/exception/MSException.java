package io.metersphere.sdk.exception;

import io.metersphere.sdk.controller.handler.result.IResultCode;
import org.apache.commons.lang3.StringUtils;

public class MSException extends RuntimeException {

    protected IResultCode errorCode;

    public MSException(String message) {
        super(message);
    }

    public MSException(IResultCode errorCode) {
        super(StringUtils.EMPTY);
        this.errorCode = errorCode;
    }

    public MSException(IResultCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MSException(IResultCode errorCode, Throwable t) {
        super(t);
        this.errorCode = errorCode;
    }

    public IResultCode getErrorCode() {
        return errorCode;
    }
}
