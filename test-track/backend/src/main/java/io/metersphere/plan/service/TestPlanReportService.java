package io.metersphere.plan.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.excel.constants.TestPlanTestCaseStatus;
import io.metersphere.i18n.Translator;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.request.QueryTestPlanRequest;
import io.metersphere.plan.request.TestPlanReportSaveRequest;
import io.metersphere.plan.request.api.ApiPlanReportRequest;
import io.metersphere.plan.request.api.TestPlanRunRequest;
import io.metersphere.plan.request.performance.LoadPlanReportDTO;
import io.metersphere.plan.request.ui.TestPlanUiExecuteReportDTO;
import io.metersphere.plan.request.ui.UiPlanReportRequest;
import io.metersphere.plan.service.remote.api.*;
import io.metersphere.plan.service.remote.performance.PlanTestPlanLoadCaseService;
import io.metersphere.plan.service.remote.ui.PlanTestPlanUiScenarioCaseService;
import io.metersphere.plan.utils.TestPlanReportUtil;
import io.metersphere.plan.utils.TestPlanRequestUtil;
import io.metersphere.request.report.QueryTestPlanReportRequest;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.IssuesService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.task.service.TaskService;
import io.metersphere.utils.BatchProcessingUtil;
import io.metersphere.utils.DiscoveryUtil;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xpack.track.dto.IssuesDao;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song.tianyang
 * 2021/1/8 4:34 下午
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanReportService {

    @Resource
    TestPlanReportMapper testPlanReportMapper;
    @Resource
    TestPlanReportDataMapper testPlanReportDataMapper;
    @Resource
    PlanTestPlanLoadCaseService planTestPlanLoadCaseService;
    @Resource
    ExtTestPlanReportMapper extTestPlanReportMapper;
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    ExtTestPlanMapper extTestPlanMapper;
    @Lazy
    @Resource
    TestPlanService testPlanService;
    @Resource
    TestPlanReportContentMapper testPlanReportContentMapper;
    @Resource
    ExtTestPlanReportContentMapper extTestPlanReportContentMapper;
    @Resource
    private TestPlanPrincipalMapper testPlanPrincipalMapper;
    @Resource
    private TaskService taskService;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private BaseEnvGroupProjectService baseEnvGroupProjectService;
    @Resource
    private PlanApiDefinitionExecResultService planApiDefinitionExecResultService;
    @Resource
    private PlanApiScenarioReportService planApiScenarioReportService;
    @Resource
    private PlanUiScenarioReportService planUiScenarioReportService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanExecutionQueueMapper testPlanExecutionQueueMapper;
    @Resource
    private TestPlanMessageService testPlanMessageService;
    @Resource
    private PlanTestPlanUiScenarioCaseService planTestPlanUiScenarioCaseService;
    @Resource
    private PlanTestPlanApiCaseService planTestPlanApiCaseService;
    @Resource
    private PlanTestPlanScenarioCaseService planTestPlanScenarioCaseService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;
    @Resource
    private BaseProjectService baseProjectService;
    @Lazy
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Lazy
    @Resource
    private IssuesService issuesService;
    @Resource
    private ApiExecutionQueueMapper apiExecutionQueueMapper;
    @Resource
    private ApiExecutionQueueDetailMapper apiExecutionQueueDetailMapper;
    @Resource
    private ExtApiExecutionQueueMapper extApiExecutionQueueMapper;

    private final String GROUP = "GROUP";

    //这个方法是消息通知时获取报告内容的。
    public List<TestPlanReport> getReports(List<String> reportIdList) {
        List<TestPlanReport> reportList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(reportIdList)) {
            TestPlanReportExample example = new TestPlanReportExample();
            example.createCriteria().andIdIn(reportIdList);
            reportList = testPlanReportMapper.selectByExample(example);
        }
        return reportList;
    }

    public List<TestPlanReportDTO> list(QueryTestPlanReportRequest request) {
        if (StringUtils.isBlank(request.getProjectId())) {
            return new ArrayList<>();
        } else {
            request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
            try {
                this.formatSelectRequest(request);
            } catch (Exception e) {
                LogUtil.error("格式化查询请求参数出错!", e);
            }

            /*
             2.8之前的逻辑中，会检查有没有查到成功率，没查到的话重新计算。 2.8之后成功率的计算和测试数据统计绑定在一起。
             以下是针对旧数据的处理：如果存在已完成但0成功率为0的报告，进行通过率的计算
             */
            List<TestPlanReportDTO> testPlanReportDTOList = extTestPlanReportMapper.list(request);
            for (TestPlanReportDTO testPlanReportDTO : testPlanReportDTOList) {
                if (StringUtils.equalsAnyIgnoreCase(
                        testPlanReportDTO.getStatus(),
                        TestPlanReportStatus.FAILED.name(),
                        TestPlanReportStatus.COMPLETED.name(),
                        TestPlanReportStatus.SUCCESS.name())
                        && (testPlanReportDTO.getPassRate() == null || testPlanReportDTO.getPassRate() == 0)) {
                    TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportDTO.getId());
                    TestPlanReportContentWithBLOBs content = this.selectTestPlanReportContentByReportId(testPlanReportDTO.getId());
                    TestPlanReportDataStruct testPlanReportCountData
                            = testPlanService.buildOldVersionTestPlanReport(testPlanReport, content);
                    if (testPlanReportCountData != null) {
                        testPlanReportDTO.setPassRate(testPlanReportCountData.getPassRate());
                    }
                }
            }
            return testPlanReportDTOList;
        }
    }

    private void formatSelectRequest(QueryTestPlanReportRequest request) {
        if (MapUtils.isNotEmpty(request.getCombine()) && request.getCombine().containsKey("status")) {
            if (request.getCombine().get("status") != null) {
                HashMap<String, Object> map = (HashMap<String, Object>) request.getCombine().get("status");
                List<String> statusFilterList = new ArrayList<>();
                ((List<String>) map.get("value")).forEach(item -> {
                    if (StringUtils.equalsAnyIgnoreCase(item, TestPlanReportStatus.COMPLETED.name())) {
                        CollectionUtils.addAll(statusFilterList, new String[]{
                                TestPlanReportStatus.COMPLETED.name(),
                                TestPlanReportStatus.FAILED.name(),
                                TestPlanReportStatus.SUCCESS.name()
                        });
                    } else if ("Underway".equals(item)) {
                        statusFilterList.add("Running");
                    } else {
                        statusFilterList.add("Starting");
                    }
                });
                map.put("status", statusFilterList);
            }
        }
    }

    public boolean hasRunningReport(String planId) {
        return extTestPlanReportContentMapper.hasRunningReport(planId);
    }

    public boolean hasRunningReport(List<String> planIds) {
        return extTestPlanReportContentMapper.hasRunningReportByPlanIds(planIds);
    }

    public TestPlanApiReportInfoDTO genApiReportInfoForSchedule(String planId, RunModeConfigDTO runModeConfigDTO) {
        TestPlanApiReportInfoDTO testPlanApiReportInfo = new TestPlanApiReportInfoDTO();
        Map<String, String> planApiCaseIdMap = new LinkedHashMap<>();
        Map<String, String> planScenarioIdMap = new LinkedHashMap<>();

        List<TestPlanApiScenarioInfoDTO> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiScenarioInfoDTO model : testPlanApiScenarioList) {
            planScenarioIdMap.put(model.getId(), model.getApiScenarioId());
        }
        List<TestPlanApiCaseInfoDTO> testPlanApiCaseList = extTestPlanApiCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiCaseInfoDTO model : testPlanApiCaseList) {
            planApiCaseIdMap.put(model.getId(), model.getApiCaseId());
        }

        testPlanApiReportInfo.setPlanApiCaseIdMap(planApiCaseIdMap);
        testPlanApiReportInfo.setPlanScenarioIdMap(planScenarioIdMap);
        //解析运行环境信息
        TestPlanReportRunInfoDTO runInfoDTO = this.parseTestPlanRunInfo(runModeConfigDTO, testPlanApiCaseList, testPlanApiScenarioList);
        testPlanApiReportInfo.setRunInfoDTO(runInfoDTO);
        return testPlanApiReportInfo;
    }

    public TestPlanReportRunInfoDTO parseTestPlanRunInfo(
            RunModeConfigDTO config,
            List<TestPlanApiCaseInfoDTO> cases,
            List<TestPlanApiScenarioInfoDTO> scenarios) {

        TestPlanReportRunInfoDTO runInfoDTO = new TestPlanReportRunInfoDTO();
        runInfoDTO.setRunMode(config.getMode());

        Map<String, List<String>> projectInvMap = TestPlanReportUtil.getTestPlanExecutedEnvironments(config.getTestPlanDefaultEnvMap(), config.getEnvMap());
        runInfoDTO.setRequestEnvMap(projectInvMap);

        final Map<String, String> runEnvMap = new HashMap<>();
        if (StringUtils.equals(GROUP, config.getEnvironmentType()) && StringUtils.isNotEmpty(config.getEnvironmentGroupId())) {
            Map<String, String> groupMap = baseEnvGroupProjectService.getEnvMap(config.getEnvironmentGroupId());
            if (MapUtils.isNotEmpty(groupMap)) {
                runEnvMap.putAll(groupMap);
            }
            runInfoDTO.setEnvGroupId(config.getEnvironmentGroupId());
        }
        // 场景环境处理
        scenarios.forEach(item -> {
            Map<String, String> envMap = null;
            if (StringUtils.equalsIgnoreCase(GROUP, item.getEnvironmentType())
                    && StringUtils.isNotEmpty(item.getEnvironmentGroupId())) {
                envMap = baseEnvGroupProjectService.getEnvMap(item.getEnvironmentGroupId());
            } else {
                if (MapUtils.isNotEmpty(runEnvMap) && runEnvMap.containsKey(item.getProjectId())) {
                    runInfoDTO.putScenarioRunInfo(item.getId(), item.getProjectId(), runEnvMap.get(item.getProjectId()));
                } else if (StringUtils.isNotEmpty(item.getEnvironment())) {
                    try {
                        envMap = JSON.parseObject(item.getEnvironment(), Map.class);
                    } catch (Exception e) {
                        LogUtil.error("解析场景环境失败!", e);
                    }
                }
            }
            if (MapUtils.isNotEmpty(envMap)) {
                for (Map.Entry<String, String> entry : envMap.entrySet()) {
                    String projectId = entry.getKey();
                    String envIdStr = entry.getValue();
                    runInfoDTO.putScenarioRunInfo(item.getId(), projectId, envIdStr);
                }
            }
        });

        // 用例环境处理
        cases.forEach(item -> {
            if (MapUtils.isNotEmpty(runEnvMap) && runEnvMap.containsKey(item.getProjectId())) {
                runInfoDTO.putApiCaseRunInfo(item.getId(), item.getProjectId(), runEnvMap.get(item.getProjectId()));
            } else {
                runInfoDTO.putApiCaseRunInfo(item.getId(), item.getProjectId(), item.getEnvironmentId());
            }
        });
        return runInfoDTO;
    }

    public TestPlanScheduleReportInfoDTO genTestPlanReportBySchedule(String planReportId, String planId, String userId, String triggerMode, RunModeConfigDTO runModeConfigDTO) {
        TestPlanReport testPlanReport = this.getTestPlanReport(planReportId);
        TestPlanScheduleReportInfoDTO returnDTO = new TestPlanScheduleReportInfoDTO();
        TestPlanReportRunInfoDTO runInfoDTO = null;
        if (testPlanReport != null) {
            returnDTO.setTestPlanReport(testPlanReport);
        }

        TestPlanReportSaveRequest saveRequest = new TestPlanReportSaveRequest.Builder()
                .setReportID(planReportId)
                .setPlanId(planId)
                .setUserId(userId)
                .setCountResources(false)
                .setTriggerMode(triggerMode)
                .build();

        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();

        if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
            // 这里不再调用API服务生成相关数据，否则当关联用例/场景量大时会超时
            TestPlanApiReportInfoDTO testPlanApiReportInfoDTO = this.genApiReportInfoForSchedule(planId, runModeConfigDTO);
            Map<String, String> planScenarioIdMap = testPlanApiReportInfoDTO.getPlanScenarioIdMap();
            Map<String, String> planApiCaseIdMap = testPlanApiReportInfoDTO.getPlanApiCaseIdMap();
            runInfoDTO = testPlanApiReportInfoDTO.getRunInfoDTO();

            saveRequest.setApiCaseIsExecuting(!planApiCaseIdMap.isEmpty());
            saveRequest.setScenarioIsExecuting(!planScenarioIdMap.isEmpty());
            saveRequest.setApiCaseIdMap(planApiCaseIdMap);
            saveRequest.setScenarioIdMap(planScenarioIdMap);

            returnDTO.setPlanScenarioIdMap(planScenarioIdMap);
            returnDTO.setApiTestCaseDataMap(planApiCaseIdMap);
        }

        List<String> loadResourcePools = new ArrayList<>();
        if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
            Map<String, String> performanceIdMap = new LinkedHashMap<>();
            List<TestPlanLoadCaseDTO> testPlanLoadCaseDTOList = planTestPlanLoadCaseService.list(planId);
            for (TestPlanLoadCaseDTO dto : testPlanLoadCaseDTOList) {
                performanceIdMap.put(dto.getId(), dto.getLoadCaseId());
            }
            saveRequest.setPerformanceIsExecuting(!performanceIdMap.isEmpty());
            saveRequest.setPerformanceIdMap(performanceIdMap);
            returnDTO.setPerformanceIdMap(performanceIdMap);

            loadResourcePools = planTestPlanLoadCaseService.selectResourcePoolsByPlan(planId);
        }

        if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
            Map<String, String> uiScenarioIdMap = new LinkedHashMap<>();
            List<TestPlanUiScenario> testPlanUiScenarioList = planTestPlanUiScenarioCaseService.list(planId);
            if (CollectionUtils.isNotEmpty(testPlanUiScenarioList)) {
                for (TestPlanUiScenario dto : testPlanUiScenarioList) {
                    uiScenarioIdMap.put(dto.getId(), dto.getUiScenarioId());
                }
                List<TestPlanApiScenarioInfoDTO> testPlanUiScenarioInfoList = extTestPlanScenarioCaseMapper.selectLegalUiDataByTestPlanId(planId);
                TestPlanReportRunInfoDTO uiRunInfoDTO = this.parseTestPlanUiRunInfo(runModeConfigDTO, testPlanUiScenarioInfoList);
                if (runInfoDTO != null) {
                    runInfoDTO.setUiScenarioRunInfo(uiRunInfoDTO.getUiScenarioRunInfo());
                } else {
                    runInfoDTO = uiRunInfoDTO;
                }
            }
            saveRequest.setUiScenarioIsExecuting(!uiScenarioIdMap.isEmpty());
            saveRequest.setUiScenarioIdMap(uiScenarioIdMap);
            returnDTO.setUiScenarioIdMap(uiScenarioIdMap);
        }

        if (testPlanReport == null) {
            if(runInfoDTO == null){
                runInfoDTO = new TestPlanReportRunInfoDTO();
            }
            if (!saveRequest.isApiCaseIsExecuting() && !saveRequest.isScenarioIsExecuting()) {
                //如果没有接口用例以及场景运行，执行配置中所选的资源池配置置空，避免报告显示资源池时给用户造成困扰;
                runModeConfigDTO.setResourcePoolId(null);
                if (!saveRequest.isUiScenarioIsExecuting()) {
                    //如果也没有ui运行，则运行环境也置空，避免显示了没用到的环境给用户造成困扰。
                    runInfoDTO.setRequestEnvMap(new HashMap<>());
                }
            }

            runInfoDTO.setResourcePools(loadResourcePools);
            if (StringUtils.isNotEmpty(runModeConfigDTO.getResourcePoolId())) {
                if (!runInfoDTO.getResourcePools().contains(runModeConfigDTO.getResourcePoolId())) {
                    runInfoDTO.getResourcePools().add(runModeConfigDTO.getResourcePoolId());
                }
            }
            returnDTO = this.genTestPlanReport(saveRequest, runInfoDTO);
        }
        returnDTO.setPlanScenarioIdMap(saveRequest.getScenarioIdMap());
        returnDTO.setApiTestCaseDataMap(saveRequest.getApiCaseIdMap());
        returnDTO.setPerformanceIdMap(saveRequest.getPerformanceIdMap());
        returnDTO.setUiScenarioIdMap(saveRequest.getUiScenarioIdMap());

        String debugMsg = String.format(
                "生成测试计划报告!id:【%s】,PlanScenarioIdMap:【%s】,ApiTestCaseDataMap:【%s】,PerformanceIdMap:【%s】,UiScenarioIdMap:【%s】",
                planReportId,
                JSON.toJSONString(saveRequest.getScenarioIdMap()),
                JSON.toJSONString(saveRequest.getApiCaseIdMap()),
                JSON.toJSONString(saveRequest.getPerformanceIdMap()),
                JSON.toJSONString(saveRequest.getUiScenarioIdMap()));
        LoggerUtil.info(debugMsg);
        return returnDTO;
    }

    private TestPlanReportRunInfoDTO parseTestPlanUiRunInfo(RunModeConfigDTO config, List<TestPlanApiScenarioInfoDTO> scenarios) {
        TestPlanReportRunInfoDTO runInfoDTO = new TestPlanReportRunInfoDTO();
        final Map<String, String> runEnvMap = MapUtils.isNotEmpty(config.getEnvMap()) ? config.getEnvMap() : new HashMap<>();
        runInfoDTO.setRunMode(config.getMode());

        if (StringUtils.equals(GROUP, config.getEnvironmentType()) && StringUtils.isNotEmpty(config.getEnvironmentGroupId())) {
            Map<String, String> groupMap = baseEnvGroupProjectService.getEnvMap(config.getEnvironmentGroupId());
            if (MapUtils.isNotEmpty(groupMap)) {
                runEnvMap.putAll(groupMap);
            }
            runInfoDTO.setEnvGroupId(config.getEnvironmentGroupId());
        }
        // 场景环境处理
        scenarios.forEach(item -> {
            Map<String, String> envMap = null;
            if (StringUtils.equalsIgnoreCase(GROUP, item.getEnvironmentType())
                    && StringUtils.isNotEmpty(item.getEnvironmentGroupId())) {
                envMap = baseEnvGroupProjectService.getEnvMap(item.getEnvironmentGroupId());
            } else {
                if (MapUtils.isNotEmpty(runEnvMap) && runEnvMap.containsKey(item.getProjectId())) {
                    runInfoDTO.putUiScenarioRunInfo(item.getId(), item.getProjectId(), runEnvMap.get(item.getProjectId()));
                } else if (StringUtils.isNotEmpty(item.getEnvironment())) {
                    try {
                        envMap = JSON.parseObject(item.getEnvironment(), Map.class);
                    } catch (Exception e) {
                        LogUtil.error("解析场景环境失败!", e);
                    }
                }
            }
            if (MapUtils.isNotEmpty(envMap)) {
                for (Map.Entry<String, String> entry : envMap.entrySet()) {
                    String projectId = entry.getKey();
                    String envIdStr = entry.getValue();
                    runInfoDTO.putUiScenarioRunInfo(item.getId(), projectId, envIdStr);
                }
            }
        });

        return runInfoDTO;
    }

    /**
     * saveRequest.reportId               报告ID(外部传入）
     * saveRequest.planId                 测试计划ID
     * saveRequest.userId                 用户ID
     * saveRequest.triggerMode            执行方式
     * saveRequest.countResources         是否统计资源-false的话， 下面三个不同资源是否运行则由参数决定。 true的话则由统计后的结果决定
     * saveRequest.apiCaseIsExecuting     接口案例是否执行中
     * saveRequest.scenarioIsExecuting    场景案例是否执行中
     * saveRequest.performanceIsExecuting 性能案例是否执行中
     */
    public TestPlanScheduleReportInfoDTO genTestPlanReport(TestPlanReportSaveRequest saveRequest, TestPlanReportRunInfoDTO runInfoDTO) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(saveRequest.getPlanId());
        String testPlanReportID = saveRequest.getReportID();
        TestPlanReport testPlanReport = new TestPlanReport();
        testPlanReport.setTestPlanId(saveRequest.getPlanId());
        testPlanReport.setId(testPlanReportID);
        testPlanReport.setCreateTime(System.currentTimeMillis());
        testPlanReport.setUpdateTime(System.currentTimeMillis());
        try {
            testPlanReport.setName(testPlan.getName() + "-" + DateUtils.getTimeString(new Date()));
            if (testPlanReport.getName().length() > 128) {
                testPlanReport.setName(testPlan.getName().substring(0, 106) + "-" + DateUtils.getTimeString(new Date()));
            }
        } catch (Exception ignored) {
        }

        testPlanReport.setTriggerMode(saveRequest.getTriggerMode());
        testPlanReport.setCreator(saveRequest.getUserId());
        testPlanReport.setStartTime(System.currentTimeMillis());
        testPlanReport.setEndTime(System.currentTimeMillis());
        testPlanReport.setIsNew(true);
        if (runInfoDTO != null) {
            testPlanReport.setRunInfo(JSON.toJSONString(runInfoDTO));
        }

        if (!saveRequest.isCountResources()) {
            testPlanReport.setIsApiCaseExecuting(saveRequest.isApiCaseIsExecuting());
            testPlanReport.setIsScenarioExecuting(saveRequest.isScenarioIsExecuting());
            testPlanReport.setIsPerformanceExecuting(saveRequest.isPerformanceIsExecuting());
            testPlanReport.setIsUiScenarioExecuting(saveRequest.isUiScenarioIsExecuting());
        }

        if (BooleanUtils.isTrue(testPlanReport.getIsScenarioExecuting())
                || BooleanUtils.isTrue(testPlanReport.getIsApiCaseExecuting())
                || BooleanUtils.isTrue(testPlanReport.getIsPerformanceExecuting())
                || BooleanUtils.isTrue(testPlanReport.getIsUiScenarioExecuting())) {
            testPlanReport.setStatus(TestPlanReportStatus.RUNNING.name());
        } else {
            testPlanReport.setStatus(TestPlanReportStatus.COMPLETED.name());
        }

        testPlanReportMapper.insert(testPlanReport);

        TestPlanScheduleReportInfoDTO returnDTO = new TestPlanScheduleReportInfoDTO();
        returnDTO.setTestPlanReport(testPlanReport);
        return returnDTO;
    }

    public void genTestPlanReportContent(TestPlanScheduleReportInfoDTO returnDTO) {
        TestPlanReportContentWithBLOBs testPlanReportContent = new TestPlanReportContentWithBLOBs();
        testPlanReportContent.setId(UUID.randomUUID().toString());
        testPlanReportContent.setTestPlanReportId(returnDTO.getTestPlanReport().getId());
        testPlanReportContentMapper.insert(testPlanReportContent);
    }

    public TestPlanReportDTO getMetric(String reportId) {
        TestPlanReportDTO returnDTO = new TestPlanReportDTO();
        TestPlanReport report = testPlanReportMapper.selectByPrimaryKey(reportId);
        if (report != null) {
            TestPlanReportDataExample example = new TestPlanReportDataExample();
            example.createCriteria().andTestPlanReportIdEqualTo(reportId);
            List<TestPlanReportDataWithBLOBs> reportDataList = testPlanReportDataMapper.selectByExampleWithBLOBs(example);
            if (!reportDataList.isEmpty()) {
                TestPlanReportDataWithBLOBs reportData = reportDataList.get(0);

                if (!StringUtils.isEmpty(reportData.getExecuteResult())) {
                    returnDTO.setExecuteResult(JSON.parseObject(reportData.getExecuteResult(), TestCaseReportAdvanceStatusResultDTO.class));
                }
                if (!StringUtils.isEmpty(reportData.getModuleExecuteResult())) {
                    returnDTO.setModuleExecuteResult(JSON.parseArray(reportData.getModuleExecuteResult(), TestCaseReportModuleResultDTO.class));
                }
                if (!StringUtils.isEmpty(reportData.getFailurTestCases())) {
                    returnDTO.setFailureTestCases(JSON.parseObject(reportData.getFailurTestCases(), FailureTestCasesAdvanceDTO.class));
                }
                if (!StringUtils.isEmpty(reportData.getIssuesInfo())) {
                    returnDTO.setIssues(JSON.parseArray(reportData.getIssuesInfo(), Issues.class));
                }
                List<String> creatorList = new ArrayList<>();
                creatorList.add(report.getCreator());
                returnDTO.setExecutors(creatorList);
                String name = getPrincipalName(report.getTestPlanId());
                returnDTO.setPrincipal(name);
                returnDTO.setPrincipalName(name);
                returnDTO.setStartTime(report.getStartTime());
                returnDTO.setEndTime(report.getEndTime());

                String testProject = extTestPlanMapper.findTestProjectNameByTestPlanID(report.getTestPlanId());
                returnDTO.setProjectName(testProject);
            }

            returnDTO.setId(report.getId());
            returnDTO.setName(report.getName());
            returnDTO.setStartTime(report.getStartTime());
            returnDTO.setEndTime(report.getEndTime());
            returnDTO.setTestPlanId(report.getTestPlanId());
            returnDTO.setReportComponents(report.getComponents());
        }
        return returnDTO;
    }

    private String getPrincipalName(String planId) {
        if (StringUtils.isBlank(planId)) {
            return StringUtils.EMPTY;
        }
        String principalName = StringUtils.EMPTY;
        TestPlanPrincipalExample example = new TestPlanPrincipalExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanPrincipal> principals = testPlanPrincipalMapper.selectByExample(example);
        List<String> principalIds = principals.stream().map(TestPlanPrincipal::getPrincipalId).collect(Collectors.toList());
        Map<String, String> userMap = ServiceUtils.getUserNameMap(principalIds);
        for (String principalId : principalIds) {
            String name = userMap.get(principalId);
            if (StringUtils.isNotBlank(principalName)) {
                principalName = principalName + "、" + name;
            } else {
                principalName = principalName + name;
            }
        }
        return principalName;
    }

    //更新测试计划报告的数据结构
    public void updateReportStructInfo(TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs, TestPlanReportDataStruct reportStruct) {
        //更新BaseCount统计字段和通过率
        testPlanReportContentWithBLOBs.setPassRate(reportStruct.getPassRate());
        testPlanReportContentWithBLOBs.setApiBaseCount(JSON.toJSONString(reportStruct));
        testPlanReportContentMapper.updateByPrimaryKeySelective(testPlanReportContentWithBLOBs);
    }

    private boolean isTestPlanCountOver(TestPlanReport testPlanReport) {
        if (testPlanReport != null && StringUtils.equalsAnyIgnoreCase(testPlanReport.getStatus(),
                "stopped",
                TestPlanReportStatus.COMPLETED.name(),
                TestPlanReportStatus.SUCCESS.name(),
                TestPlanReportStatus.FAILED.name())) {
            return !extTestPlanReportContentMapper.isApiBasicCountIsNull(testPlanReport.getId());
        }
        return false;
    }

    public void testPlanExecuteOver(String testPlanReportId, String finishStatus) {
        TestPlanReport testPlanReport = this.getTestPlanReport(testPlanReportId);
        if (this.isTestPlanCountOver(testPlanReport)) {
            return;
        }
        boolean isSendMessage = false;
        if (testPlanReport != null) {
            testPlanReport.setIsApiCaseExecuting(false);
            testPlanReport.setIsScenarioExecuting(false);
            testPlanReport.setIsPerformanceExecuting(false);
            testPlanReport.setIsUiScenarioExecuting(false);
            if (StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), ExecuteResult.TEST_PLAN_RUNNING.toString())) {
                isSendMessage = true;
            }
            TestPlanReportContentWithBLOBs content = null;
            TestPlanWithBLOBs testPlanWithBLOBs = null;
            try {
                HttpHeaderUtils.runAsUser(testPlanReport.getCreator());
                boolean isRerunningTestPlan = BooleanUtils.isTrue(StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), APITestStatus.Rerunning.name()));
                //测试计划报告结果数据初始化
                testPlanReport.setStatus(finishStatus);
                //统计并保存报告
                content = this.countAndSaveTestPlanReport(testPlanReport, isRerunningTestPlan);
                //更新测试计划的执行相关信息(状态、执行率等）
                testPlanWithBLOBs = testPlanService.selectAndChangeTestPlanExecuteInfo(testPlanReport.getTestPlanId());
            } catch (Exception e) {
                testPlanReport.setStatus(finishStatus);
                LogUtil.error("统计测试计划状态失败！", e);
            } finally {
                HttpHeaderUtils.clearUser();
                testPlanReportMapper.updateByPrimaryKey(testPlanReport);
                if (testPlanWithBLOBs != null) {
                    testPlanMessageService.checkTestPlanStatusAndSendMessage(testPlanReport, content, testPlanWithBLOBs, isSendMessage);
                }
                this.executeTestPlanByQueue(testPlanReportId);
            }
        }
    }

    @Async
    public void testPlanUnExecute(TestPlanReport testPlanReport) {
        if (testPlanReport != null && !StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.COMPLETED.name())) {
            testPlanReport.setIsApiCaseExecuting(false);
            testPlanReport.setIsScenarioExecuting(false);
            testPlanReport.setIsPerformanceExecuting(false);
            testPlanReport.setIsUiScenarioExecuting(false);
            testPlanReport.setStatus(TestPlanReportStatus.COMPLETED.name());
            testPlanReportMapper.updateByPrimaryKey(testPlanReport);
            this.executeTestPlanByQueue(testPlanReport.getId());
        }
    }

    /**
     * 统计测试计划报告信息
     */
    public TestPlanReportContentWithBLOBs countAndSaveTestPlanReport(TestPlanReport testPlanReport, boolean isRerunningTestPlan) {
        long endTime = System.currentTimeMillis();
        //原逻辑中要判断包含测试计划功能用例时才会赋予结束时间。执行测试计划产生的测试报告，它的结束时间感觉没有这种判断必要。
        testPlanReport.setEndTime(endTime);
        testPlanReport.setUpdateTime(endTime);
        TestPlanReportContentWithBLOBs content = this.selectTestPlanReportContentByReportId(testPlanReport.getId());
        if (content != null) {
            //更新content表对结束日期  重跑的测试计划报告不用更新
            if (!isRerunningTestPlan) {
                content.setStartTime(testPlanReport.getStartTime());
                content.setEndTime(endTime);
            } else {
                //重跑的测试计划需要重新统计报告信息
                content.setApiBaseCount(null);
            }
            TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
            TestPlanReportDataStruct apiBaseCountStruct = testPlanService.generateReportStruct(testPlan, testPlanReport, content, isRerunningTestPlan);
            if (apiBaseCountStruct.getPassRate() == 1) {
                testPlanReport.setStatus(TestPlanReportStatus.SUCCESS.name());
            } else if (apiBaseCountStruct.getPassRate() < 1) {
                testPlanReport.setStatus(TestPlanReportStatus.FAILED.name());
            }
            //更新数据结构
            this.updateReportStructInfo(content, apiBaseCountStruct);
        }
        return content;
    }


    public void executeTestPlanByQueue(String testPlanReportId) {
        TestPlanExecutionQueueExample testPlanExecutionQueueExample = new TestPlanExecutionQueueExample();
        testPlanExecutionQueueExample.createCriteria().andReportIdEqualTo(testPlanReportId);
        List<TestPlanExecutionQueue> planExecutionQueues = testPlanExecutionQueueMapper.selectByExample(testPlanExecutionQueueExample);
        String runMode = null;
        String resourceId = null;
        if (CollectionUtils.isNotEmpty(planExecutionQueues)) {
            runMode = planExecutionQueues.get(0).getRunMode();
            resourceId = planExecutionQueues.get(0).getResourceId();
            testPlanExecutionQueueMapper.deleteByExample(testPlanExecutionQueueExample);
        }

        if (runMode != null && StringUtils.equalsIgnoreCase(runMode, RunModeConstants.SERIAL.name()) && resourceId != null) {
            TestPlanExecutionQueueExample queueExample = new TestPlanExecutionQueueExample();
            queueExample.createCriteria().andReportIdIsNotNull().andResourceIdEqualTo(resourceId);
            queueExample.setOrderByClause("`num` ASC");
            List<TestPlanExecutionQueue> planExecutionQueueList = testPlanExecutionQueueMapper.selectByExample(queueExample);
            if (CollectionUtils.isNotEmpty(planExecutionQueueList)) {
                TestPlanExecutionQueue testPlanExecutionQueue = planExecutionQueueList.get(0);
                TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanExecutionQueue.getTestPlanId());
                Map jsonObject = JSON.parseMap(testPlan.getRunModeConfig());
                TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                TestPlanRunRequest runRequest = JSON.parseObject(JSON.toJSONString(jsonObject), TestPlanRunRequest.class);
                if (StringUtils.isNotBlank(testPlanExecutionQueue.getExecuteUser())) {
                    runRequest.setUserId(testPlanExecutionQueue.getExecuteUser());
                }
                runRequest.setTestPlanId(testPlanExecutionQueue.getTestPlanId());
                runRequest.setReportId(testPlanExecutionQueue.getReportId());
                runRequest.setTestPlanId(testPlan.getId());
                runRequest.setTriggerMode(TriggerMode.BATCH.name());
                try {
                    if (SessionUtils.getUser() == null) {
                        HttpHeaderUtils.runAsUser("admin");
                    }
                    //如果运行测试计划的过程中出现异常，则整个事务会回滚。 删除队列的事务也不会提交，也不会执行后面的测试计划
                    testPlanService.runPlan(runRequest);
                } catch (Exception e) {
                    LogUtil.error("执行队列中的下一个测试计划失败！ ", e);
                    this.testPlanExecuteOver(runRequest.getReportId(), TestPlanReportStatus.FAILED.name());
                } finally {
                    HttpHeaderUtils.clearUser();
                }
            }
        }
    }

    /**
     * @param planReportId    测试计划报告ID
     * @param resourceRunMode 资源的运行模式,triggerMode非Scedule可以为null
     * @param triggerMode     触发方式  ReportTriggerMode.enum
     */
    public void countReportByTestPlanReportId(String planReportId, String resourceRunMode, String triggerMode) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(planReportId);

        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(testPlanReport.getTestPlanId());
        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);

        testPlanReport.setEndTime(System.currentTimeMillis());
        testPlanReport.setUpdateTime(System.currentTimeMillis());

        //只针对定时任务做处理
        if (StringUtils.equalsAny(triggerMode, ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())
                && StringUtils.equalsAny(resourceRunMode, ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
            testPlanReport.setIsApiCaseExecuting(false);
        } else if (StringUtils.equalsAny(triggerMode, ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())
                && StringUtils.equalsAny(resourceRunMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name())) {
            testPlanReport.setIsScenarioExecuting(false);
        } else if (StringUtils.equalsAny(triggerMode, ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())
                && StringUtils.equalsAny(resourceRunMode, ApiRunMode.SCHEDULE_PERFORMANCE_TEST.name(), ApiRunMode.JENKINS_PERFORMANCE_TEST.name())) {
            testPlanReport.setIsPerformanceExecuting(false);
        } else {
            testPlanReport.setIsPerformanceExecuting(false);
            testPlanReport.setIsScenarioExecuting(false);
            testPlanReport.setIsApiCaseExecuting(false);
        }

        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId);
        List<TestPlanReportContentWithBLOBs> testPlanReportContentList = testPlanReportContentMapper.selectByExampleWithBLOBs(example);

        TestPlanReportContentWithBLOBs testPlanReportContent = null;
        TestPlanReportDataStruct reportDTO = testPlanService.buildPlanReport(testPlan.getId(), false);
        if (!testPlanReportContentList.isEmpty()) {
            testPlanReportContent = parseReportDaoToReportContent(reportDTO, testPlanReportContentList.get(0));
            testPlanReportContent.setStartTime(null);
            testPlanReportContent.setEndTime(null);
            testPlanReportContent.setApiBaseCount(JSON.toJSONString(reportDTO));
            testPlanReportContentMapper.updateByPrimaryKeySelective(testPlanReportContent);
        }

        if (reportDTO.getStartTime() == null) {
            reportDTO.setStartTime(System.currentTimeMillis());
        }

        if (reportDTO.getEndTime() == null) {
            reportDTO.setEndTime(System.currentTimeMillis());
        }

        String testPlanStatus = this.getTestPlanReportStatus(testPlanReport, reportDTO);
        testPlanReport.setStatus(testPlanStatus);
        testPlanReportMapper.updateByPrimaryKey(testPlanReport);
    }

    public TestPlanReportContentWithBLOBs parseReportDaoToReportContent(TestPlanReportDataStruct reportDTO, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        String id = testPlanReportContentWithBLOBs.getId();
        String testPlanReportId = testPlanReportContentWithBLOBs.getTestPlanReportId();
        if (testPlanReportContentWithBLOBs.getEndTime() != null) {
            reportDTO.setEndTime(testPlanReportContentWithBLOBs.getEndTime());
        }
        String summary = testPlanReportContentWithBLOBs.getSummary();
        BeanUtils.copyBean(testPlanReportContentWithBLOBs, reportDTO);
        testPlanReportContentWithBLOBs.setSummary(summary);
        testPlanReportContentWithBLOBs.setId(id);
        testPlanReportContentWithBLOBs.setTestPlanReportId(testPlanReportId);
        if (reportDTO.getFunctionResult() != null) {
            testPlanReportContentWithBLOBs.setFunctionResult(JSON.toJSONString(reportDTO.getFunctionResult()));
        }
        if (reportDTO.getApiResult() != null) {
            testPlanReportContentWithBLOBs.setApiResult(JSON.toJSONString(reportDTO.getApiResult()));
        }
        if (reportDTO.getUiResult() != null) {
            testPlanReportContentWithBLOBs.setUiResult(JSON.toJSONString(reportDTO.getUiResult()));
        }
        if (reportDTO.getUiAllCases() != null) {
            testPlanReportContentWithBLOBs.setUiAllCases(JSON.toJSONString(reportDTO.getUiAllCases()));
        }
        if (reportDTO.getLoadResult() != null) {
            testPlanReportContentWithBLOBs.setLoadResult(JSON.toJSONString(reportDTO.getLoadResult()));
        }
        if (reportDTO.getFunctionAllCases() != null) {
            testPlanReportContentWithBLOBs.setFunctionAllCases(JSON.toJSONString(reportDTO.getFunctionAllCases()));
        }
        if (reportDTO.getIssueList() != null) {
            testPlanReportContentWithBLOBs.setIssueList(JSON.toJSONString(reportDTO.getIssueList()));
        }
        if (reportDTO.getApiAllCases() != null) {
            testPlanReportContentWithBLOBs.setApiAllCases(JSON.toJSONString(reportDTO.getApiAllCases()));
        }
        if (reportDTO.getApiFailureCases() != null) {
            testPlanReportContentWithBLOBs.setApiFailureCases(JSON.toJSONString(reportDTO.getApiFailureCases()));
        }
        if (reportDTO.getScenarioAllCases() != null) {
            testPlanReportContentWithBLOBs.setScenarioAllCases(JSON.toJSONString(reportDTO.getScenarioAllCases()));
        }
        if (reportDTO.getScenarioFailureCases() != null) {
            testPlanReportContentWithBLOBs.setScenarioFailureCases(JSON.toJSONString(reportDTO.getScenarioFailureCases()));
        }
        if (reportDTO.getLoadAllCases() != null) {
            testPlanReportContentWithBLOBs.setLoadAllCases(JSON.toJSONString(reportDTO.getLoadAllCases()));
        }
        if (reportDTO.getLoadFailureCases() != null) {
            testPlanReportContentWithBLOBs.setLoadFailureCases(JSON.toJSONString(reportDTO.getLoadFailureCases()));
        }
        if (reportDTO.getErrorReportCases() != null) {
            testPlanReportContentWithBLOBs.setErrorReportCases(JSON.toJSONString(reportDTO.getErrorReportCases()));
        }
        if (reportDTO.getErrorReportScenarios() != null) {
            testPlanReportContentWithBLOBs.setErrorReportScenarios(JSON.toJSONString(reportDTO.getErrorReportScenarios()));
        }
        if (reportDTO.getUnExecuteCases() != null) {
            testPlanReportContentWithBLOBs.setUnExecuteCases(JSON.toJSONString(reportDTO.getUnExecuteCases()));
        }
        if (reportDTO.getUnExecuteScenarios() != null) {
            testPlanReportContentWithBLOBs.setUnExecuteScenarios(JSON.toJSONString(reportDTO.getUnExecuteScenarios()));
        }

        return testPlanReportContentWithBLOBs;
    }

    /**
     * 计算测试计划的状态
     *
     * @param testPlanReport
     * @return
     */
    private String getTestPlanReportStatus(TestPlanReport testPlanReport, TestPlanReportDataStruct reportDTO) {
        String status = TestPlanReportStatus.COMPLETED.name();
        if (testPlanReport != null) {
            if (testPlanReport.getIsApiCaseExecuting() || testPlanReport.getIsPerformanceExecuting() || testPlanReport.getIsScenarioExecuting()) {
                status = TestPlanReportStatus.RUNNING.name();
            } else {
                if (reportDTO != null) {
                    status = TestPlanReportStatus.SUCCESS.name();
                    try {
                        if (hasFunctionFailedCases(reportDTO.getFunctionAllCases())
                                || CollectionUtils.isNotEmpty(reportDTO.getApiFailureCases())
                                || CollectionUtils.isNotEmpty(reportDTO.getScenarioFailureCases())
                                || CollectionUtils.isNotEmpty(reportDTO.getLoadFailureCases())) {
                            status = TestPlanReportStatus.FAILED.name();
                            return status;
                        }
                    } catch (Exception e) {
                        status = TestPlanReportStatus.FAILED.name();
                    }
                } else {
                    status = TestPlanReportStatus.COMPLETED.name();
                }
            }
        }
        return status;
    }

    private boolean hasFunctionFailedCases(List<TestPlanCaseDTO> functionAllCases) {
        if (functionAllCases == null) {
            return false;
        }
        for (TestPlanCaseDTO functionAllCase : functionAllCases) {
            if (StringUtils.equals(functionAllCase.getStatus(), TestPlanTestCaseStatus.Failure.name())) {
                return true;
            }
        }
        return false;
    }

    public TestPlanReport getTestPlanReport(String planId) {
        return testPlanReportMapper.selectByPrimaryKey(planId);
    }

    public List<String> getTestPlanReportIdsByLoadTestReportId(String loadTestReportId) {
        List<String> testPlanReportId = extTestPlanMapper.findIdByPerformanceReportId(loadTestReportId);
        return testPlanReportId;
    }

    public void delete(List<String> testPlanReportIdList) {
        if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
            TestPlanReportExample example = new TestPlanReportExample();
            example.createCriteria().andIdIn(testPlanReportIdList);
            testPlanReportMapper.deleteByExample(example);
            TestPlanReportDataExample testPlanReportDataExample = new TestPlanReportDataExample();
            testPlanReportDataExample.createCriteria().andTestPlanReportIdIn(testPlanReportIdList);
            testPlanReportDataMapper.deleteByExample(testPlanReportDataExample);
            TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
            contentExample.createCriteria().andTestPlanReportIdIn(testPlanReportIdList);
            testPlanReportContentMapper.deleteByExample(contentExample);
            BatchProcessingUtil.batchDeleteApiReport(testPlanReportIdList, this::deleteApiCaseReportByTestPlanExecute, this::deleteScenarioReportByTestPlanExecute, this::deleteUiReportByTestPlanExecute);
        }
    }

    //删除执行测试计划产生的接口用例报告
    private void deleteApiCaseReportByTestPlanExecute(List<String> testPlanReportIdList) {
        if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
            extTestPlanReportContentMapper.deleteApiReportByTestPlanReportList(testPlanReportIdList);
        }
    }

    //删除执行测试计划产生的场景报告
    private void deleteScenarioReportByTestPlanExecute(List<String> testPlanReportIdList) {
        if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
            List<String> scenarioReportIds = extTestPlanReportContentMapper.selectScenarioReportByTestPlanReportIds(testPlanReportIdList);
            if (CollectionUtils.isNotEmpty(scenarioReportIds)) {
                extTestPlanReportContentMapper.deleteScenarioReportByIds(scenarioReportIds);
                extTestPlanReportContentMapper.deleteScenarioReportResultByIds(scenarioReportIds);
                extTestPlanReportContentMapper.deleteScenarioReportStructureByIds(scenarioReportIds);
            }
        }
    }

    //删除执行测试计划产生的UI报告
    private void deleteUiReportByTestPlanExecute(List<String> testPlanReportIdList) {
        if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
            // 如果 UI 的表没有初始化，则不删除
            if (!taskService.checkUiPermission()) {
                return;
            }
            List<String> scenarioReportIds = extTestPlanReportContentMapper.selectUiReportByTestPlanReportIds(testPlanReportIdList);
            if (CollectionUtils.isNotEmpty(scenarioReportIds)) {
                try {
                    extTestPlanReportContentMapper.deleteUiReportByIds(scenarioReportIds);
                    extTestPlanReportContentMapper.deleteUiReportResultByIds(scenarioReportIds);
                    extTestPlanReportContentMapper.deleteUiReportStructureByIds(scenarioReportIds);
                } catch (Exception e) {
                    LogUtil.error("删除UI报告出错!", e);
                }
            }
        }
    }

    public void delete(QueryTestPlanReportRequest request) {
        List<String> deleteReportIds = request.getDataIds();
        if (request.isSelectAllDate()) {
            deleteReportIds = this.getAllApiIdsByFrontedSelect(request.getFilters(), request.getName(), request.getProjectId(), request.getUnSelectIds(), request.getCombine());
        }
        this.delete(deleteReportIds);
    }

    private void deleteReportBatch(List<String> reportIds) {
        int handleCount = 5000;
        List<String> handleIdList;

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanReportMapper planReportMapper = sqlSession.getMapper(TestPlanReportMapper.class);
        TestPlanReportDataMapper planReportDataMapper = sqlSession.getMapper(TestPlanReportDataMapper.class);
        TestPlanReportContentMapper planReportContentMapper = sqlSession.getMapper(TestPlanReportContentMapper.class);

        try {
            while (reportIds.size() > handleCount) {
                handleIdList = new ArrayList<>(handleCount);
                List<String> otherIdList = new ArrayList<>();
                for (int index = 0; index < reportIds.size(); index++) {
                    if (index < handleCount) {
                        handleIdList.add(reportIds.get(index));
                    } else {
                        otherIdList.add(reportIds.get(index));
                    }
                }

                TestPlanReportExample deleteReportExample = new TestPlanReportExample();
                deleteReportExample.createCriteria().andIdIn(handleIdList);
                planReportMapper.deleteByExample(deleteReportExample);

                TestPlanReportDataExample example = new TestPlanReportDataExample();
                example.createCriteria().andTestPlanReportIdIn(handleIdList);
                planReportDataMapper.deleteByExample(example);

                TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
                contentExample.createCriteria().andTestPlanReportIdIn(handleIdList);
                planReportContentMapper.deleteByExample(contentExample);

                //                //删除关联的接口用例报告
                //                ApiDefinitionExecResultExample apiDefinitionExecResultExample = new ApiDefinitionExecResultExample();
                //                apiDefinitionExecResultExample.createCriteria().andRelevanceTestPlanReportIdIn(handleIdList);
                //                batchDefinitionExecResultMapper.deleteByExample(apiDefinitionExecResultExample);
                //
                //                //删除关联的场景和ui用例报告
                //                ApiScenarioReportExample apiScenarioReportExample = new ApiScenarioReportExample();
                //                apiScenarioReportExample.createCriteria().andRelevanceTestPlanReportIdIn(handleIdList);
                //                batchScenarioReportMapper.deleteByExample(apiScenarioReportExample);
                //
                //                //删除关联的性能测试用例报告
                //                LoadTestReportExample loadTestReportExample = new LoadTestReportExample();
                //                loadTestReportExample.createCriteria().andRelevanceTestPlanReportIdIn(handleIdList);
                //                batchLoadTestReportMapper.deleteByExample(loadTestReportExample);

                sqlSession.flushStatements();

                reportIds = otherIdList;
            }

            if (!reportIds.isEmpty()) {
                TestPlanReportExample deleteReportExample = new TestPlanReportExample();
                deleteReportExample.createCriteria().andIdIn(reportIds);
                planReportMapper.deleteByExample(deleteReportExample);


                TestPlanReportDataExample example = new TestPlanReportDataExample();
                example.createCriteria().andTestPlanReportIdIn(reportIds);
                planReportDataMapper.deleteByExample(example);

                TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
                contentExample.createCriteria().andTestPlanReportIdIn(reportIds);
                planReportContentMapper.deleteByExample(contentExample);

                //                //删除关联的接口用例报告
                //                ApiDefinitionExecResultExample apiDefinitionExecResultExample = new ApiDefinitionExecResultExample();
                //                apiDefinitionExecResultExample.createCriteria().andRelevanceTestPlanReportIdIn(reportIds);
                //                batchDefinitionExecResultMapper.deleteByExample(apiDefinitionExecResultExample);
                //
                //                //删除关联的场景和ui用例报告
                //                ApiScenarioReportExample apiScenarioReportExample = new ApiScenarioReportExample();
                //                apiScenarioReportExample.createCriteria().andRelevanceTestPlanReportIdIn(reportIds);
                //                batchScenarioReportMapper.deleteByExample(apiScenarioReportExample);
                //
                //                //删除关联的性能测试用例报告
                //                LoadTestReportExample loadTestReportExample = new LoadTestReportExample();
                //                loadTestReportExample.createCriteria().andRelevanceTestPlanReportIdIn(reportIds);
                //                batchLoadTestReportMapper.deleteByExample(loadTestReportExample);

                sqlSession.flushStatements();
            }
        } finally {
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }

    }

    private List<String> getAllApiIdsByFrontedSelect(Map<String, List<String>> filters, String name, String projectId, List<String> unSelectIds, Map<String, Object> combine) {
        QueryTestPlanReportRequest request = new QueryTestPlanReportRequest();
        request.setFilters(filters);
        request.setName(name);
        request.setProjectId(projectId);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        if (combine != null) {
            request.setCombine(combine);
        }
        List<TestPlanReportDTO> resList = extTestPlanReportMapper.list(request);
        List<String> ids = new ArrayList<>(0);
        if (!resList.isEmpty()) {
            List<String> allIds = resList.stream().map(TestPlanReportDTO::getId).collect(Collectors.toList());
            ids = allIds.stream().filter(id -> !unSelectIds.contains(id)).collect(Collectors.toList());
        }
        return ids;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanReportExample example = new TestPlanReportExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanReport> nodes = testPlanReportMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            List<String> names = nodes.stream().map(TestPlanReport::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), null, String.join(",", names), null, new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public void deleteByPlanId(String planId) {
        TestPlanReportExample example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanReport> reportList = this.testPlanReportMapper.selectByExample(example);
        List<String> testPlanReportIdList = new ArrayList<>();
        for (TestPlanReport report : reportList) {
            testPlanReportIdList.add(report.getId());
        }
        this.delete(testPlanReportIdList);
    }

    public void deleteByPlanIds(List<String> planIds) {
        TestPlanReportExample example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdIn(planIds);
        List<TestPlanReport> reportList = testPlanReportMapper.selectByExample(example);
        List<String> ids = reportList.stream().map(TestPlanReport::getId).collect(Collectors.toList());
        this.delete(ids);
    }

    public TestPlanReportDataStruct getShareDbReport(ShareInfo shareInfo, String reportId) {
        if (SessionUtils.getUser() == null) {
            HttpHeaderUtils.runAsUser(shareInfo.getCreateUserId());
        }
        try {
            return getReport(reportId);
        } finally {
            HttpHeaderUtils.clearUser();
        }
    }

    public TestPlanReportContentWithBLOBs selectTestPlanReportContentByReportId(String reportId) {
        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(reportId);
        List<TestPlanReportContentWithBLOBs> testPlanReportContents = testPlanReportContentMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(testPlanReportContents)) {
            return null;
        } else {
            return testPlanReportContents.get(0);
        }
    }

    public TestPlanReportDataStruct getReport(String reportId) {
        TestPlanReportDataStruct testPlanReportDTO = new TestPlanReportDataStruct();
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(reportId);
        TestPlanReportContentWithBLOBs testPlanReportContent = this.selectTestPlanReportContentByReportId(reportId);
        if (ObjectUtils.anyNull(testPlanReport, testPlanReportContent)) {
            return testPlanReportDTO;
        }
        if (this.isDynamicallyGenerateReports(testPlanReportContent) || StringUtils.isNotEmpty(testPlanReportContent.getApiBaseCount())) {
            TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
            testPlanReportDTO = testPlanService.generateReportStruct(testPlan, testPlanReport, testPlanReportContent, false);
        } else {
            testPlanReportDTO = new TestPlanReportDataStruct(testPlanReportContent);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getSummary())) {
            testPlanReportDTO.setSummary(testPlanReportContent.getSummary());
        }
        testPlanReportDTO.setId(reportId);
        testPlanReportDTO.setName(testPlanReport.getName());
        return testPlanReportDTO;
    }

    /**
     * 获取测试计划实时报告
     *
     * @param planId
     * @return
     */
    public TestPlanReportDataStruct getRealTimeReport(String planId) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(planId);
        TestPlanReportDataStruct report = new TestPlanReportDataStruct();
        TestPlanFunctionResultReportDTO functionResult = new TestPlanFunctionResultReportDTO();
        TestPlanApiResultReportDTO apiResult = new TestPlanApiResultReportDTO();
        TestPlanUiResultReportDTO uiResult = new TestPlanUiResultReportDTO();
        report.setFunctionResult(functionResult);
        report.setApiResult(apiResult);
        report.setUiResult(uiResult);
        report.setStartTime(testPlan.getActualStartTime());
        report.setEndTime(testPlan.getActualEndTime());
        report.setSummary(testPlan.getReportSummary());
        report.setConfig(testPlan.getReportConfig());
        testPlanTestCaseService.calculatePlanReport(planId, report);
        issuesService.calculatePlanReport(planId, report);
        planTestPlanApiCaseService.calculatePlanReport(planId, report);
        planTestPlanScenarioCaseService.calculatePlanReport(planId, report);
        planTestPlanLoadCaseService.calculatePlanReport(planId, report);
        planTestPlanUiScenarioCaseService.calculatePlanReport(planId, report);

        if (report.getExecuteCount() != 0 && report.getCaseCount() != null) {
            report.setExecuteRate(report.getExecuteCount() * 0.1 * 10 / report.getCaseCount());
        } else {
            report.setExecuteRate(0.0);
        }
        if (report.getPassCount() != 0 && report.getCaseCount() != null) {
            report.setPassRate(report.getPassCount() * 0.1 * 10 / report.getCaseCount());
        } else {
            report.setPassRate(0.0);
        }

        report.setName(testPlan.getName());
        Project project = baseProjectService.getProjectById(testPlan.getProjectId());
        if (project.getPlatform() != null && project.getPlatform().equals(IssuesManagePlatform.Local.name())) {
            report.setIsThirdPartIssue(false);
        } else {
            report.setIsThirdPartIssue(true);
        }

        //查找资源池
        if (CollectionUtils.isNotEmpty(report.getResourcePools())) {
            TestResourcePoolExample example = new TestResourcePoolExample();
            example.createCriteria().andIdIn(report.getResourcePools());
            List<TestResourcePool> resourcePoolList = testResourcePoolMapper.selectByExample(example);
            report.setResourcePool(getResourcePoolName(resourcePoolList, report.getResourcePools().contains("LOCAL")));
        }
        return report;
    }

    public void initRunInformation(TestPlanReportDataStruct testPlanReportDTO, TestPlanReport testPlanReport) {
        if (ObjectUtils.isNotEmpty(testPlanReportDTO) && StringUtils.isNotEmpty(testPlanReport.getRunInfo())) {
            try {
                TestPlanReportRunInfoDTO runInfoDTO = JSON.parseObject(testPlanReport.getRunInfo(), TestPlanReportRunInfoDTO.class);
                this.setEnvironmentToDTO(testPlanReportDTO, runInfoDTO);
            } catch (Exception e) {
                LogUtil.error("解析测试计划报告记录的运行环境信息[" + testPlanReport.getRunInfo() + "]时出错!", e);
            }
        }
    }

    private String getResourcePoolName(List<TestResourcePool> resourcePoolList, boolean hasLocal) {
        ArrayList<String> resourcePoolName = new ArrayList<>();
        if (hasLocal) {
            resourcePoolName.add("LOCAL");
        }
        if (CollectionUtils.isNotEmpty(resourcePoolList)) {
            resourcePoolList.forEach(item -> {
                if (!resourcePoolName.contains(item.getName())) {
                    resourcePoolName.add(item.getName());
                }
            });
        }

        String resourcePoolNames = StringUtils.join(resourcePoolName.toArray(), StringUtils.SPACE);
        return resourcePoolNames;
    }

    public void setEnvironmentToDTO(TestPlanReportDataStruct testPlanReportDTO, TestPlanReportRunInfoDTO runInfoDTO) {
        if (ObjectUtils.allNotNull(testPlanReportDTO, runInfoDTO)) {
            //查找资源池
            if (CollectionUtils.isNotEmpty(runInfoDTO.getResourcePools())) {
                TestResourcePoolExample example = new TestResourcePoolExample();
                example.createCriteria().andIdIn(runInfoDTO.getResourcePools());
                List<TestResourcePool> resourcePoolList = testResourcePoolMapper.selectByExample(example);
                testPlanReportDTO.setResourcePool(getResourcePoolName(resourcePoolList, runInfoDTO.getResourcePools().contains("LOCAL")));
            } else if (CollectionUtils.isNotEmpty(testPlanReportDTO.getApiAllCases()) || CollectionUtils.isNotEmpty(testPlanReportDTO.getScenarioAllCases())) {
                //存在接口或者场景的用例，资源池默认是local
                testPlanReportDTO.setResourcePool("LOCAL");
            }
            // 环境组/运行环境
            if (StringUtils.isNotEmpty(runInfoDTO.getEnvGroupId())) {
                EnvironmentGroup environmentGroup = apiTestEnvironmentService.selectById(runInfoDTO.getEnvGroupId());
                if (StringUtils.isNotEmpty(environmentGroup.getName())) {
                    testPlanReportDTO.setEnvGroupName(environmentGroup.getName());
                }
            } else {
                Map<String, List<String>> requestEnvMap = new HashMap<>();
                if (MapUtils.isEmpty(runInfoDTO.getRequestEnvMap())) {
                    testPlanReportDTO.setProjectEnvMap(requestEnvMap);
                } else {
                    requestEnvMap = runInfoDTO.getRequestEnvMap();
                    Map<String, List<String>> projectEnvMap = new HashMap<>();
                    for (Map.Entry<String, List<String>> entry : requestEnvMap.entrySet()) {
                        String projectId = entry.getKey();
                        List<String> envIdList = entry.getValue();
                        Project project = baseProjectService.getProjectById(projectId);
                        String projectName = project == null ? null : project.getName();
                        if (StringUtils.isNotEmpty(projectName)) {
                            List<String> envNameList = new ArrayList<>();
                            for (String envId : envIdList) {
                                String envName = apiTestEnvironmentService.selectNameById(envId);
                                if (StringUtils.isNoneBlank(envName)) {
                                    envNameList.add(envName);
                                }
                            }
                            //考虑到存在不同工作空间下有相同名称的项目，这里还是要检查一下项目名称是否已被记录
                            if (projectEnvMap.containsKey(projectName)) {
                                envNameList.forEach(envName -> {
                                    if (!projectEnvMap.get(projectName).contains(envName)) {
                                        projectEnvMap.get(projectName).add(envName);
                                    }
                                });
                            } else {
                                projectEnvMap.put(projectName, new ArrayList<>() {{
                                    this.addAll(envNameList);
                                }});
                            }
                        }
                    }
                    if (MapUtils.isNotEmpty(projectEnvMap)) {
                        testPlanReportDTO.setProjectEnvMap(projectEnvMap);
                    }
                }
            }
            //运行模式
            testPlanReportDTO.setRunMode(StringUtils.equalsIgnoreCase(runInfoDTO.getRunMode(), "serial") ? Translator.get("serial") : Translator.get("parallel"));
        }
    }

    private boolean isDynamicallyGenerateReports(TestPlanReportContentWithBLOBs testPlanReportContent) {
        return testPlanReportContent != null &&
                (StringUtils.isNotEmpty(testPlanReportContent.getPlanApiCaseReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanScenarioReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanLoadCaseReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanUiScenarioReportStruct()));
    }

    private boolean isDynamicallyGenerateReports(String reportId) {
        return extTestPlanReportContentMapper.isDynamicallyGenerateReport(reportId);
    }

    public void createTestPlanReportContentReportIds(String testPlanReportID,
                                                     List<TestPlanApiDTO> apiTestCases,
                                                     List<TestPlanScenarioDTO> scenarioCases,
                                                     List<TestPlanUiScenarioDTO> uiScenarios,
                                                     Map<String, String> loadCaseReportIdMap) {
        TestPlanReportContentWithBLOBs content = new TestPlanReportContentWithBLOBs();
        content.setId(UUID.randomUUID().toString());
        content.setTestPlanReportId(testPlanReportID);
        if (MapUtils.isNotEmpty(loadCaseReportIdMap)) {
            content.setPlanLoadCaseReportStruct(JSON.toJSONString(loadCaseReportIdMap));
        }

        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            content.setPlanApiCaseReportStruct(JSON.toJSONString(apiTestCases));
        }
        if (CollectionUtils.isNotEmpty(scenarioCases)) {
            content.setPlanScenarioReportStruct(JSON.toJSONString(scenarioCases));
        }
        if (CollectionUtils.isNotEmpty(uiScenarios)) {
            content.setPlanUiScenarioReportStruct(JSON.toJSONString(uiScenarios));
        }

        testPlanReportContentMapper.insert(content);
    }

    private Map<String, String> parseLoadCaseReportMap(String loadReportStructStr) {
        Map<String, String> returnMap = new HashMap<>();
        if (StringUtils.isNotEmpty(loadReportStructStr)) {
            Map<String, String> caseReportList = null;
            try {
                caseReportList = JSON.parseMap(loadReportStructStr);
            } catch (Exception ignored) {
            }
            if (MapUtils.isNotEmpty(caseReportList)) {
                returnMap = caseReportList;
            }
        }
        return returnMap;
    }

    private TestPlanUiExecuteReportDTO parseUiCaseReportMap(String reportStructStr) {
        TestPlanUiExecuteReportDTO uiExecuteReportDTO = new TestPlanUiExecuteReportDTO();
        if (StringUtils.isNotEmpty(reportStructStr)) {
            Map<String, String> uiReportMap = new HashMap<>();
            Map<String, TestPlanUiScenarioDTO> uiDTOMap = new LinkedHashMap<>();
            List<TestPlanUiScenarioDTO> testPlanUiScenarioDTOList = null;
            try {
                testPlanUiScenarioDTOList = JSON.parseArray(reportStructStr, TestPlanUiScenarioDTO.class);
            } catch (Exception ignored) {
            }
            if (CollectionUtils.isNotEmpty(testPlanUiScenarioDTOList)) {
                for (TestPlanUiScenarioDTO dto : testPlanUiScenarioDTOList) {
                    uiReportMap.put(dto.getId(), dto.getReportId());
                    uiDTOMap.put(dto.getId(), dto);
                }
            }
            uiExecuteReportDTO.setTestPlanUiScenarioIdAndReportIdMap(uiReportMap);
            uiExecuteReportDTO.setUiScenarioInfoDTOMap(uiDTOMap);
        }
        return uiExecuteReportDTO;
    }

    private Map<String, String> parseCaseReportMap(String reportStructStr) {
        Map<String, String> returnMap = new HashMap<>();
        if (StringUtils.isNotEmpty(reportStructStr)) {
            List<Map> caseReportList = null;
            try {
                caseReportList = JSON.parseArray(reportStructStr, Map.class);
            } catch (Exception ignored) {
            }
            if (CollectionUtils.isEmpty(caseReportList)) {
                try {
                    returnMap = JSON.parseObject(reportStructStr, Map.class);
                } catch (Exception ignored) {
                }
            } else {
                for (Map itemMap : caseReportList) {
                    if (itemMap.containsKey("id") && itemMap.containsKey("reportId")) {
                        String id = itemMap.get("id").toString();
                        String reportId = itemMap.get("reportId").toString();
                        if (StringUtils.isNoneEmpty(id, reportId)) {
                            returnMap.put(id, reportId);
                        }
                    }
                }
            }
        }
        return returnMap;
    }

    public TestPlanCaseReportResultDTO selectCaseDetailByTestPlanReport(Map reportConfig, String testPlanId, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】开始处理用例执行结果");
        TestPlanCaseReportResultDTO reportDetailDTO = new TestPlanCaseReportResultDTO();
        //查找api测试报告结果
        if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct()) || StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct())) {
            LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】开始查找接口测试报告结果");
            try {
                Map<String, String> apiCaseReportMap = this.parseCaseReportMap(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct());
                Map<String, String> scenarioReportMap = this.parseCaseReportMap(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct());
                ApiPlanReportRequest request = new ApiPlanReportRequest();
                request.setConfig(reportConfig);
                request.setSaveResponse(false);
                if (MapUtils.isNotEmpty(apiCaseReportMap)) {
                    request.setApiReportIdList(new ArrayList<>(apiCaseReportMap.values()));
                }
                if (MapUtils.isNotEmpty(scenarioReportMap)) {
                    request.setScenarioReportIdList(new ArrayList<>(scenarioReportMap.values()));
                }
                ApiReportResultDTO apiReportResult = new ApiReportResultDTO();
                apiReportResult.setApiReportResultMap(this.selectApiCaseRunResultByIds(request.getApiReportIdList()));
                apiReportResult.setScenarioReportResultMap(this.selectScenarioRunResultByIds(request.getScenarioReportIdList()));
                reportDetailDTO.setApiPlanReportDTO(this.getApiPlanReport(reportConfig, testPlanReportContentWithBLOBs, apiReportResult));
            } catch (Exception e) {
                LogUtil.error("连接Api-test查找报告结果信息失败!", e);
            }
            LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】接口测试报告结构查找结束");
        }
        //查找性能测试报告结果
        if (DiscoveryUtil.hasService(MicroServiceName.PERFORMANCE_TEST)) {
            LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】开始查找性能测试报告结果");
            Map<String, String> testPlanLoadCaseIdAndReportIdMap = this.parseLoadCaseReportMap(testPlanReportContentWithBLOBs.getPlanLoadCaseReportStruct());
            if (MapUtils.isNotEmpty(testPlanLoadCaseIdAndReportIdMap)) {
                ApiPlanReportRequest request = new ApiPlanReportRequest();
                request.setConfig(reportConfig);
                request.setSaveResponse(false);
                request.setReportIdMap(testPlanLoadCaseIdAndReportIdMap);
                try {
                    LoadPlanReportDTO loadPlanReport = planTestPlanLoadCaseService.getLoadExecuteReport(request);
                    reportDetailDTO.setLoadPlanReportDTO(loadPlanReport);
                } catch (Exception e) {
                    LogUtil.error("连接Load-test查找报告结果信息失败!", e);
                }
            }
            LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】性能测试报告结果查找结束");
        }
        //查找UI测试报告结果
        if (DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】开始查找UI测试报告结果");
            TestPlanUiExecuteReportDTO uiExecuteReportDTO = this.parseUiCaseReportMap(testPlanReportContentWithBLOBs.getPlanUiScenarioReportStruct());
            if (MapUtils.isNotEmpty(uiExecuteReportDTO.getTestPlanUiScenarioIdAndReportIdMap())
                    && MapUtils.isNotEmpty(uiExecuteReportDTO.getUiScenarioInfoDTOMap())) {
                UiPlanReportRequest planSubReportRequest = new UiPlanReportRequest();
                planSubReportRequest.setTestPlanExecuteReportDTO(uiExecuteReportDTO);
                planSubReportRequest.setConfig(reportConfig);
                planSubReportRequest.setSaveResponse(false);
                try {
                    UiPlanReportDTO uiReport = planTestPlanUiScenarioCaseService.getUiReport(planSubReportRequest);
                    reportDetailDTO.setUiPlanReportDTO(uiReport);
                } catch (Exception e) {
                    LogUtil.error("连接Ui-test查找报告结果信息失败!", e);
                }
            }
            LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】UI测试报告结果查找结束");
        }

        //统计功能用例
        if (testPlanService.checkReportConfig(reportConfig, "functional")) {
            List<String> statusList = testPlanService.getFunctionalReportStatusList(reportConfig);
            if (statusList != null) {
                // 不等于null，说明配置了用例，根据配置的状态查询用例
                List<TestPlanCaseDTO> allCases = testPlanTestCaseService.getAllCasesByStatusList(testPlanId, statusList);
                reportDetailDTO.setFunctionCaseList(allCases);
            }

            if (TestPlanReportUtil.checkReportConfig(reportConfig, "functional", "issue")) {
                List<IssuesDao> issueList = issuesService.getIssuesByPlanId(testPlanId);
                reportDetailDTO.setIssueList(issueList);
            }
        }
        LogUtil.info("测试计划报告【" + testPlanReportContentWithBLOBs.getTestPlanReportId() + "】用例执行结果处理结束");
        return reportDetailDTO;
    }

    private Map<String, String> selectApiCaseRunResultByIds(List<String> apiReportIdList) {
        Map<String, String> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(apiReportIdList)) {
            List<ApiDefinitionExecResult> apiDefinitionExecResultList = extTestPlanApiCaseMapper.selectReportStatusByReportIds(apiReportIdList);
            if (CollectionUtils.isNotEmpty(apiDefinitionExecResultList)) {
                returnMap = apiDefinitionExecResultList.stream().collect(Collectors.toMap(ApiDefinitionExecResult::getId, ApiDefinitionExecResult::getStatus, (k1, k2) -> k1));
            }
        }
        return returnMap;
    }

    private Map<String, String> selectScenarioRunResultByIds(List<String> scenarioReportIdList) {
        Map<String, String> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(scenarioReportIdList)) {
            List<ApiScenarioReport> apiDefinitionExecResultList = extTestPlanScenarioCaseMapper.selectReportStatusByReportIds(scenarioReportIdList);
            if (CollectionUtils.isNotEmpty(apiDefinitionExecResultList)) {
                returnMap = apiDefinitionExecResultList.stream().collect(Collectors.toMap(ApiScenarioReport::getId, ApiScenarioReport::getStatus, (k1, k2) -> k1));
            }
        }
        return returnMap;
    }

    private List<TestPlanScenarioDTO> getByScenarioExecReportIds(String planScenarioReportStruct, Map<String, String> scenarioReportResultMap) {
        if (MapUtils.isEmpty(scenarioReportResultMap) || StringUtils.isEmpty(planScenarioReportStruct)) {
            return new ArrayList<>();
        }
        List<TestPlanScenarioDTO> testPlanScenarioDTOList = null;
        try {
            testPlanScenarioDTOList = JSON.parseArray(planScenarioReportStruct, TestPlanScenarioDTO.class);
            if (CollectionUtils.isNotEmpty(testPlanScenarioDTOList)) {
                String defaultStatus = ApiReportStatus.ERROR.name();
                for (TestPlanScenarioDTO dto : testPlanScenarioDTOList) {
                    String reportId = dto.getReportId();
                    dto.setReportId(reportId);
                    dto.setLastResult(scenarioReportResultMap.getOrDefault(reportId, defaultStatus));
                }
            }
        } catch (Exception e) {
            LogUtil.error("解析接口报告数据结构失败！", e);
        }
        return testPlanScenarioDTOList;
    }

    public List<TestPlanApiDTO> getByApiExecReportIds(String apiCaseReportStructStr, Map<String, String> reportResultMap) {
        if (MapUtils.isEmpty(reportResultMap) || StringUtils.isEmpty(apiCaseReportStructStr)) {
            return new ArrayList<>();
        }
        List<TestPlanApiDTO> testPlanApiDTOList = null;
        try {
            testPlanApiDTOList = JSON.parseArray(apiCaseReportStructStr, TestPlanApiDTO.class);
            if (CollectionUtils.isNotEmpty(testPlanApiDTOList)) {
                String defaultStatus = ApiReportStatus.ERROR.name();
                for (TestPlanApiDTO dto : testPlanApiDTOList) {
                    String reportId = dto.getReportId();
                    dto.setExecResult(reportResultMap.getOrDefault(reportId, defaultStatus));
                }
            }
        } catch (Exception e) {
            LogUtil.error("解析接口报告数据结构失败！", e);
        }
        return testPlanApiDTOList;
    }

    private ApiPlanReportDTO getApiPlanReport(Map config, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs, ApiReportResultDTO apiReportResult) {
        ApiPlanReportDTO report = new ApiPlanReportDTO();

        if (ServiceUtils.checkConfigEnable(config, "api")) {
            List<TestPlanApiDTO> apiAllCases = null;
            List<TestPlanScenarioDTO> scenarioAllCases = null;
            if (TestPlanReportUtil.checkReportConfig(config, "api", "all")) {
                // 接口
                apiAllCases = getByApiExecReportIds(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct(), apiReportResult.getApiReportResultMap());
                //场景
                scenarioAllCases = getByScenarioExecReportIds(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct(), apiReportResult.getScenarioReportResultMap());
                //                this.checkApiCaseCreatorName(apiAllCases, scenarioAllCases);
                report.setApiAllCases(apiAllCases);
                report.setScenarioAllCases(scenarioAllCases);
            }

            //筛选符合配置需要的执行结果的用例和场景
            TestPlanReportUtil.screenApiCaseByStatusAndReportConfig(report, apiAllCases, config);
            TestPlanReportUtil.screenScenariosByStatusAndReportConfig(report, scenarioAllCases, config);
        }
        return report;
    }


    public void cleanUpReport(long time, String projectId) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId);
        List<TestPlan> testPlans = testPlanMapper.selectByExample(testPlanExample);
        List<String> testPlanIds = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(testPlanIds)) {
            TestPlanReportExample example = new TestPlanReportExample();
            example.createCriteria().andCreateTimeLessThan(time).andTestPlanIdIn(testPlanIds);
            List<TestPlanReport> testPlanReports = testPlanReportMapper.selectByExample(example);
            List<String> ids = testPlanReports.stream().map(TestPlanReport::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(ids)) {
                deleteReportBatch(ids);
            }
        }
    }

    public void reName(String planId, String planName) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(planId);
        if (testPlanReport != null) {
            testPlanReport.setName(planName);
            testPlanReportMapper.updateByPrimaryKey(testPlanReport);
        }
    }

    public TestPlanReport checkTestPlanReportHasErrorCase(TestPlanReport report, TestPlanReportContentWithBLOBs testPlanReportContent) {
        if (testPlanReportContent != null) {
            UserDTO userDTO = baseUserService.getUserDTO(report.getCreator());
            HttpHeaderUtils.runAsUser(userDTO);
            boolean hasErrorCase = this.isTestPlanReportHasErrorCase(testPlanReportContent);
            HttpHeaderUtils.clearUser();
            if (hasErrorCase) {
                report.setStatus(TestPlanReportStatus.FAILED.name());
            } else {
                report.setStatus(TestPlanReportStatus.SUCCESS.name());
            }
            testPlanReportMapper.updateByPrimaryKeySelective(report);
        }
        return report;
    }

    private boolean isTestPlanReportHasErrorCase(TestPlanReportContentWithBLOBs content) {
        //更新测试计划里用例的执行结果，并检查是否有错误用例。
        boolean hasErrorCase = false;
        if (content != null) {
            //更新接口用例、场景用例的最终执行状态
            if (!hasErrorCase && StringUtils.isNotEmpty(content.getPlanApiCaseReportStruct())) {
                try {
                    List<TestPlanApiDTO> apiTestCases = JSON.parseArray(content.getPlanApiCaseReportStruct(), TestPlanApiDTO.class);
                    List<String> reportIdList = new ArrayList<>();
                    apiTestCases.forEach(item -> {
                        if (StringUtils.isNotEmpty(item.getReportId())) {
                            reportIdList.add(item.getReportId());
                        }
                    });
                    Map<String, String> reportResult = planApiDefinitionExecResultService.selectReportResultByReportIds(reportIdList);
                    String defaultStatus = ApiReportStatus.ERROR.name();
                    for (TestPlanApiDTO dto : apiTestCases) {
                        String reportId = dto.getReportId();
                        if (StringUtils.isEmpty(reportId)) {
                            dto.setExecResult(defaultStatus);
                        } else {
                            String execStatus = reportResult.get(reportId);
                            if (execStatus == null) {
                                execStatus = defaultStatus;
                            }
                            dto.setExecResult(execStatus);
                        }
                        if (!StringUtils.equalsAnyIgnoreCase(dto.getExecResult(), ApiReportStatus.SUCCESS.name())) {
                            hasErrorCase = true;
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("Parse test plan report api case error! ", e);
                }
            }

            if (!hasErrorCase && StringUtils.isNotEmpty(content.getPlanScenarioReportStruct())) {
                try {
                    List<TestPlanScenarioDTO> scenarioCases = JSON.parseArray(content.getPlanScenarioReportStruct(), TestPlanScenarioDTO.class);
                    List<String> reportIdList = new ArrayList<>();
                    scenarioCases.forEach(item -> {
                        if (StringUtils.isNotEmpty(item.getReportId())) {
                            reportIdList.add(item.getReportId());
                        }
                    });
                    String defaultStatus = ApiReportStatus.ERROR.name();
                    Map<String, String> reportStatus = planApiScenarioReportService.getReportStatusByReportIds(reportIdList);

                    for (TestPlanScenarioDTO dto : scenarioCases) {
                        String reportId = dto.getReportId();
                        if (StringUtils.isNotEmpty(reportId)) {
                            String execStatus = reportStatus.get(reportId);
                            if (execStatus == null) {
                                execStatus = defaultStatus;
                            } else {
                                if (StringUtils.equalsIgnoreCase(execStatus, ApiReportStatus.ERROR.name())) {
                                    execStatus = ApiReportStatus.ERROR.name();
                                }
                            }
                            dto.setLastResult(execStatus);
                            dto.setStatus(execStatus);
                            if (!StringUtils.equalsAnyIgnoreCase(execStatus, ApiReportStatus.SUCCESS.name())) {
                                hasErrorCase = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("Parse test plan report scenario case error!", e);
                }
            }

            if (!hasErrorCase && StringUtils.isNotEmpty(content.getPlanUiScenarioReportStruct())) {
                try {
                    List<TestPlanUiScenarioDTO> scenarioCases = JSON.parseArray(content.getPlanUiScenarioReportStruct(), TestPlanUiScenarioDTO.class);
                    List<String> reportIdList = new ArrayList<>();
                    scenarioCases.forEach(item -> {
                        if (StringUtils.isNotEmpty(item.getReportId())) {
                            reportIdList.add(item.getReportId());
                        }
                    });
                    String defaultStatus = "Fail";
                    Map<String, String> reportStatus = planUiScenarioReportService.getReportStatusByReportIds(reportIdList);

                    for (TestPlanUiScenarioDTO dto : scenarioCases) {
                        String reportId = dto.getReportId();
                        if (StringUtils.isNotEmpty(reportId)) {
                            String execStatus = reportStatus.get(reportId);
                            if (execStatus == null) {
                                execStatus = defaultStatus;
                            } else {
                                if (StringUtils.equalsIgnoreCase(execStatus, "Error")) {
                                    execStatus = "Fail";
                                }
                            }
                            dto.setLastResult(execStatus);
                            dto.setStatus(execStatus);
                            if (!StringUtils.equalsAnyIgnoreCase(execStatus, "success")) {
                                hasErrorCase = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("Parse test plan report ui scenario case error!", e);
                }
            }
        }
        return hasErrorCase;
    }

    /**
     * 服务异常重启处理
     */
    public void exceptionHandling() {
        LogUtil.info("开始处理服务重启导致执行未完成的报告状态");
        extTestPlanReportMapper.updateAllStatus();
        LogUtil.info("处理服务重启导致执行未完成的报告状态完成");
        LogUtil.info("开始清除测试计划相关的执行队列");
        this.deleteAllQueue();
        LogUtil.info("清除测试计划相关的执行队列完成");
    }

    public void deleteAllQueue() {
        LogUtil.info("开始清除测试计划的资源执行队列");
        //删除测试计划资源执行队列
        List<String> queueDeleteIdList = extApiExecutionQueueMapper.selectIdByReportIdIsNull();
        if (CollectionUtils.isNotEmpty(queueDeleteIdList)) {
            BatchProcessingUtil.consumerByStringList(queueDeleteIdList, this::deleteApiExecutionQueueDetail);
            BatchProcessingUtil.consumerByStringList(queueDeleteIdList, this::deleteApiExecutionQueue);
        }
        LogUtil.info("开始清除测试计划的批量串行执行队列");
        //删除测试计划串行队列
        TestPlanExecutionQueueExample example = new TestPlanExecutionQueueExample();
        testPlanExecutionQueueMapper.deleteByExample(example);
    }

    public void deleteApiExecutionQueue(List<String> queueIdList) {
        if (CollectionUtils.isNotEmpty(queueIdList)) {
            ApiExecutionQueueExample example = new ApiExecutionQueueExample();
            example.createCriteria().andIdIn(queueIdList);
            apiExecutionQueueMapper.deleteByExample(example);
        }
    }

    public void deleteApiExecutionQueueDetail(List<String> queueIdList) {
        if (CollectionUtils.isNotEmpty(queueIdList)) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdIn(queueIdList);
            apiExecutionQueueDetailMapper.deleteByExample(example);
        }
    }

    public String getLastReportByPlanId(String planId) {
        TestPlanReportExample example = new TestPlanReportExample();
        example.setOrderByClause("create_time desc");
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanReport> testPlanReports = testPlanReportMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testPlanReports)) {
            return testPlanReports.get(0).getId();
        }
        return null;
    }

    public void editReport(TestPlanReportContentWithBLOBs reportContentWithBLOBs) {
        if (StringUtils.isNotBlank(reportContentWithBLOBs.getTestPlanReportId())) {
            TestPlanReportContentExample example = new TestPlanReportContentExample();
            example.createCriteria().andTestPlanReportIdEqualTo(reportContentWithBLOBs.getTestPlanReportId());
            testPlanReportContentMapper.updateByExampleSelective(reportContentWithBLOBs, example);
        }
    }

    public String selectLastReportByTestPlanId(String testPlanId) {
        return extTestPlanReportMapper.selectLastReportByTestPlanId(testPlanId);
    }
}
