package io.metersphere.service;

import io.metersphere.api.dto.automation.ApiTestReportVariable;
import io.metersphere.api.exec.scenario.ApiEnvironmentRunningParamService;
import io.metersphere.api.jmeter.utils.ReportStatusUtil;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.vo.ResultVO;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.scenario.ApiScenarioExecutionInfoService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.service.scenario.ApiScenarioReportStructureService;
import io.metersphere.service.scenario.ApiScenarioService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
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
    private RedisTemplateService redisTemplateService;
    @Resource
    private ApiScenarioExecutionInfoService scenarioExecutionInfoService;
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    BaseShareInfoService baseShareInfoService;

    // 场景
    private static final List<String> scenarioRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.SCENARIO.name());
        this.add(ApiRunMode.SCENARIO_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_SCENARIO.name());
        this.add(ApiRunMode.JENKINS_SCENARIO_PLAN.name());
    }};

    // 接口测试 用例/接口
    private static final List<String> caseRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.DEFINITION.name());
        this.add(ApiRunMode.JENKINS.name());
        this.add(ApiRunMode.API_PLAN.name());
    }};

    // 测试计划 用例/接口
    private static final List<String> planCaseRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.SCHEDULE_API_PLAN.name());
        this.add(ApiRunMode.JENKINS_API_PLAN.name());
        this.add(ApiRunMode.MANUAL_PLAN.name());
    }};

    /**
     * 执行结果存储
     *
     * @param dto 执行结果
     */
    public void saveResults(ResultDTO dto) {
        // 处理环境
        List<String> environmentList = new LinkedList<>();
        if (dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("ENV")) {
            environmentList = (List<String>) dto.getArbitraryData().get("ENV");
        }
        //处理环境参数
        if (CollectionUtils.isNotEmpty(environmentList)) {
            apiEnvironmentRunningParamService.parseEnvironment(environmentList);
        }

        // 测试计划用例触发结果处理
        if (planCaseRunModes.contains(dto.getRunMode())) {
            apiDefinitionExecResultService.saveApiResultByScheduleTask(dto);
        } else if (caseRunModes.contains(dto.getRunMode())) {
            // 手动触发/批量触发 用例结果处理
            apiDefinitionExecResultService.saveApiResult(dto);
        } else if (scenarioRunModes.contains(dto.getRunMode())) {
            // 场景报告结果处理
            apiScenarioReportService.saveResult(dto);
        }
    }

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
        if (scenario != null && report != null) {
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
        } else if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            return apiScenarioReportService.updateSchedulePlanCase(dto);
        } else {
            return this.editReport(dto);
        }
    }

    public void testEnded(ResultDTO dto) {
        // 删除串行资源锁
        if (StringUtils.equals(dto.getRunType(), RunModeConstants.SERIAL.toString())) {
            String key = StringUtils.join(RunModeConstants.SERIAL.name(), "_", dto.getReportId());
            redisTemplateService.delete(key);
        }
        if (dto.getRequestResults() == null) {
            dto.setRequestResults(new LinkedList<>());
        }
        if (scenarioRunModes.contains(dto.getRunMode())) {
            ApiScenarioReport scenarioReport = edit(dto);
            if (scenarioReport != null) {
                String environment = StringUtils.EMPTY;
                //执行人
                String userName = StringUtils.EMPTY;
                //负责人
                String principal = StringUtils.EMPTY;

                ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId());
                if (apiScenario != null) {
                    if (StringUtils.equalsAnyIgnoreCase(dto.getRunMode(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                        scenarioExecutionInfoService.insertExecutionInfo(dto.getTestId(), scenarioReport.getStatus(), scenarioReport.getTriggerMode(), scenarioReport.getProjectId() == null ? apiScenario.getProjectId() : scenarioReport.getProjectId(), ExecutionExecuteTypeEnum.TEST_PLAN.name(), apiScenario.getVersionId());
                    } else {
                        scenarioExecutionInfoService.insertExecutionInfo(scenarioReport.getScenarioId(), scenarioReport.getStatus(), scenarioReport.getTriggerMode(), scenarioReport.getProjectId() == null ? apiScenario.getProjectId() : scenarioReport.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name(), apiScenario.getVersionId());
                    }
                    environment = apiScenarioReportService.getEnvironment(apiScenario);
                    userName = apiAutomationService.getUser(apiScenario.getUserId());
                    principal = apiAutomationService.getUser(apiScenario.getPrincipal());
                } else {
                    TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId());
                    if (testPlanApiScenario != null) {
                        apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
                        if (apiScenario != null) {
                            scenarioExecutionInfoService.insertExecutionInfo(testPlanApiScenario.getId(), scenarioReport.getStatus(), scenarioReport.getTriggerMode(), scenarioReport.getProjectId() == null ? apiScenario.getProjectId() : scenarioReport.getProjectId(), ExecutionExecuteTypeEnum.TEST_PLAN.name(), apiScenario.getVersionId());
                        }
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
                    if (StringUtils.equals(ReportTriggerMode.API.name(), reportTask.getTriggerMode()) || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), reportTask.getTriggerMode())) {
                        sendTask(reportTask, dto.getTestId());
                    }
                }
            }
        } else if (StringUtils.equalsAnyIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name())) {
            ApiDefinitionExecResultWithBLOBs record = new ApiDefinitionExecResultWithBLOBs();
            record.setId(dto.getReportId());
            record.setStatus(ApiReportStatus.STOPPED.name());

            ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
            example.createCriteria().andIdEqualTo(dto.getReportId()).andStatusEqualTo(ApiReportStatus.RUNNING.name());
            apiDefinitionExecResultService.updateByExampleSelective(record, example);

            if (StringUtils.isNotEmpty(dto.getTestId())) {
                ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                apiTestCase.setLastResultId(dto.getReportId());
                apiTestCase.setId(dto.getTestId());
                apiTestCase.setStatus(record.getStatus());
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
