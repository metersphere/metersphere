package io.metersphere.system.config;

import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class TestConfiguration {

        @Bean(destroyMethod = "close")
        public MockServerClient mockServerClient(@Value("${embedded.mockserver.host}") String host,
                                                 @Value("${embedded.mockserver.port}") int port) {

            return new MockServerClient(host, port);
        }
    }