package io.metersphere.gateway.config;

import io.metersphere.commons.constants.SessionConstants;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
public class SessionConfig {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        HeaderWebSessionIdResolver sessionIdResolver = new HeaderWebSessionIdResolver();
        sessionIdResolver.setHeaderName(SessionConstants.HEADER_TOKEN);        // Define Session Header Name
        return sessionIdResolver;
    }

    @Bean
    public RedisSessionRepository redisSessionRepository() {
        return new RedisSessionRepository(redisTemplate);
    }
}

