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
        // 处理环境
        List<String> environmentList = new LinkedList<>();
        for (String key : resultDtoMap.keySet()) {
            List<ResultDTO> resultDTOS = resultDtoMap.get(key);
            for (ResultDTO dto : resultDTOS) {
                if (dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("ENV")) {
                    environmentList = (List<String>) dto.getArbitraryData().get("ENV");
                }
                //处理环境参数
                if (CollectionUtils.isNotEmpty(environmentList)) {
                    apiEnvironmentRunningParamService.parseEnvironment(environmentList);
                }
                // 处理集合报告的console日志
                if (StringUtils.isNotEmpty(dto.getConsole()) && StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    String reportId = dto.getReportId();
                    if (StringUtils.equalsIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name())) {
                        reportId = dto.getTestPlanReportId();
                    }
                    apiScenarioReportStructureService.update(reportId, dto.getConsole(), true);
                }
            }
            //测试计划定时任务-接口执行逻辑的话，需要同步测试计划的报告数据
            if (StringUtils.equalsAny(key, "schedule-task", "api-test-case-task")) {
                apiDefinitionExecResultService.batchSaveApiResult(resultDTOS);
            } else if (StringUtils.equalsAny(key, "api-scenario-task")) {
                apiScenarioReportService.batchSaveResult(resultDTOS);
            }
        }
    }

    private ApiScenarioReport editReport(ResultDTO dto) {
        // 更新报告状态
        ResultVO resultVO = ReportStatusUtil.computedProcess(dto);
        ApiScenarioReport report = apiScenarioReportService.editReport(dto.getReportType(), dto.getReportId(), resultVO.getStatus(), dto.getRunMode());
        // 更新场景状态
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (scenario == null) {
            scenario = apiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
        }
        if (scenario != null) {
            scenario.setLastResult(resultVO.getStatus());
            scenario.setPassRate(resultVO.computerPassRate());
            scenario.setReportId(dto.getReportId());
            int executeTimes = 0;
            if (scenario.getExecuteTimes() != null) {
                executeTimes = scenario.getExecuteTimes().intValue();
            }
            scenario.setExecuteTimes(executeTimes + 1);
            apiScenarioMapper.updateByPrimaryKey(scenario);
        }

        // 发送通知
        if (scenario != null && report != null && !StringUtils.equals(report.getExecuteType(), "Debug")) {
            apiScenarioReportService.sendNotice(scenario, report);
        }
        return report;
    }

    public ApiScenarioReport edit(ResultDTO dto) {
        if (!StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            // 更新控制台信息
            apiScenarioReportStructureService.update(dto.getReportId(), dto.getConsole(), false);
        }
        if (StringUtils.equals(dto.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
            return apiScenarioReportService.updatePlanCase(dto);
        } else if (StringUtils.equalsAny(dto.getRunMode(),
                ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            return apiScenarioReportService.updateSchedulePlanCase(dto);
        } else {
            return this.editReport(dto);
        }
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

            String environment = StringUtils.EMPTY;
            //执行人
            String userName = apiAutomationService.getUser(scenarioReport.getUserId());
            //负责人
            String principal = StringUtils.EMPTY;

            if (planRunModes.contains(dto.getRunMode())) {
                TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId());
                if (testPlanApiScenario == null) {
                    //测试计划-场景列表中，批量/单独执行场景时，关联ID记录在testID中
                    testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(dto.getTestId());
                }
                if (testPlanApiScenario != null) {
                    ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
                    if (apiScenario != null) {
                        scenarioExecutionInfoService.insertScenarioInfo(apiScenario, scenarioReport, dto);
                        environment = apiScenarioReportService.getEnvironment(apiScenario);
                        principal = apiAutomationService.getUser(apiScenario.getPrincipal());
                    }
                }
            } else {
                ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId());
                //集合报告查不出场景。场景的执行次数统计是在margeReport函数中进行的。所以这里要进行一个判断。
                if (apiScenario != null) {
                    scenarioExecutionInfoService.insertScenarioInfo(apiScenario, scenarioReport, dto);
                    environment = apiScenarioReportService.getEnvironment(apiScenario);
                    principal = apiAutomationService.getUser(apiScenario.getPrincipal());
                } else {
                    //负责人取当前负责人
                    principal = apiAutomationService.getUser(scenarioReport.getUserId());
                }
            }

            //报告内容
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

            if (reportTask != null) {
                if (StringUtils.equals(ReportTriggerMode.API.name(), reportTask.getTriggerMode())
                        || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), reportTask.getTriggerMode())) {
                    sendTask(reportTask, dto.getTestId());
                }
            }
        } else if (apiRunModes.contains(dto.getRunMode()) && BooleanUtils.isTrue(dto.getErrorEnded())) {
            // 只处理RUNNING中的执行报告
            updateRunningResult(dto);
        }
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
