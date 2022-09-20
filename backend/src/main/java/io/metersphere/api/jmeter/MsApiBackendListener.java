package io.metersphere.api.jmeter;


import com.alibaba.fastjson.JSON;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.cache.JMeterEngineCache;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ResultDTO;
import io.metersphere.jmeter.JMeterBase;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.RetryResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MsApiBackendListener extends AbstractBackendListenerClient implements Serializable {
    private ApiExecutionQueueService apiExecutionQueueService;
    private List<SampleResult> queues;
    private ResultDTO dto;

    /**
     * 参数初始化方法
     */
    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        LoggerUtil.info("初始化监听");
        queues = new LinkedList<>();
        this.setParam(context);
        super.setupTest(context);
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        LoggerUtil.info("接收到JMETER执行数据【" + sampleResults.size() + " 】", dto.getReportId());
        queues.addAll(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) {
        try {
            super.teardownTest(context);
            // 清理过程步骤
            queues = RetryResultUtil.clearLoops(queues);
            JMeterBase.resultFormatting(queues, dto);
            if (dto.isRetryEnable()) {
                LoggerUtil.info("重试结果处理【" + dto.getReportId() + " 】开始");
                RetryResultUtil.mergeRetryResults(dto.getRequestResults());
                LoggerUtil.info("重试结果处理【" + dto.getReportId() + " 】结束");
            }

            String console = FixedCapacityUtils.getJmeterLogger(dto.getReportId(), !StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString()));
            if (FileUtils.isFolderExists(dto.getReportId())) {
                console += "\r\n" + "tmp folder  " + FileUtils.BODY_FILE_DIR + File.separator + dto.getReportId() + " has deleted.";
            }
            dto.setConsole(console);

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
            FileUtils.deleteBodyFiles(dto.getReportId());
            if (FileServer.getFileServer() != null) {
                LoggerUtil.info("进入监听，开始关闭CSV", dto.getReportId());
                FileServer.getFileServer().closeCsv(dto.getReportId());
            }
            if (JMeterEngineCache.runningEngine.containsKey(dto.getReportId())) {
                JMeterEngineCache.runningEngine.remove(dto.getReportId());
            }
            queues.clear();
        }
    }

    /**
     * 初始化参数
     *
     * @param context
     */
    private void setParam(BackendListenerContext context) {
        dto = new ResultDTO();
        dto.setTestId(context.getParameter(BackendListenerConstants.TEST_ID.name()));
        dto.setRunMode(context.getParameter(BackendListenerConstants.RUN_MODE.name()));
        dto.setReportId(context.getParameter(BackendListenerConstants.REPORT_ID.name()));
        dto.setReportType(context.getParameter(BackendListenerConstants.REPORT_TYPE.name()));
        dto.setTestPlanReportId(context.getParameter(BackendListenerConstants.MS_TEST_PLAN_REPORT_ID.name()));
        if (context.getParameter(BackendListenerConstants.RETRY_ENABLE.name()) != null) {
            dto.setRetryEnable(Boolean.parseBoolean(context.getParameter(BackendListenerConstants.RETRY_ENABLE.name())));
        }
        dto.setQueueId(context.getParameter(BackendListenerConstants.QUEUE_ID.name()));
        dto.setRunType(context.getParameter(BackendListenerConstants.RUN_TYPE.name()));

        String ept = context.getParameter(BackendListenerConstants.EPT.name());
        if (StringUtils.isNotEmpty(ept)) {
            dto.setExtendedParameters(JSON.parseObject(context.getParameter(BackendListenerConstants.EPT.name()), Map.class));
        }
    }
}
