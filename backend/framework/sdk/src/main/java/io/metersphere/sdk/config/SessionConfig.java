package io.metersphere.sdk.config;


import io.metersphere.sdk.constants.SessionConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
@Configuration
public class SessionConfig {

    @Bean
    public HeaderHttpSessionIdResolver sessionIdResolver() {
        return new HeaderHttpSessionIdResolver(SessionConstants.HEADER_TOKEN);
    }
}
