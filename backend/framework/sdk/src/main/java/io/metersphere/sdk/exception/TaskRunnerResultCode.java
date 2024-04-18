package io.metersphere.sdk.exception;

/**
 * @author jianxing
 */
public enum TaskRunnerResultCode implements IResultCode {

    MS_URL_NOT_AVAILABLE(200001, "ms_url_not_available");


    private final int code;
    private final String message;

    TaskRunnerResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return getTranslationMessage(this.message);
    }
}
