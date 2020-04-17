package io.metersphere.config;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.i18n.I18nManager;
import io.metersphere.i18n.Translator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class I18nConfig {

    @Bean
    @ConditionalOnMissingBean
    public I18nManager i18nManager() {
        List<String> dirs = new ArrayList<>();
        dirs.add("i18n/");
        return new I18nManager(dirs);
    }

    @Bean
    @ConditionalOnMissingBean
    public Translator translator() {
        return new Translator();
    }

    @Bean
    @ConditionalOnMissingBean
    public CommonBeanFactory commonBeanFactory() {
        return new CommonBeanFactory();
    }
}
