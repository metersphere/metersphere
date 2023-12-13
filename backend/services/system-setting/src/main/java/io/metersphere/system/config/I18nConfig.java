package io.metersphere.system.config;

import io.metersphere.sdk.util.Translator;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class I18nConfig {

    @Bean
    @ConditionalOnMissingBean
    public Translator translator() {
        return new Translator();
    }


    /**
     * JSR-303校验国际化
     *
     * @param messageSource
     * @return
     */
    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(MessageSource messageSource) {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setProviderClass(HibernateValidator.class);
        localValidatorFactoryBean.setValidationMessageSource(messageSource);
        return localValidatorFactoryBean;
    }

    @Bean
    public Validator validator(LocalValidatorFactoryBean localValidatorFactoryBean) {
        return localValidatorFactoryBean.getValidator();
    }
}
