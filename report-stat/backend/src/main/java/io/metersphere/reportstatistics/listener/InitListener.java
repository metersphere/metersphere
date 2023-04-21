package io.metersphere.reportstatistics.listener;

import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.service.BaseScheduleService;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitListener implements ApplicationRunner {

    @Resource
    private BaseScheduleService baseScheduleService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        baseScheduleService.startEnableSchedules(ScheduleGroup.SCHEDULE_SEND_REPORT);
    }
}
