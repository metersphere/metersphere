package io.metersphere.sdk.config;

import io.metersphere.sdk.sechedule.BaseScheduleService;
import io.metersphere.sdk.sechedule.ScheduleManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleConfig {

    @Bean
    @ConditionalOnProperty(prefix = "quartz", value = "enabled", havingValue = "true")
    public ScheduleManager scheduleManager() {
        return new ScheduleManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = "quartz", value = "enabled", havingValue = "true")
    public BaseScheduleService baseScheduleService() {
        return new BaseScheduleService();
    }
}
