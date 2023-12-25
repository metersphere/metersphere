package io.metersphere.sdk.util;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class Translator {
    private static MessageSource messageSource;

    @Resource
    public void setMessageSource(MessageSource messageSource) {
        Translator.messageSource = messageSource;
    }

    /**
     * 单Key翻译
     */
    public static String get(String key) {
        return messageSource.getMessage(key, null, "Not Support Key: " + key, LocaleContextHolder.getLocale());
    }

    /**
     * 单Key翻译，并设置默认值
     */
    public static String get(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 带参数
     */
    public static String getWithArgs(String key, Object... args) {
        return messageSource.getMessage(key, args, "Not Support Key: " + key, LocaleContextHolder.getLocale());
    }
}
