package io.metersphere.sdk.exception;

import io.metersphere.sdk.controller.handler.result.IResultCode;
import org.apache.commons.lang3.StringUtils;

public class MSException extends RuntimeException {

    protected IResultCode errorCode;

    protected MSException(String message) {
        super(message);
    }

    protected MSException(IResultCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected MSException(IResultCode errorCode, Throwable t) {
        super(t);
        this.errorCode = errorCode;
    }

    public static void throwException(String message) {
        throw new MSException(message);
    }

    public static void throwException(Throwable t) {
        throw new MSException(null, t);
    }

    public static void throwException(IResultCode errorCode) {
        throw new MSException(errorCode, StringUtils.EMPTY);
    }

    public static void throwException(IResultCode errorCode, String message) {
        throw new MSException(errorCode, message);
    }

    public static void throwException(IResultCode errorCode, Throwable t) {
        throw new MSException(errorCode, t);
    }

    public IResultCode getErrorCode() {
        return errorCode;
    }
}
