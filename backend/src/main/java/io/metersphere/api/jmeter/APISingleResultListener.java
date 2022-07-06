package io.metersphere.api.jmeter;


import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.cache.JMeterEngineCache;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ResultDTO;
import io.metersphere.jmeter.JMeterBase;
import io.metersphere.jmeter.MsExecListener;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.RetryResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class APISingleResultListener implements MsExecListener {
    private ApiExecutionQueueService apiExecutionQueueService;
    private List<SampleResult> queues;

    /**
     * 参数初始化方法
     */
    @Override
    public void setupTest() {
        queues = new LinkedList<>();
        LoggerUtil.info("初始化监听");
    }

    @Override
    public void handleTeardownTest(List<SampleResult> results, ResultDTO dto, Map<String, Object> kafkaConfig) {
        LoggerUtil.info("接收到执行结果：" + results.size(), dto.getReportId());
        queues.addAll(results);
    }

    @Override
    public void testEnded(ResultDTO dto, Map<String, Object> kafkaConfig) {
        try {
            // 清理过程步骤
            queues = RetryResultUtil.clearLoops(queues);
            JMeterBase.resultFormatting(queues, dto);
            if (dto.isRetryEnable()) {
                LoggerUtil.info("重试结果处理【" + dto.getReportId() + " 】开始");
                RetryResultUtil.mergeRetryResults(dto.getRequestResults());
                LoggerUtil.info("重试结果处理【" + dto.getReportId() + " 】结束");
            }

            dto.setConsole(FixedCapacityUtils.getJmeterLogger(dto.getReportId(), !StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())));
            // 入库存储
            CommonBeanFactory.getBean(TestResultService.class).saveResults(dto);
            LoggerUtil.info("进入TEST-END处理报告【" + dto.getReportId() + " 】" + dto.getRunMode() + " 整体执行完成");
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(dto.getReportId());
            // 整体执行结束更新资源状态
            CommonBeanFactory.getBean(TestResultService.class).testEnded(dto);
            if (apiExecutionQueueService == null) {
                apiExecutionQueueService = CommonBeanFactory.getBean(ApiExecutionQueueService.class);
            }
            if (StringUtils.isNotEmpty(dto.getQueueId())) {
                apiExecutionQueueService.queueNext(dto);
            }
            // 更新测试计划报告
            if (StringUtils.isNotEmpty(dto.getTestPlanReportId())) {
                LoggerUtil.info("Check Processing Test Plan report status：" + dto.getQueueId() + "，" + dto.getTestId());
                apiExecutionQueueService.testPlanReportTestEnded(dto.getTestPlanReportId());
            }
            LoggerUtil.info("TEST-END处理结果集完成", dto.getReportId());
        } catch (Exception e) {
            LoggerUtil.error("结果集处理异常", dto.getReportId(), e);
        } finally {
            if (JMeterEngineCache.runningEngine.containsKey(dto.getReportId())) {
                JMeterEngineCache.runningEngine.remove(dto.getReportId());
            }
            queues.clear();
        }
    }
}
