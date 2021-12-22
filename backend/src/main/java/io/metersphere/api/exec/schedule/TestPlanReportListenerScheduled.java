package io.metersphere.api.exec.schedule;

import io.metersphere.api.cache.TestPlanReportExecuteCatch;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestPlanReportListenerScheduled {
    /**
     * 定时调用监听检查报告状态
     */
    @Scheduled(cron = "*/9 * * * * ?")
    public void testPlanScheduled() {
        //判断缓冲队列是否存在记录
        if (CollectionUtils.isNotEmpty(MessageCache.jobReportCache)) {
            for (int i = 0; i < MessageCache.jobReportCache.size(); i++) {
                this.listener(MessageCache.jobReportCache.get(i));
            }
        }
    }

    private void listener(String planReportId) {
        if (TestPlanReportExecuteCatch.getTestPlanExecuteInfo(planReportId) != null) {
            if (!CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).checkPlanReport(planReportId)) {
                LoggerUtil.info("检查测试计划执行报告：【" + planReportId + "】");
                CommonBeanFactory.getBean(TestPlanReportService.class).countReport(planReportId);
            }
        } else {
            MessageCache.jobReportCache.remove(planReportId);
            LoggerUtil.info("测试计划执行报告：【" + planReportId + "】执行完成，剩余队列：" + MessageCache.jobReportCache.size());
        }
    }
}
