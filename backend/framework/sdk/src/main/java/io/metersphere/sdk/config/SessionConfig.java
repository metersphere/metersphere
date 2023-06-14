package io.metersphere.sdk.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.sdk.constants.SessionConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;

@Configuration
public class SessionConfig {

    @Bean
    public HeaderHttpSessionIdResolver sessionIdResolver() {
        return new HeaderHttpSessionIdResolver(SessionConstants.HEADER_TOKEN);
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(objectMapper());
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
