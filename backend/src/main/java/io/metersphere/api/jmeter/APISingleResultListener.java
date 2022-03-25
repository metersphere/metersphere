package io.metersphere.api.jmeter;


import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.ResultDTO;
import io.metersphere.jmeter.MsExecListener;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class APISingleResultListener extends MsExecListener {

    private ApiExecutionQueueService apiExecutionQueueService;

    @Override
    public void handleTeardownTest(ResultDTO dto, Map<String, Object> kafkaConfig) {
        LoggerUtil.info("处理单条执行结果报告【" + dto.getReportId() + " 】,资源【 " + dto.getTestId() + " 】");
        dto.setConsole(FixedCapacityUtils.getJmeterLogger(dto.getReportId()));
        CommonBeanFactory.getBean(TestResultService.class).saveResults(dto);
    }

    @Override
    public void testEnded(ResultDTO dto, Map<String, Object> kafkaConfig) {
        try {
            LoggerUtil.info("进入TEST-END处理报告【" + dto.getReportId() + " 】整体执行完成；" + dto.getRunMode());
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(dto.getReportId());
            dto.setConsole(FixedCapacityUtils.getJmeterLogger(dto.getReportId()));
            // 整体执行结束更新资源状态
            CommonBeanFactory.getBean(TestResultService.class).testEnded(dto);

            if (apiExecutionQueueService == null) {
                apiExecutionQueueService = CommonBeanFactory.getBean(ApiExecutionQueueService.class);
            }
            LoggerUtil.info("执行队列处理：" + dto.getQueueId());
            if (StringUtils.isNotEmpty(dto.getQueueId())) {
                apiExecutionQueueService.queueNext(dto);
            }
            // 更新测试计划报告
            if (StringUtils.isNotEmpty(dto.getTestPlanReportId())) {
                LoggerUtil.info("Check Processing Test Plan report status：" + dto.getQueueId() + "，" + dto.getTestId());
                apiExecutionQueueService.testPlanReportTestEnded(dto.getTestPlanReportId());
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
        }
    }
}
