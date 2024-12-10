package io.metersphere.service;

import io.metersphere.api.dto.automation.ApiTestReportVariable;
import io.metersphere.api.exec.scenario.ApiEnvironmentRunningParamService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.plan.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.scenario.ApiScenarioExecutionInfoService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.service.scenario.ApiScenarioReportStructureService;
import io.metersphere.service.scenario.ApiScenarioService;
import io.metersphere.utils.ReportStatusUtil;
import io.metersphere.vo.ResultVO;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TestResultService {
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioService apiAutomationService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;
    @Resource
    private ApiScenarioExecutionInfoService scenarioExecutionInfoService;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private BaseShareInfoService baseShareInfoService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;

    // 场景
    private static final List<String> scenarioRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.SCENARIO.name());
        this.add(ApiRunMode.SCENARIO_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_SCENARIO.name());
        this.add(ApiRunMode.JENKINS_SCENARIO_PLAN.name());
    }};

    private static final List<String> planRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.SCENARIO_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name());
        this.add(ApiRunMode.JENKINS_SCENARIO_PLAN.name());
    }};

    private static final List<String> apiRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.DEFINITION.name());
        this.add(ApiRunMode.API_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_API_PLAN.name());
    }};

    /**
     * 批量存储来自NODE/K8s的执行结果
     */
    public void batchSaveResults(Map<String, List<ResultDTO>> resultDtoMap) {
        // 遍历 resultDtoMap
        resultDtoMap.forEach((key, resultDTOS) -> {
            resultDTOS.forEach(dto -> {
                // 处理环境参数
                Optional.ofNullable(dto.getArbitraryData())
                        .map(data -> (List<String>) data.get("ENV"))
                        .filter(CollectionUtils::isNotEmpty)
                        .ifPresent(apiEnvironmentRunningParamService::parseEnvironment);

                // 处理集合报告的 console 日志
                if (StringUtils.isNotEmpty(dto.getConsole()) &&
                        StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {

                    String reportId = StringUtils.equalsIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name())
                            ? dto.getTestPlanReportId()
                            : dto.getReportId();

                    apiScenarioReportStructureService.update(reportId, dto.getConsole(), true);
                }
            });

            // 根据任务类型同步保存结果
            if (StringUtils.equalsAny(key, "schedule-task", "api-test-case-task")) {
                apiDefinitionExecResultService.batchSaveApiResult(resultDTOS);
            } else if (StringUtils.equalsAny(key, "api-scenario-task")) {
                apiScenarioReportService.batchSaveResult(resultDTOS);
            }
        });
    }

    private ApiScenarioReport editReport(ResultDTO dto) {
        // 更新报告状态
        ResultVO resultVO = ReportStatusUtil.computedProcess(dto);
        ApiScenarioReport report = apiScenarioReportService.editReport(dto.getReportType(), dto.getReportId(), resultVO.getStatus(), dto.getRunMode());

        // 更新场景状态
        ApiScenarioWithBLOBs scenario = Optional.ofNullable(apiScenarioMapper.selectByPrimaryKey(dto.getTestId()))
                .orElseGet(() -> apiScenarioMapper.selectByPrimaryKey(report.getScenarioId()));

        if (scenario != null) {
            scenario.setLastResult(resultVO.getStatus());
            scenario.setPassRate(resultVO.computerPassRate());
            scenario.setReportId(dto.getReportId());
            scenario.setExecuteTimes(Optional.ofNullable(scenario.getExecuteTimes()).orElse(0) + 1);

            apiScenarioMapper.updateByPrimaryKey(scenario);
        }

        // 发送通知
        if (scenario != null && report != null && !"Debug".equals(report.getExecuteType())) {
            apiScenarioReportService.sendNotice(scenario, report);
        }

        return report;
    }

    public ApiScenarioReport edit(ResultDTO dto) {
        // 更新控制台信息
        if (!RunModeConstants.SET_REPORT.toString().equals(dto.getReportType())) {
            apiScenarioReportStructureService.update(dto.getReportId(), dto.getConsole(), false);
        }

        // 根据运行模式选择更新逻辑
        return switch (dto.getRunMode()) {
            case "SCENARIO_PLAN" -> apiScenarioReportService.updatePlanCase(dto);
            case "SCHEDULE_SCENARIO_PLAN", "JENKINS_SCENARIO_PLAN" ->
                    apiScenarioReportService.updateSchedulePlanCase(dto);
            default -> this.editReport(dto);
        };
    }


    public void testEnded(ResultDTO dto) {
        if (dto.getRequestResults() == null) {
            dto.setRequestResults(new LinkedList<>());
        }

        if (scenarioRunModes.contains(dto.getRunMode())) {
            ApiScenarioReport scenarioReport = edit(dto);
            if (scenarioReport == null) {
                return;
            }

            // 执行人
            String userName = apiAutomationService.getUser(scenarioReport.getUserId());
            // 初始化负责人和环境
            String principal;
            String environment = StringUtils.EMPTY;

            ApiScenarioWithBLOBs apiScenario = fetchScenario(dto, scenarioReport);
            if (apiScenario != null) {
                scenarioExecutionInfoService.insertScenarioInfo(apiScenario, scenarioReport, dto);
                environment = apiScenarioReportService.getEnvironment(apiScenario);
                principal = apiAutomationService.getUser(apiScenario.getPrincipal());
            } else {
                // 无场景时负责人取当前用户
                principal = apiAutomationService.getUser(scenarioReport.getUserId());
            }

            // 构建报告内容
            ApiTestReportVariable reportTask = buildReportTask(scenarioReport, userName, principal, environment);
            if (isTaskReport(reportTask.getTriggerMode())) {
                sendTask(reportTask, dto.getTestId());
            }
        } else if (apiRunModes.contains(dto.getRunMode()) && BooleanUtils.isTrue(dto.getErrorEnded())) {
            // 只处理 RUNNING 状态的执行报告
            updateRunningResult(dto);
        }
    }

    private ApiScenarioWithBLOBs fetchScenario(ResultDTO dto, ApiScenarioReport scenarioReport) {
        if (planRunModes.contains(dto.getRunMode())) {
            TestPlanApiScenario testPlanApiScenario = Optional.ofNullable(
                            testPlanApiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId()))
                    .orElseGet(() -> testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId()));

            if (testPlanApiScenario != null) {
                return apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
            }
        } else {
            return apiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId());
        }
        return null;
    }

    private ApiTestReportVariable buildReportTask(ApiScenarioReport scenarioReport, String userName, String principal, String environment) {
        ApiTestReportVariable reportTask = new ApiTestReportVariable();
        reportTask.setStatus(scenarioReport.getStatus());
        reportTask.setId(scenarioReport.getId());
        reportTask.setTriggerMode(scenarioReport.getTriggerMode());
        reportTask.setName(scenarioReport.getName());
        reportTask.setExecutor(userName);
        reportTask.setUserId(scenarioReport.getUserId());
        reportTask.setPrincipal(principal);
        reportTask.setExecutionTime(DateUtils.getTimeString(scenarioReport.getUpdateTime()));
        reportTask.setEnvironment(environment);
        reportTask.setProjectId(scenarioReport.getProjectId());
        return reportTask;
    }

    private boolean isTaskReport(String triggerMode) {
        return StringUtils.equalsAny(triggerMode, ReportTriggerMode.API.name(), ReportTriggerMode.SCHEDULE.name());
    }


    private void updateRunningResult(ResultDTO dto) {
        ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(dto.getReportId());
        if (result != null && StringUtils.equals(ApiReportStatus.RUNNING.name(), result.getStatus())) {
            result.setStatus(ApiReportStatus.PENDING.name());
            RequestResult item = new RequestResult();
            ResponseResult responseResult = new ResponseResult();
            responseResult.setConsole(dto.getConsole());
            item.setResponseResult(responseResult);
            result.setContent(JSON.toJSONString(item));

            apiDefinitionExecResultMapper.updateByPrimaryKeyWithBLOBs(result);
            if (StringUtils.equals(dto.getRunMode(), ApiRunMode.API_PLAN.name())) {
                TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(dto.getTestId());
                if (testPlanApiCase != null) {
                    testPlanApiCase.setStatus(result.getStatus());
                    testPlanApiCaseMapper.updateByPrimaryKey(testPlanApiCase);
                }
            } else if (StringUtils.isNotEmpty(dto.getTestId())) {
                ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                apiTestCase.setLastResultId(dto.getReportId());
                apiTestCase.setId(dto.getTestId());
                apiTestCase.setStatus(result.getStatus());
                apiTestCaseService.updateByPrimaryKeySelective(apiTestCase);
            }
        }
    }

    private void sendTask(ApiTestReportVariable report, String testId) {
        if (report == null) {
            return;
        }
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + report.getId();

        String subject = StringUtils.EMPTY;
        String event = StringUtils.EMPTY;
        String successContext = "${operator}执行接口自动化成功: ${name}" + ", 报告: ${reportUrl}";
        String failedContext = "${operator}执行接口自动化失败: ${name}" + ", 报告: ${reportUrl}";
        String shareUrl = getScenarioShareUrl(report.getId(), report.getUserId());
        if (StringUtils.equals(ReportTriggerMode.API.name(), report.getTriggerMode())) {
            subject = "Jenkins任务通知";
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), report.getTriggerMode())) {
            subject = "任务通知";
        }
        if (StringUtils.equalsIgnoreCase(ApiReportStatus.SUCCESS.name(), report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (StringUtils.equalsIgnoreCase(ApiReportStatus.ERROR.name(), report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(testId);
        Map paramMap = new HashMap<>();
        paramMap.put(PropertyConstant.TYPE, "api");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("reportUrl", reportUrl);
        paramMap.put("operator", report.getExecutor());
        paramMap.put("scenarioShareUrl", baseSystemConfigDTO.getUrl() + "/api/share-api-report" + shareUrl);
        paramMap.putAll(new BeanMap(report));
        paramMap.putAll(new BeanMap(scenario));
        NoticeModel noticeModel = NoticeModel.builder().operator(report.getUserId()).successContext(successContext).failedContext(failedContext).testId(testId).status(report.getStatus()).event(event).subject(subject).paramMap(paramMap).build();
        noticeSendService.send(report.getTriggerMode(), NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public String getScenarioShareUrl(String scenarioReportId, String userId) {
        ShareInfo shareRequest = new ShareInfo();
        shareRequest.setCustomData(scenarioReportId);
        shareRequest.setShareType(ShareType.API_REPORT.name());
        shareRequest.setCreateUserId(userId);
        ShareInfo shareInfo = baseShareInfoService.generateShareInfo(shareRequest);
        return baseShareInfoService.conversionShareInfoToDTO(shareInfo).getShareUrl();
    }
}
