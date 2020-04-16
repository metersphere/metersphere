package io.metersphere.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Translator {
    private static MessageSource messageSource;

    /**
     * 单Key翻译
     */
    public static String get(String key) {
        return messageSource.getMessage(key, null, "Not Support Key", LocaleContextHolder.getLocale());
    }

    @Resource
    public void setMessageSource(MessageSource messageSource) {
        Translator.messageSource = messageSource;
    }
}
