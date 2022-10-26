package io.metersphere.gateway.config;

import io.metersphere.log.service.OperatingLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfig {

    @Bean
    public OperatingLogService operatingLogService() {
        return new OperatingLogService();
    }
}
