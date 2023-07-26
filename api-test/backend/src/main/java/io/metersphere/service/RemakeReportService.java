package io.metersphere.service;

import io.metersphere.api.jmeter.ApiLocalRunner;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.FixedCapacityUtil;
import io.metersphere.commons.utils.GenerateHashTreeUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
public class RemakeReportService {
    @Resource
    @Lazy
    private ApiExecutionQueueService queueService;
    @Resource
    @Lazy
    private TestResultService testResultService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;

    public void queueNext(JmeterRunRequestDTO request, String errorMsg) {
        try {
            ResultDTO dto = new ResultDTO();
            BeanUtils.copyBean(dto, request);
            dto.setQueueId(request.getQueueId());
            dto.setTestId(request.getTestId());
            dto.setErrorEnded(true);
            LoggerUtil.info("进入异常结果处理：" + dto.getRunMode() + " 整体处理完成", dto.getReportId());
            LoggerUtil.error("执行异常处理：" + errorMsg, request.getReportId());
            if (StringUtils.isNotEmpty(dto.getQueueId())) {
                queueService.queueNext(dto);
            }
            // 更新测试计划报告
            LoggerUtil.info("Check Processing Test Plan report status.queueId："
                    + dto.getQueueId() + "，runMode:" + dto.getRunMode() + "，testId:" + dto.getTestId(), dto.getReportId());
            queueService.checkTestPlanCaseTestEnd(dto.getTestId(), dto.getRunMode(), dto.getTestPlanReportId());
        } catch (Exception e) {
            LoggerUtil.error("回退报告异常", request.getReportId(), e);
        } finally {
            ApiLocalRunner.clearCache(request.getReportId());
        }
    }

    public void updateReport(JmeterRunRequestDTO request, String errorMsg) {
        ResultDTO dto = new ResultDTO();
        BeanUtils.copyBean(dto, request);
        dto.setQueueId(request.getQueueId());
        dto.setTestId(request.getTestId());
        dto.setErrorEnded(true);
        String consoleMsg = FixedCapacityUtil.getJmeterLogger(dto.getReportId(), true);
        dto.setConsole(consoleMsg + StringUtils.LF + errorMsg);
        testResultService.testEnded(dto);
    }

    public void testEnded(JmeterRunRequestDTO request, String errorMsg) {
        updateReport(request, errorMsg);
        queueNext(request, errorMsg);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateApiReport(ApiExecutionQueueDetail queue, JmeterRunRequestDTO runRequest) {
        ApiDefinitionExecResultWithBLOBs execResult = apiDefinitionExecResultMapper.selectByPrimaryKey(queue.getReportId());
        if (execResult != null) {
            if (MapUtils.isNotEmpty(runRequest.getExtendedParameters())) {
                runRequest.getExtendedParameters().put(CommonConstants.USER_ID, execResult.getUserId());
            } else {
                runRequest.setExtendedParameters(new HashMap<>() {{
                    this.put(CommonConstants.USER_ID, execResult.getUserId());
                }});
            }
            execResult.setStartTime(System.currentTimeMillis());
            execResult.setStatus(ApiReportStatus.RUNNING.name());
            apiDefinitionExecResultMapper.updateByPrimaryKeySelective(execResult);
            LoggerUtil.info("进入串行模式，准备执行资源：[" + execResult.getName() + " ]", execResult.getId());
        }
    }

    /**
     * 更新报告状态
     *
     * @param queue
     * @param runRequest
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateScenarioReport(ApiExecutionQueueDetail queue, JmeterRunRequestDTO runRequest) {
        if (!GenerateHashTreeUtil.isSetReport(runRequest.getReportType()) &&
                StringUtils.equalsAny(runRequest.getRunMode(),
                        ApiRunMode.SCENARIO.name(),
                        ApiRunMode.SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO.name(),
                        ApiRunMode.JENKINS_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_SCENARIO.name(),
                        ApiRunMode.UI_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_SCHEDULE_SCENARIO.name(),
                        ApiRunMode.UI_SCHEDULE_SCENARIO_PLAN.name())
        ) {
            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(queue.getReportId());
            if (report != null) {
                report.setStatus(ApiReportStatus.RUNNING.name());
                report.setCreateTime(System.currentTimeMillis());
                report.setUpdateTime(System.currentTimeMillis());
                runRequest.setExtendedParameters(new HashMap<>() {{
                    this.put(CommonConstants.USER_ID, report.getCreateUser());
                }});
                apiScenarioReportMapper.updateByPrimaryKey(report);
                LoggerUtil.info("进入串行模式，准备执行资源：[ " + report.getName() + " ]", report.getId());
            }
        }
    }

}
