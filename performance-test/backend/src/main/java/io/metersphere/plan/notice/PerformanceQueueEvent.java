package io.metersphere.plan.notice;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.consumer.LoadTestFinishEvent;
import io.metersphere.plan.service.PerfQueueService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PerformanceQueueEvent implements LoadTestFinishEvent {
    @Resource
    private PerfQueueService perfQueueService;

    public void sendNotice(LoadTestReport loadTestReport) {
        //删除性能测试在执行队列中的数据 （在测试计划执行中会将性能测试执行添加到执行队列，用于判断整个测试计划到执行进度）
        try {
            perfQueueService.queueNext(loadTestReport);
        } catch (Exception e) {
            LogUtil.error("PerformanceQueueEvent error. id:" + loadTestReport.getId());
        }
    }

    @Override
    public void execute(LoadTestReport loadTestReport) {
        LogUtil.info("PerformanceQueueEvent start. id:" + loadTestReport.getId() + ", DATA:" + JSON.toJSONString(loadTestReport));
        if (StringUtils.isNotEmpty(loadTestReport.getId())) {
            sendNotice(loadTestReport);
        }
        LogUtil.info("PerformanceQueueEvent OVER. id:" + loadTestReport.getId());
    }
}