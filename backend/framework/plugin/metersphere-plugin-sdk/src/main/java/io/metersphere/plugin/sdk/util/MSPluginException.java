package io.metersphere.plugin.sdk.util;

/**
 * 插件异常类
 * @author jianxing
 */
public class MSPluginException extends RuntimeException {
    public MSPluginException() {
        super();
    }

    public MSPluginException(String message) {
        super(message);
    }

    public MSPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public MSPluginException(Throwable cause) {
        super(cause);
    }
}
