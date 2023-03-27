package io.metersphere.plan.service;

import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.domain.TestPlanUiScenario;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanUiCaseMapper;
import io.metersphere.dto.TestPlanCaseStatusDTO;
import io.metersphere.dto.TestPlanLoadCaseStatusDTO;
import io.metersphere.utils.JsonUtils;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.websocket.LoadCaseStatusHandleSocket;
import io.metersphere.websocket.UICaseStatusHandleSocket;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class AutomationCaseExecOverService {

    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private ExtTestPlanUiCaseMapper extTestPlanUiCaseMapper;
    @Resource
    private TestCaseSyncStatusService testCaseSyncStatusService;

    public void automationCaseExecOver(String testId) {
        TestPlanApiCase testPlanApiCase = null;
        TestPlanApiScenario testPlanApiScenario = null;
        TestPlanLoadCase testPlanLoadCase = null;
        TestPlanUiScenario testPlanUiScenario = null;

        testPlanApiCase = extTestPlanApiCaseMapper.selectBaseInfoById(testId);
        if (testPlanApiCase == null) {
            testPlanApiScenario = extTestPlanScenarioCaseMapper.selectBaseInfoById(testId);
        }
        if (ObjectUtils.allNull(testPlanApiCase, testPlanApiScenario)) {
            testPlanLoadCase = extTestPlanLoadCaseMapper.selectBaseInfoById(testId);
        }
        if (ObjectUtils.allNull(testPlanApiCase, testPlanApiScenario, testPlanLoadCase)) {
            testPlanUiScenario = extTestPlanUiCaseMapper.selectBaseInfoById(testId);
        }

        String automationCaseId = null, planId = null;
        String triggerCaseExecResult = null;
        if (testPlanApiCase != null) {
            automationCaseId = testPlanApiCase.getApiCaseId();
            planId = testPlanApiCase.getTestPlanId();
            triggerCaseExecResult = testPlanApiCase.getStatus();
        } else if (testPlanApiScenario != null) {
            automationCaseId = testPlanApiScenario.getApiScenarioId();
            planId = testPlanApiScenario.getTestPlanId();
            triggerCaseExecResult = testPlanApiScenario.getLastResult();
        } else if (testPlanLoadCase != null) {
            automationCaseId = testPlanLoadCase.getLoadCaseId();
            planId = testPlanLoadCase.getTestPlanId();
            triggerCaseExecResult = testPlanLoadCase.getStatus();
        } else if (testPlanUiScenario != null) {
            automationCaseId = testPlanUiScenario.getUiScenarioId();
            planId = testPlanUiScenario.getTestPlanId();
            triggerCaseExecResult = testPlanUiScenario.getLastResult();
        }

        if (StringUtils.isNoneEmpty(automationCaseId, planId, triggerCaseExecResult)) {
            //检查是否需要自动更新功能用例状态
            testCaseSyncStatusService.updateFunctionCaseStatusByAutomationCaseId(automationCaseId, planId, triggerCaseExecResult);
        }
        if (testPlanUiScenario != null) {
            try {
                //UI执行完成发送Socket
                UICaseStatusHandleSocket.sendMessageSingle(planId, JsonUtils.toJSONString(TestPlanCaseStatusDTO.builder().planCaseId(testPlanUiScenario.getId()).planCaseStatus(triggerCaseExecResult)));
            } catch (Exception e) {
                LoggerUtil.error("ui执行完成发送socket失败！", e);
            }
        }
        if (testPlanLoadCase != null) {
            try {
                String reportStatus = extTestPlanLoadCaseMapper.queryReportStatus(testPlanLoadCase.getLoadReportId());
                TestPlanLoadCaseStatusDTO loadCaseStatusDTO = TestPlanLoadCaseStatusDTO
                        .builder()
                        .planCaseId(testPlanLoadCase.getId())
                        .planCaseStatus(triggerCaseExecResult)
                        .planCaseReportId(testPlanLoadCase.getLoadReportId())
                        .planCaseReportStatus(reportStatus)
                        .build();
                LoadCaseStatusHandleSocket.sendMessageSingle(planId, JsonUtils.toJSONString(loadCaseStatusDTO));
            } catch (Exception e) {
                LoggerUtil.error("load执行完成发送socket失败！", e);
            }
        }
    }
}
