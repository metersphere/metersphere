package io.metersphere.listener;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plan.service.TestPlanReportService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TrackAppStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private TestPlanReportService testPlanReportService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LogUtil.info("Start checking for incomplete reports");
        testPlanReportService.exceptionHandling();
        LogUtil.info("Completion Check Not Executed Completed Report");
    }
}
