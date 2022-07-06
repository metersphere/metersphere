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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 暂时保留
        // dto.setConsole(FixedCapacityUtils.getJmeterLogger(dto.getReportId()));
        // JMeterBase.resultFormatting(results, dto);
        // CommonBeanFactory.getBean(TestResultService.class).saveResults(dto);
        this.clearLoops(results);
        queues.addAll(results);
    }

    @Override
    public void testEnded(ResultDTO dto, Map<String, Object> kafkaConfig) {
        try {
            LoggerUtil.info("进入TEST-END处理结果集开始", dto.getReportId());
            JMeterBase.resultFormatting(queues, dto);
            dto.setConsole(FixedCapacityUtils.getJmeterLogger(dto.getReportId(), !StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())));
            // 入库存储
            CommonBeanFactory.getBean(TestResultService.class).saveResults(dto);
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

    private void clearLoops(List<SampleResult> results) {
        if (CollectionUtils.isNotEmpty(results)) {
            results = results.stream().filter(sampleResult ->
                    StringUtils.isNotEmpty(sampleResult.getSampleLabel())
                            && !sampleResult.getSampleLabel().startsWith("MS_CLEAR_LOOPS_VAR_"))
                    .collect(Collectors.toList());
        }
    }
}
