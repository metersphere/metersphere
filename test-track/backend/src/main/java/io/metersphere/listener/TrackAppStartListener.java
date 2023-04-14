package io.metersphere.listener;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.service.TestCaseService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class TrackAppStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private TestCaseService testCaseService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LogUtil.info("Start checking for incomplete reports");
        testPlanReportService.exceptionHandling();
        LogUtil.info("Completion Check Not Executed Completed Report");
        LogUtil.info("Start clean the tmp dir of jar classpath");
        testCaseService.cleanUpTmpDirOfClassPath();
        LogUtil.info("The tmp dir of jar classpath is cleared");
    }
}
