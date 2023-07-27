package io.metersphere.plan.service;

import io.metersphere.base.domain.ShareInfo;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.base.domain.TestPlanReportContentWithBLOBs;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.HttpHeaderUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.plan.dto.TestPlanReportDataStruct;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.BaseShareInfoService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestPlanMessageService {
    @Resource
    private BaseProjectService baseProjectService;
    @Lazy
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseShareInfoService baseShareInfoService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Lazy
    @Resource
    private TestPlanReportService testPlanReportService;

    @Async
    public void checkTestPlanStatusAndSendMessage(TestPlanReport report, TestPlanReportContentWithBLOBs testPlanReportContent, TestPlan testPlan, boolean sendMessage) {
        if (report != null && testPlanReportContent != null) {
            try {
                report = testPlanReportService.checkTestPlanReportHasErrorCase(report, testPlanReportContent);
                if (!report.getIsApiCaseExecuting() && !report.getIsPerformanceExecuting() && !report.getIsScenarioExecuting() && !report.getIsUiScenarioExecuting()) {
                    if (sendMessage && testPlan != null && StringUtils.equalsAny(report.getTriggerMode(),
                            ReportTriggerMode.MANUAL.name(),
                            ReportTriggerMode.API.name(),
                            ReportTriggerMode.SCHEDULE.name()) && !StringUtils.equalsIgnoreCase(report.getStatus(), ExecuteResult.TEST_PLAN_RUNNING.toString())
                    ) {
                        //发送通知
                        this.sendMessage(testPlan, report, testPlan.getProjectId());
                    }
                }
            } catch (Exception e) {
                LogUtil.error("检查测试计划状态出错", e);
            }

        }
    }

    private void sendMessage(TestPlan testPlan, TestPlanReport testPlanReport, String projectId) {
        assert testPlan != null;
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/track/testPlan/reportList";
        String subject;
        String successContext = "${operator}执行的 ${name} 测试计划运行成功, 报告: ${planShareUrl}";
        String failedContext = "${operator}执行的 ${name} 测试计划运行失败, 报告: ${planShareUrl}";
        String context = "${operator}完成了测试计划: ${name}, 报告: ${planShareUrl}";
        if (StringUtils.equals(testPlanReport.getTriggerMode(), ReportTriggerMode.API.name())) {
            subject = "Jenkins任务通知";
        } else {
            subject = "任务通知";
        }
        String creator = testPlanReport.getCreator();
        UserDTO userDTO = baseUserService.getUserDTO(creator);
        HttpHeaderUtils.runAsUser(userDTO);
        // 计算通过率
        TestPlanDTOWithMetric testPlanDTOWithMetric = BeanUtils.copyBean(new TestPlanDTOWithMetric(), testPlan);
        testPlanService.calcTestPlanRate(Collections.singletonList(testPlanDTOWithMetric));
        // 计算各种属性
        TestPlanReportDataStruct report = testPlanReportService.getReport(testPlanReport.getId());
        HttpHeaderUtils.clearUser();
        Map<String, Long> caseCountMap = calculateCaseCount(report);
        Map paramMap = new HashMap();
        paramMap.put("type", "testPlan");
        paramMap.put("url", url);
        paramMap.put("projectId", projectId);
        if (userDTO != null) {
            paramMap.put("operator", userDTO.getName());
            paramMap.put("executor", userDTO.getId());
        }

        // 执行率 通过率 两位小数
        if (report.getPassRate() != null && !report.getPassRate().isNaN()) {
            paramMap.put("passRate", String.format("%.2f", report.getPassRate() * 100));
        }
        if (report.getExecuteRate() != null && !report.getExecuteRate().isNaN()) {
            paramMap.put("executeRate", String.format("%.2f", report.getExecuteRate() * 100));
        }

        paramMap.putAll(caseCountMap);
        paramMap.putAll(new BeanMap(testPlanDTOWithMetric));

        String testPlanShareUrl = getTestPlanShareUrl(testPlanReport.getId(), creator);
        // 分享经过网关需要带上前缀
        paramMap.put("planShareUrl", baseSystemConfigDTO.getUrl() + "/track/share-plan-report" + testPlanShareUrl);
        handleSpecialValues(paramMap);

        /**
         * 测试计划的消息通知配置包括 完成、成功、失败
         * 所以发送通知时必定会有"完成"状态的通知
         */
        Map<String, String> execStatusEventMap = new HashMap<>();
        execStatusEventMap.put(TestPlanReportStatus.COMPLETED.name(), NoticeConstants.Event.COMPLETE);
        if (StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.SUCCESS.name())) {
            execStatusEventMap.put(testPlanReport.getStatus(), NoticeConstants.Event.EXECUTE_SUCCESSFUL);
        } else if (StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.FAILED.name())) {
            execStatusEventMap.put(testPlanReport.getStatus(), NoticeConstants.Event.EXECUTE_FAILED);
        } else if (!StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.COMPLETED.name())) {
            execStatusEventMap.put(testPlanReport.getStatus(), NoticeConstants.Event.COMPLETE);
        }
        for (Map.Entry<String, String> entry : execStatusEventMap.entrySet()) {
            String status = entry.getKey();
            String event = entry.getValue();
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(creator)
                    .context(context)
                    .successContext(successContext)
                    .failedContext(failedContext)
                    .testId(testPlan.getId())
                    .status(status)
                    .event(event)
                    .subject(subject)
                    .paramMap(paramMap)
                    .build();

            if (StringUtils.equals(testPlanReport.getTriggerMode(), ReportTriggerMode.MANUAL.name())) {
                noticeSendService.send(baseProjectService.getProjectById(projectId), NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
            }

            if (StringUtils.equalsAny(testPlanReport.getTriggerMode(), ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())) {
                noticeSendService.send(testPlanReport.getTriggerMode(), NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
            }
        }
    }

    public String getTestPlanShareUrl(String testPlanReportId, String userId) {
        ShareInfo shareRequest = new ShareInfo();
        shareRequest.setCustomData(testPlanReportId);
        shareRequest.setShareType(ShareType.PLAN_DB_REPORT.name());
        shareRequest.setCreateUserId(userId);
        ShareInfo shareInfo = baseShareInfoService.generateShareInfo(shareRequest);
        return baseShareInfoService.conversionShareInfoToDTO(shareInfo).getShareUrl();
    }

    private void handleSpecialValues(Map paramMap) {
        // 翻译${stage}占位符
        String stageKey = "stage";
        if (paramMap.containsKey(stageKey) && paramMap.get(stageKey) != null) {
            paramMap.put(stageKey, StatusReference.statusMap.get(paramMap.get(stageKey).toString()));
        }
        // 翻译${status}占位符
        String statusKey = "status";
        if (paramMap.containsKey(statusKey) && paramMap.get(statusKey) != null) {
            paramMap.put(statusKey, StatusReference.statusMap.get(paramMap.get(statusKey).toString()));
        }
    }

    private Map<String, Long> calculateCaseCount(TestPlanReportDataStruct report) {
        Map<String, Long> result = new HashMap<>();
        // 功能用例
        result.put("functionAllCount", 0L);
        result.put("functionPreparedCount", 0L);
        result.put("functionSuccessCount", 0L);
        result.put("functionFailedCount", 0L);
        result.put("functionBlockedCount", 0L);
        result.put("functionSkippedCount", 0L);
        //
        result.put("apiCaseSuccessCount", 0L);
        result.put("apiCaseFailedCount", 0L);
        result.put("apiCaseUnExecuteCount", 0L);
        result.put("apiCaseErrorReportCount", 0L);
        result.put("apiCaseAllCount", 0L);
        //
        result.put("apiScenarioSuccessCount", 0L);
        result.put("apiScenarioFailedCount", 0L);
        result.put("apiScenarioUnExecuteCount", 0L);
        result.put("apiScenarioErrorReportCount", 0L);
        result.put("apiScenarioAllCount", 0L);
        //
        result.put("uiScenarioSuccessCount", 0L);
        result.put("uiScenarioFailedCount", 0L);
        result.put("uiScenarioUnExecuteCount", 0L);
        result.put("uiScenarioAllCount", 0L);
        //
        result.put("loadCaseAllCount", 0L);
        result.put("caseCount", report.getCaseCount());
        //
        result.put("reportStartTime", report.getStartTime());
        result.put("reportEndTime", report.getEndTime());


        List<TestPlanCaseDTO> functionAllCases = report.getFunctionAllCases();
        if (CollectionUtils.isNotEmpty(functionAllCases)) {
            Map<String, Long> functionCountMap = functionAllCases.stream().collect(Collectors.groupingBy(TestPlanCaseDTO::getStatus, Collectors.counting()));
            // ["Prepare", "Pass", "Failure", "Blocking", "Skip"]
            functionCountMap.forEach((k, v) -> {
                switch (k) {
                    case "Prepare":
                        result.put("functionPreparedCount", v);
                        break;
                    case "Pass":
                        result.put("functionSuccessCount", v);
                        break;
                    case "Failure":
                        result.put("functionFailedCount", v);
                        break;
                    case "Blocking":
                        result.put("functionBlockedCount", v);
                        break;
                    case "Skip":
                        result.put("functionSkippedCount", v);
                        break;
                    default:
                        break;
                }
            });
            result.put("functionAllCount", (long) functionAllCases.size());
        }

        List<TestPlanApiDTO> apiAllCases = report.getApiAllCases();
        if (CollectionUtils.isNotEmpty(apiAllCases)) {
            Map<String, Long> apiCountMap = apiAllCases.stream()
                    .collect(Collectors.groupingBy(plan -> StringUtils.isEmpty(plan.getExecResult()) ? "default" : plan.getExecResult().toLowerCase(), Collectors.counting()));
            // ["success", "error", "default", "errorReportResult"]
            apiCountMap.forEach((k, v) -> {
                switch (k) {
                    case "success":
                        result.put("apiCaseSuccessCount", v);
                        break;
                    case "error":
                        result.put("apiCaseFailedCount", v);
                        break;
                    case "default":
                    case "pending":
                    case "stopped":
                    case "timeout":
                        result.put("apiCaseUnExecuteCount", v);
                        break;
                    case "errorreportresult":
                    case "fakeerror":
                    case "fake_error":
                        result.put("apiCaseErrorReportCount", v);
                        break;
                    default:
                        break;
                }
            });
            result.put("apiCaseAllCount", (long) apiAllCases.size());
        }

        List<TestPlanScenarioDTO> scenarioAllCases = report.getScenarioAllCases();
        if (CollectionUtils.isNotEmpty(scenarioAllCases)) {
            Map<String, Long> scenarioCountMap = scenarioAllCases.stream()
                    .collect(Collectors.groupingBy(plan -> StringUtils.isEmpty(plan.getLastResult()) ? "unexecute" : plan.getLastResult().toLowerCase(), Collectors.counting()));
            // ["Fail", "Success", "unexecute", "errorReportResult"]
            scenarioCountMap.forEach((k, v) -> {
                switch (k) {
                    case "success":
                        result.put("apiScenarioSuccessCount", v);
                        break;
                    case "fail":
                    case "error":
                        result.put("apiScenarioFailedCount", v);
                        break;
                    case "unexecute":
                    case "pending":
                    case "stopped":
                    case "timeout":
                        result.put("apiScenarioUnExecuteCount", v);
                        break;
                    case "errorreportresult":
                    case "fakeerror":
                    case "fake_error":
                        result.put("apiScenarioErrorReportCount", v);
                        break;
                    default:
                        break;
                }
            });
            result.put("apiScenarioAllCount", (long) scenarioAllCases.size());
        }

        List<TestPlanUiScenarioDTO> uiAllCases = report.getUiAllCases();
        if (CollectionUtils.isNotEmpty(uiAllCases)) {
            Map<String, Long> uiCountMap = uiAllCases.stream()
                    .collect(Collectors.groupingBy(plan -> StringUtils.isEmpty(plan.getLastResult()) ? "unexecute" : plan.getLastResult().toLowerCase(), Collectors.counting()));
            uiCountMap.forEach((k, v) -> {
                switch (k) {
                    case "success":
                        result.put("uiScenarioSuccessCount", v);
                        break;
                    case "fail":
                    case "error":
                        result.put("uiScenarioFailedCount", v);
                        break;
                    case "unexecute":
                    case "stop":
                    case "pending":
                        result.put("uiScenarioUnExecuteCount", v);
                        break;
                    default:
                        break;
                }
            });
            result.put("uiScenarioAllCount", (long) uiAllCases.size());
        }

        if (CollectionUtils.isNotEmpty(report.getLoadAllCases())) {
            result.put("loadCaseAllCount", (long) report.getLoadAllCases().size());
        }

        return result;
    }
}
