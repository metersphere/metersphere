package io.metersphere.autoconfigure;

import io.metersphere.sechedule.ScheduleManager;
import io.metersphere.service.BaseScheduleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

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
