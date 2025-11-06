package io.metersphere.i18n;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import jakarta.annotation.Resource;

import java.util.Locale;

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
     * 根据给定的消息键获取翻译内容，如果没有找到对应的消息，则返回指定的默认消息。
     *
     * @param key            消息键
     * @param defaultMessage 默认消息
     *
     * @return 翻译后的消息，若未找到则返回默认消息
     */
    public static String get(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 设置本地语言
     * @param language 语言
     */
    public static void setLanguage(String language) {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase(language, "US")) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase(language, "TW")){
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }

}
