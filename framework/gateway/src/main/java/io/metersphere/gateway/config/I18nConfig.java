package io.metersphere.gateway.config;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.i18n.Translator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class I18nConfig {

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
