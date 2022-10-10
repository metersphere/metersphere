package io.metersphere.gateway.config;

import io.metersphere.service.MicroService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicroConfig {
    @Bean
    public MicroService microService() {
        return new MicroService();
    }
}
