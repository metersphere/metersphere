package io.metersphere.autoconfigure;

import io.metersphere.commons.constants.SessionConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;

public class SessionConfig {

    @Bean
    public HeaderHttpSessionIdResolver sessionIdResolver() {
        return new HeaderHttpSessionIdResolver(SessionConstants.HEADER_TOKEN);
    }
}
