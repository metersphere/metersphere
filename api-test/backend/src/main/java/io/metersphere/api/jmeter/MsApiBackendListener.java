package io.metersphere.api.jmeter;


import com.fasterxml.jackson.core.type.TypeReference;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.utils.ReportStatusUtil;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.utils.*;
import io.metersphere.vo.ResultVO;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.MsRegexDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.jmeter.JMeterBase;
import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.service.TestResultService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.RetryResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MsApiBackendListener extends AbstractBackendListenerClient implements Serializable {
    private ApiExecutionQueueService apiExecutionQueueService;
    private TestResultService testResultService;
    private List<SampleResult> queues;
    private ResultDTO dto;
    // 当前场景报告/用例结果状态
    private ResultVO resultVO;

    private RedisTemplateService redisTemplateService;

    /**
     * 参数初始化方法
     */
    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        LoggerUtil.info("初始化监听");
        queues = new LinkedList<>();
        this.setParam(context);
        if (apiExecutionQueueService == null) {
            apiExecutionQueueService = CommonBeanFactory.getBean(ApiExecutionQueueService.class);
        }
        if (testResultService == null) {
            testResultService = CommonBeanFactory.getBean(TestResultService.class);
        }
        if (redisTemplateService == null) {
            redisTemplateService = CommonBeanFactory.getBean(RedisTemplateService.class);
        }
        resultVO = new ResultVO();
        super.setupTest(context);
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        LoggerUtil.info("接收到JMETER执行数据【" + sampleResults.size() + " 】", dto.getReportId());
        if (dto.isRetryEnable()) {
            queues.addAll(sampleResults);
        } else {
            if (!StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                dto.setConsole(FixedCapacityUtil.getJmeterLogger(getReportId(), false));
            }
            sampleResults = RetryResultUtil.clearLoops(sampleResults);
            JMeterBase.resultFormatting(sampleResults, dto);
            testResultService.saveResults(dto);
            resultVO = ReportStatusUtil.getStatus(dto, resultVO);
            dto.getArbitraryData().put(CommonConstants.LOCAL_STATUS_KEY, resultVO);
            sampleResults.clear();
        }
    }

    @Override
    public void teardownTest(BackendListenerContext context) {
        try {
            LoggerUtil.info("进入TEST-END处理报告" + dto.getRunMode(), dto.getReportId());
            super.teardownTest(context);
            // 获取执行日志
            if (!StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                dto.setConsole(FixedCapacityUtil.getJmeterLogger(getReportId(), true));
            }
            if (dto.isRetryEnable()) {
                LoggerUtil.info("重试结果处理开始", dto.getReportId());
                // 清理过程步骤
                queues = RetryResultUtil.clearLoops(queues);
                JMeterBase.resultFormatting(queues, dto);

                LoggerUtil.info("合并重试结果集", dto.getReportId());
                RetryResultUtil.mergeRetryResults(dto.getRequestResults());

                LoggerUtil.info("执行结果入库存储", dto.getReportId());
                testResultService.saveResults(dto);
                resultVO = ReportStatusUtil.getStatus(dto, resultVO);
                dto.getArbitraryData().put(CommonConstants.LOCAL_STATUS_KEY, resultVO);
                LoggerUtil.info("重试结果处理结束", dto.getReportId());
            }
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(dto.getReportId());
            // 整体执行结束更新资源状态
            testResultService.testEnded(dto);
            if (StringUtils.isNotEmpty(dto.getQueueId())) {
                LoggerUtil.info("串行进入下一个执行点", dto.getReportId());
                apiExecutionQueueService.queueNext(dto);
            }
            // 更新测试计划报告
            LoggerUtil.info("Check Processing Test Plan report status：" + dto.getQueueId() + "，" + dto.getTestId());
            apiExecutionQueueService.checkTestPlanCaseTestEnd(dto.getTestId(), dto.getRunMode(), dto.getTestPlanReportId());
            LoggerUtil.info("TEST-END处理结果集完成", dto.getReportId());

            JvmUtil.memoryInfo();
        } catch (Exception e) {
            LoggerUtil.error("结果集处理异常", dto.getReportId(), e);
        } finally {
            queues.clear();
            redisTemplateService.delFilePath(dto.getReportId());
            FileUtils.deleteBodyFiles(dto.getReportId());
            if (FileServer.getFileServer() != null) {
                LoggerUtil.info("进入监听，开始关闭CSV", dto.getReportId());
                FileServer.getFileServer().closeCsv(dto.getReportId());
            }
            ApiLocalRunner.clearCache(dto.getReportId());
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
        if (dto.getArbitraryData() == null) {
            dto.setArbitraryData(new LinkedHashMap<>());
        }
        String ept = context.getParameter(BackendListenerConstants.EPT.name());
        if (StringUtils.isNotEmpty(ept)) {
            dto.setExtendedParameters(JSON.parseObject(context.getParameter(BackendListenerConstants.EPT.name()), Map.class));
        }
        if (StringUtils.isNotBlank(context.getParameter(BackendListenerConstants.FAKE_ERROR.name()))) {
            Map<String, List<MsRegexDTO>> fakeErrorMap = JSON.parseObject(
                    context.getParameter(BackendListenerConstants.FAKE_ERROR.name()),
                    new TypeReference<Map<String, List<MsRegexDTO>>>() {});
            dto.setFakeErrorMap(fakeErrorMap);
        }
    }

    private String getReportId() {
        String reportId = dto.getReportId();
        if (StringUtils.isNotEmpty(dto.getTestPlanReportId())
                && !FixedCapacityUtil.containsKey(dto.getTestPlanReportId())
                && StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            reportId = dto.getTestPlanReportId();
        }
        return reportId;
    }
}
