package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.cache.TestPlanExecuteInfo;
import io.metersphere.api.cache.TestPlanReportExecuteCatch;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.service.ShareInfoService;
import io.metersphere.api.exec.utils.NamedThreadFactory;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.track.domain.ReportResultComponent;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.report.QueryTestPlanReportRequest;
import io.metersphere.track.request.report.TestPlanReportSaveRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author song.tianyang
 * 2021/1/8 4:34 下午
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanReportService {

    Logger testPlanLog = LoggerFactory.getLogger("testPlanExecuteLog");

    @Resource
    TestPlanReportMapper testPlanReportMapper;
    @Resource
    ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    TestPlanReportDataMapper testPlanReportDataMapper;
    @Resource
    ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    ExtTestPlanReportMapper extTestPlanReportMapper;
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    ExtTestPlanMapper extTestPlanMapper;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    LoadTestReportMapper loadTestReportMapper;
    @Resource
    TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Lazy
    @Resource
    TestPlanService testPlanService;
    @Lazy
    @Resource
    TestPlanReportContentMapper testPlanReportContentMapper;
    @Resource
    ShareInfoService shareInfoService;
    @Resource
    private TestPlanPrincipalMapper testPlanPrincipalMapper;
    @Resource
    private UserService userService;
    @Resource
    private ProjectService projectService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20, new NamedThreadFactory("TestPlanReportService"));

    public List<TestPlanReportDTO> list(QueryTestPlanReportRequest request) {
        List<TestPlanReportDTO> list = new ArrayList<>();
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        if (StringUtils.isBlank(request.getProjectId())) {
            return list;
        }
        if (request.getCombine() != null && !request.getCombine().isEmpty()) {
            if (request.getCombine().get("status") != null) {
                HashMap<String, Object> map = (HashMap<String, Object>) request.getCombine().get("status");
                List<String> valueList = (List<String>) map.get("value");
                List<String> newVal = new ArrayList<>();
                valueList.forEach(item -> {
                    if ("Completed".equals(item)) {
                        newVal.add("success");
                        newVal.add("failed");
                        newVal.add("completed");
                    } else if ("Underway".equals(item)) {
                        newVal.add("Running");
                    } else {
                        newVal.add("Starting");
                    }
                });
                valueList.clear();
                valueList.addAll(newVal);
            }
        }
        list = extTestPlanReportMapper.list(request);
        return list;
    }

    public TestPlanScheduleReportInfoDTO genTestPlanReportBySchedule(String projectID, String planId, String userId, String triggerMode) {
        Map<String, String> planScenarioIdMap = new LinkedHashMap<>();
        Map<String, String> planTestCaseIdMap = new LinkedHashMap<>();
        Map<String, String> performanceIdMap = new LinkedHashMap<>();
        List<TestPlanApiScenario> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiScenario model : testPlanApiScenarioList) {
            planScenarioIdMap.put(model.getId(), model.getApiScenarioId());
        }
        List<TestPlanApiCase> testPlanApiCaseList = extTestPlanApiCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiCase model : testPlanApiCaseList) {
            planTestCaseIdMap.put(model.getId(), model.getApiCaseId());
        }
        LoadCaseRequest loadCaseRequest = new LoadCaseRequest();
        loadCaseRequest.setTestPlanId(planId);
        loadCaseRequest.setProjectId(projectID);
        List<TestPlanLoadCaseDTO> testPlanLoadCaseDTOList = extTestPlanLoadCaseMapper.selectTestPlanLoadCaseList(loadCaseRequest);
        for (TestPlanLoadCaseDTO dto : testPlanLoadCaseDTOList) {
            performanceIdMap.put(dto.getId(), dto.getLoadCaseId());
        }
        String planReportId = UUID.randomUUID().toString();

        Map<String, String> apiCaseInfoMap = new HashMap<>();
        for (String id : planTestCaseIdMap.keySet()) {
            apiCaseInfoMap.put(id, TestPlanApiExecuteStatus.PREPARE.name());
        }
        Map<String, String> scenarioInfoMap = new HashMap<>();
        for (String id : planScenarioIdMap.keySet()) {
            scenarioInfoMap.put(id, TestPlanApiExecuteStatus.PREPARE.name());
        }
        Map<String, String> performanceInfoMap = new HashMap<>();
        for (String id : performanceIdMap.values()) {
            performanceInfoMap.put(id, TestPlanApiExecuteStatus.PREPARE.name());
        }
        TestPlanReportSaveRequest saveRequest = new TestPlanReportSaveRequest(planReportId, planId, userId, triggerMode,
                planTestCaseIdMap.size() > 0, planScenarioIdMap.size() > 0, performanceIdMap.size() > 0,
                apiCaseInfoMap, scenarioInfoMap, performanceInfoMap);
        TestPlanReport report = this.genTestPlanReport(saveRequest);
        TestPlanScheduleReportInfoDTO returnDTO = new TestPlanScheduleReportInfoDTO();
        returnDTO.setTestPlanReport(report);
        returnDTO.setPlanScenarioIdMap(planScenarioIdMap);
        returnDTO.setApiTestCaseDataMap(planTestCaseIdMap);
        returnDTO.setPerformanceIdMap(performanceIdMap);
        return returnDTO;
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
    public TestPlanReport genTestPlanReport(TestPlanReportSaveRequest saveRequest) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(saveRequest.getPlanId());
        String testPlanReportID = saveRequest.getReportID();
        TestPlanReport testPlanReport = new TestPlanReport();
        testPlanReport.setTestPlanId(saveRequest.getPlanId());
        testPlanReport.setId(testPlanReportID);
        testPlanReport.setCreateTime(System.currentTimeMillis());
        testPlanReport.setUpdateTime(System.currentTimeMillis());
        try {
            testPlanReport.setName(testPlan.getName() + "-" + DateUtils.getTimeString(new Date()));
        } catch (Exception ignored) {
        }

        testPlanReport.setTriggerMode(saveRequest.getTriggerMode());
        testPlanReport.setCreator(saveRequest.getUserId());
        testPlanReport.setStartTime(System.currentTimeMillis());
        testPlanReport.setEndTime(System.currentTimeMillis());
        testPlanReport.setIsNew(true);

        TestPlanReportContentWithBLOBs testPlanReportContent = new TestPlanReportContentWithBLOBs();
        testPlanReportContent.setId(UUID.randomUUID().toString());
        testPlanReportContent.setTestPlanReportId(testPlanReportID);
        if (testPlanReportContent.getStartTime() == null) {
            testPlanReportContent.setStartTime(System.currentTimeMillis());
        }
        if (testPlanReportContent.getEndTime() == null) {
            testPlanReportContent.setEndTime(System.currentTimeMillis());
        }

        Map<String, String> apiCaseInfoMap = new HashMap<>();
        Map<String, String> scenarioInfoMap = new HashMap<>();
        Map<String, String> performanceInfoMap = new HashMap<>();

        if (saveRequest.isCountResources()) {
            List<TestPlanApiCase> testPlanApiCaseList = extTestPlanApiCaseMapper.selectLegalDataByTestPlanId(saveRequest.getPlanId());
            List<String> apiCaseIdList = testPlanApiCaseList.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
            testPlanReport.setIsApiCaseExecuting(!apiCaseIdList.isEmpty());

            List<TestPlanApiScenario> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectLegalDataByTestPlanId(saveRequest.getPlanId());
            List<String> scenarioIdList = testPlanApiScenarioList.stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList());
            testPlanReport.setIsScenarioExecuting(!scenarioIdList.isEmpty());

            LoadCaseRequest loadCaseRequest = new LoadCaseRequest();
            loadCaseRequest.setTestPlanId(saveRequest.getPlanId());
            loadCaseRequest.setProjectId(testPlan.getProjectId());
            List<String> performanceIdList = extTestPlanLoadCaseMapper.selectTestPlanLoadCaseList(loadCaseRequest)
                    .stream().map(TestPlanLoadCaseDTO::getLoadCaseId).collect(Collectors.toList());
            testPlanReport.setIsPerformanceExecuting(!performanceIdList.isEmpty());

            for (String id : apiCaseIdList) {
                apiCaseInfoMap.put(id, TestPlanApiExecuteStatus.PREPARE.name());
            }
            for (String id : scenarioIdList) {
                scenarioInfoMap.put(id, TestPlanApiExecuteStatus.PREPARE.name());
            }
            for (String id : performanceIdList) {
                performanceInfoMap.put(id, TestPlanApiExecuteStatus.PREPARE.name());
            }
        } else {
            testPlanReport.setIsApiCaseExecuting(saveRequest.isApiCaseIsExecuting());
            testPlanReport.setIsScenarioExecuting(saveRequest.isScenarioIsExecuting());
            testPlanReport.setIsPerformanceExecuting(saveRequest.isPerformanceIsExecuting());

            apiCaseInfoMap = saveRequest.getApiCaseIdMap();
            scenarioInfoMap = saveRequest.getScenarioIdMap();
            performanceInfoMap = saveRequest.getPerformanceIdMap();
        }

        TestPlanReportExecuteCatch.addApiTestPlanExecuteInfo(testPlanReportID, saveRequest.getUserId(), apiCaseInfoMap, scenarioInfoMap, performanceInfoMap);

        if (testPlanReport.getIsScenarioExecuting() || testPlanReport.getIsApiCaseExecuting() || testPlanReport.getIsPerformanceExecuting()) {
            testPlanReport.setStatus(TestPlanReportStatus.RUNNING.name());
        } else {
            testPlanReport.setStatus(TestPlanReportStatus.COMPLETED.name());
        }

        testPlanReportMapper.insert(testPlanReport);
        testPlanReportContentMapper.insert(testPlanReportContent);

        //更新TestPlan状态，改为进行中
        testPlan.setStatus(TestPlanStatus.Underway.name());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
        return testPlanReport;
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
                    returnDTO.setExecuteResult(JSONObject.parseObject(reportData.getExecuteResult(), TestCaseReportAdvanceStatusResultDTO.class));
                }
                if (!StringUtils.isEmpty(reportData.getModuleExecuteResult())) {
                    returnDTO.setModuleExecuteResult(JSONArray.parseArray(reportData.getModuleExecuteResult(), TestCaseReportModuleResultDTO.class));
                }
                if (!StringUtils.isEmpty(reportData.getFailurTestCases())) {
                    returnDTO.setFailureTestCases(JSONObject.parseObject(reportData.getFailurTestCases(), FailureTestCasesAdvanceDTO.class));
                }
                if (!StringUtils.isEmpty(reportData.getIssuesInfo())) {
                    returnDTO.setIssues(JSONArray.parseArray(reportData.getIssuesInfo(), Issues.class));
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
            return "";
        }
        String principalName = "";
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

    public synchronized void updateReport(List<String> testPlanReportIdList, String runMode, String triggerMode) {
        for (String planReportId : testPlanReportIdList) {
            this.countReportByTestPlanReportId(planReportId, runMode, triggerMode, null);
        }
    }

    public synchronized void updateReportByScenarioIdList(List<String> testPlanReportIdList, String runMode, String triggerMode, List<String> scenarioIdList) {
        for (String planReportId : testPlanReportIdList) {
            this.countReportByTestPlanReportId(planReportId, runMode, triggerMode, scenarioIdList);
        }
    }

    public TestCaseReportMetricDTO countReportData(TestPlanDTO testPlan, Map<String, Map<String, String>> executeResult) {
        TestCaseReportMetricDTO returnDTO = new TestCaseReportMetricDTO();
        ReportResultComponent reportResultComponent = new ReportResultComponent(testPlan);
        reportResultComponent.afterBuild(returnDTO);

        TestCaseReportAdvanceStatusResultDTO statusDTO = new TestCaseReportAdvanceStatusResultDTO();
        Map<String, String> apiCaseExecuteMap = executeResult.get(TestPlanResourceType.API_CASE.name());
        Map<String, String> scenarioExecuteMap = executeResult.get(TestPlanResourceType.SCENARIO_CASE.name());
        Map<String, String> performanceCaseExecuteMap = executeResult.get(TestPlanResourceType.PERFORMANCE_CASE.name());

        List<TestCaseReportStatusResultDTO> apiResult = new ArrayList<>();
        List<TestCaseReportStatusResultDTO> scenarioResult = new ArrayList<>();
        List<TestCaseReportStatusResultDTO> loadResult = new ArrayList<>();

        List<String> faliureApiCaseIdList = new ArrayList<>();
        List<String> faliureScenarioCaseIdList = new ArrayList<>();
        List<String> faliureLoadCaseIdList = new ArrayList<>();

        if (MapUtils.isNotEmpty(apiCaseExecuteMap)) {
            Map<String, Integer> countMap = new HashMap<>();
            for (Map.Entry<String, String> executeEntry : apiCaseExecuteMap.entrySet()) {
                String caseResult = executeEntry.getValue();
                String id = executeEntry.getKey();
                if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.SUCCESS.name())) {
                    if (countMap.containsKey("Pass")) {
                        countMap.put("Pass", countMap.get("Pass") + 1);
                    } else {
                        countMap.put("Pass", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.FAILD.name())) {
                    faliureApiCaseIdList.add(id);
                    if (countMap.containsKey("Failure")) {
                        countMap.put("Failure", countMap.get("Failure") + 1);
                    } else {
                        countMap.put("Failure", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.PREPARE.name())) {
                    faliureApiCaseIdList.add(id);
                    if (countMap.containsKey("Skip")) {
                        countMap.put("Skip", countMap.get("Skip") + 1);
                    } else {
                        countMap.put("Skip", 1);
                    }
                } else {
                    if (countMap.containsKey("Underway")) {
                        countMap.put("Underway", countMap.get("Underway") + 1);
                    } else {
                        countMap.put("Underway", 1);
                    }
                }
            }
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                String status = entry.getKey();
                Integer value = entry.getValue();
                TestCaseReportStatusResultDTO dto = new TestCaseReportStatusResultDTO();
                dto.setStatus(status);
                dto.setCount(value);
                apiResult.add(dto);
            }
        }

        if (MapUtils.isNotEmpty(scenarioExecuteMap)) {
            Map<String, Integer> countMap = new HashMap<>();
            for (Map.Entry<String, String> executeEntry : scenarioExecuteMap.entrySet()) {
                String caseResult = executeEntry.getValue();
                String id = executeEntry.getKey();
                if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.SUCCESS.name())) {
                    if (countMap.containsKey("Pass")) {
                        countMap.put("Pass", countMap.get("Pass") + 1);
                    } else {
                        countMap.put("Pass", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.FAILD.name())) {
                    faliureApiCaseIdList.add(id);
                    if (countMap.containsKey("Failure")) {
                        countMap.put("Failure", countMap.get("Failure") + 1);
                    } else {
                        countMap.put("Failure", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.PREPARE.name())) {
                    faliureApiCaseIdList.add(id);
                    if (countMap.containsKey("Skip")) {
                        countMap.put("Skip", countMap.get("Skip") + 1);
                    } else {
                        countMap.put("Skip", 1);
                    }
                } else {
                    if (countMap.containsKey("Underway")) {
                        countMap.put("Underway", countMap.get("Underway") + 1);
                    } else {
                        countMap.put("Underway", 1);
                    }
                }
            }
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                String status = entry.getKey();
                Integer value = entry.getValue();
                TestCaseReportStatusResultDTO dto = new TestCaseReportStatusResultDTO();
                dto.setStatus(status);
                dto.setCount(value);
                scenarioResult.add(dto);
            }
        }

        if (MapUtils.isNotEmpty(performanceCaseExecuteMap)) {
            Map<String, Integer> countMap = new HashMap<>();
            for (Map.Entry<String, String> executeEntry : performanceCaseExecuteMap.entrySet()) {
                String caseResult = executeEntry.getValue();
                String id = executeEntry.getKey();
                if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.SUCCESS.name())) {
                    if (countMap.containsKey("Pass")) {
                        countMap.put("Pass", countMap.get("Pass") + 1);
                    } else {
                        countMap.put("Pass", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.FAILD.name())) {
                    faliureApiCaseIdList.add(id);
                    if (countMap.containsKey("Failure")) {
                        countMap.put("Failure", countMap.get("Failure") + 1);
                    } else {
                        countMap.put("Failure", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(caseResult, TestPlanApiExecuteStatus.PREPARE.name())) {
                    faliureApiCaseIdList.add(id);
                    if (countMap.containsKey("Skip")) {
                        countMap.put("Skip", countMap.get("Skip") + 1);
                    } else {
                        countMap.put("Skip", 1);
                    }
                } else {
                    if (countMap.containsKey("Underway")) {
                        countMap.put("Underway", countMap.get("Underway") + 1);
                    } else {
                        countMap.put("Underway", 1);
                    }
                }
            }
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                String status = entry.getKey();
                Integer value = entry.getValue();
                TestCaseReportStatusResultDTO dto = new TestCaseReportStatusResultDTO();
                dto.setStatus(status);
                dto.setCount(value);
                loadResult.add(dto);
            }
        }
        statusDTO.setApiResult(apiResult);
        statusDTO.setScenarioResult(scenarioResult);
        statusDTO.setLoadResult(loadResult);

        returnDTO.setExecuteResult(statusDTO);

        //统计失败用例
        FailureTestCasesAdvanceDTO failureDto = new FailureTestCasesAdvanceDTO();
        failureDto.setFunctionalTestCases(new ArrayList<>());
        if (!faliureApiCaseIdList.isEmpty()) {
            TestPlanApiCaseService testPlanApiCaseService = CommonBeanFactory.getBean(TestPlanApiCaseService.class);
            ApiTestCaseRequest request = new ApiTestCaseRequest();
            request.setPlanId(testPlan.getId());
            request.setIds(faliureApiCaseIdList);
            List<TestPlanApiCaseDTO> testPlanApiCaseDTOList = testPlanApiCaseService.list(request);
            failureDto.setApiTestCases(testPlanApiCaseDTOList);
        } else {
            failureDto.setApiTestCases(new ArrayList<>());
        }
        if (!faliureScenarioCaseIdList.isEmpty()) {
            TestPlanScenarioCaseService testPlanScenarioCaseService = CommonBeanFactory.getBean(TestPlanScenarioCaseService.class);

            TestPlanScenarioRequest request = new TestPlanScenarioRequest();
            request.setPlanId(testPlan.getId());
            request.setScenarioIds(faliureScenarioCaseIdList);
            List<ApiScenarioDTO> scenarioDTOS = testPlanScenarioCaseService.list(request);
            failureDto.setScenarioTestCases(scenarioDTOS);
        } else {
            failureDto.setScenarioTestCases(new ArrayList<>());
        }
        if (!faliureLoadCaseIdList.isEmpty()) {
            TestPlanLoadCaseService testPlanLoadCaseService = CommonBeanFactory.getBean(TestPlanLoadCaseService.class);
            LoadCaseRequest request = new LoadCaseRequest();
            request.setTestPlanId(testPlan.getId());
            request.setIds(faliureLoadCaseIdList);
            List<TestPlanLoadCaseDTO> loadDTOs = testPlanLoadCaseService.list(request);
            failureDto.setLoadTestCases(loadDTOs);
        } else {
            failureDto.setLoadTestCases(new ArrayList<>());
        }
        returnDTO.setFailureTestCases(failureDto);

        return returnDTO;
    }

    public TestPlanReport updateReport(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs reportContent, TestPlanExecuteInfo executeInfo) {
        if (testPlanReport == null || executeInfo == null) {
            return null;
        }

        boolean apiCaseIsOk = executeInfo.isApiCaseAllExecuted();
        boolean scenarioIsOk = executeInfo.isScenarioAllExecuted();
        boolean performanceIsOk = executeInfo.isLoadCaseAllExecuted();

        if (apiCaseIsOk) {
            testPlanReport.setIsApiCaseExecuting(false);
        }
        if (scenarioIsOk) {
            testPlanReport.setIsScenarioExecuting(false);
        }
        if (performanceIsOk) {
            testPlanReport.setIsPerformanceExecuting(false);
        }

        int[] componentIndexArr = new int[]{1, 3, 4};
        testPlanReport.setComponents(JSONArray.toJSONString(componentIndexArr));

        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);

        Map<String, Map<String, String>> testPlanExecuteResult = executeInfo.getExecutedResult();
        testPlanLog.info("ReportId[" + testPlanReport.getId() + "] COUNT OVER. COUNT RESULT :" + JSONObject.toJSONString(testPlanExecuteResult));

        TestPlanSimpleReportDTO reportDTO = testPlanService.buildPlanReport(executeInfo, testPlanReport.getTestPlanId(), apiCaseIsOk && scenarioIsOk && performanceIsOk);
        reportDTO.setStartTime(testPlanReport.getStartTime());
        long endTime = System.currentTimeMillis();
        //全部结束时，更新时间
        if (apiCaseIsOk && scenarioIsOk && performanceIsOk) {
            reportDTO.setEndTime(endTime);
            //如果测试案例没有未结束的功能用例，则更新最后结束日期。
            TestPlanTestCaseMapper testPlanTestCaseMapper = CommonBeanFactory.getBean(TestPlanTestCaseMapper.class);
            TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
            testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(testPlanReport.getTestPlanId()).andStatusNotEqualTo("Prepare");
            long testCaseCount = testPlanTestCaseMapper.countByExample(testPlanTestCaseExample);
            boolean updateTestPlanTime = testCaseCount > 0;
            if (updateTestPlanTime) {
                testPlanReport.setEndTime(endTime);
                testPlanReport.setUpdateTime(endTime);
            }
            TestPlanReportExecuteCatch.remove(testPlanReport.getId());
            testPlanLog.info("Task is finish. Remove listener:" + testPlanReport.getId());
        }
        testPlanReportContentMapper.updateByPrimaryKeySelective(parseReportDaoToReportContent(reportDTO, reportContent));

        String testPlanStatus = this.getTestPlanReportStatus(testPlanReport, reportDTO);
        testPlanReport.setStatus(testPlanStatus);

        testPlanReport = this.update(testPlanReport);
        return testPlanReport;
    }

    /**
     * @param planReportId    测试计划报告ID
     * @param resourceRunMode 资源的运行模式,triggerMode非Scedule可以为null
     * @param triggerMode     触发方式  ReportTriggerMode.enum
     */
    public void countReportByTestPlanReportId(String planReportId, String resourceRunMode, String triggerMode, List<String> scenarioIdList) {
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
        TestPlanSimpleReportDTO reportDTO = testPlanService.buildPlanReport(testPlan.getId(), false);
        if (!testPlanReportContentList.isEmpty()) {
            testPlanReportContent = testPlanReportContentList.get(0);
            testPlanReportContentMapper.updateByPrimaryKeySelective(parseReportDaoToReportContent(reportDTO, testPlanReportContent));
        }

        if (reportDTO.getStartTime() == null) {
            reportDTO.setStartTime(System.currentTimeMillis());
        }

        if (reportDTO.getEndTime() == null) {
            reportDTO.setEndTime(System.currentTimeMillis());
        }

        String testPlanStatus = this.getTestPlanReportStatus(testPlanReport, reportDTO);
        testPlanReport.setStatus(testPlanStatus);
        this.update(testPlanReport);
    }

    public TestPlanReportContentWithBLOBs parseReportDaoToReportContent(TestPlanSimpleReportDTO reportDTO, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        String id = testPlanReportContentWithBLOBs.getId();
        BeanUtils.copyBean(testPlanReportContentWithBLOBs, reportDTO);
        testPlanReportContentWithBLOBs.setId(id);
        if (reportDTO.getFunctionResult() != null) {
            testPlanReportContentWithBLOBs.setFunctionResult(JSONObject.toJSONString(reportDTO.getFunctionResult()));
        }
        if (reportDTO.getApiResult() != null) {
            testPlanReportContentWithBLOBs.setApiResult(JSONObject.toJSONString(reportDTO.getApiResult()));
        }
        if (reportDTO.getLoadResult() != null) {
            testPlanReportContentWithBLOBs.setLoadResult(JSONObject.toJSONString(reportDTO.getLoadResult()));
        }
        if (reportDTO.getFunctionAllCases() != null) {
            testPlanReportContentWithBLOBs.setFunctionAllCases(JSONObject.toJSONString(reportDTO.getFunctionAllCases()));
        }
        if (reportDTO.getFunctionFailureCases() != null) {
            testPlanReportContentWithBLOBs.setFunctionFailureCases(JSONObject.toJSONString(reportDTO.getFunctionFailureCases()));
        }
        if (reportDTO.getIssueList() != null) {
            testPlanReportContentWithBLOBs.setIssueList(JSONObject.toJSONString(reportDTO.getIssueList()));
        }
        if (reportDTO.getApiAllCases() != null) {
            testPlanReportContentWithBLOBs.setApiAllCases(JSONObject.toJSONString(reportDTO.getApiAllCases()));
        }
        if (reportDTO.getApiFailureCases() != null) {
            testPlanReportContentWithBLOBs.setApiFailureCases(JSONObject.toJSONString(reportDTO.getApiFailureCases()));
        }
        if (reportDTO.getScenarioAllCases() != null) {
            testPlanReportContentWithBLOBs.setScenarioAllCases(JSONObject.toJSONString(reportDTO.getScenarioAllCases()));
        }
        if (reportDTO.getScenarioFailureCases() != null) {
            testPlanReportContentWithBLOBs.setScenarioFailureCases(JSONObject.toJSONString(reportDTO.getScenarioFailureCases()));
        }
        if (reportDTO.getLoadAllCases() != null) {
            testPlanReportContentWithBLOBs.setLoadAllCases(JSONObject.toJSONString(reportDTO.getLoadAllCases()));
        }
        if (reportDTO.getLoadFailureCases() != null) {
            testPlanReportContentWithBLOBs.setLoadFailureCases(JSONObject.toJSONString(reportDTO.getLoadFailureCases()));
        }
        return testPlanReportContentWithBLOBs;
    }

    /**
     * 计算测试计划的状态
     *
     * @param testPlanReport
     * @return
     */
    private String getTestPlanReportStatus(TestPlanReport testPlanReport, TestPlanSimpleReportDTO reportDTO) {
        String status = TestPlanReportStatus.COMPLETED.name();
        if (testPlanReport != null) {
            if (testPlanReport.getIsApiCaseExecuting() || testPlanReport.getIsPerformanceExecuting() || testPlanReport.getIsScenarioExecuting()) {
                status = TestPlanReportStatus.RUNNING.name();
            } else {
                if (reportDTO != null) {
                    status = TestPlanReportStatus.SUCCESS.name();
                    try {
                        if (CollectionUtils.isNotEmpty(reportDTO.getFunctionFailureCases())
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

    private String getTestPlanReportStatus(TestPlanReport testPlanReport, TestPlanReportDataWithBLOBs testPlanReportData) {
        String status = TestPlanReportStatus.COMPLETED.name();
        if (testPlanReport != null) {
            if (testPlanReport.getIsApiCaseExecuting() || testPlanReport.getIsPerformanceExecuting() || testPlanReport.getIsScenarioExecuting()) {
                status = TestPlanReportStatus.RUNNING.name();
            } else {
                if (testPlanReportData != null) {
                    String failCaseString = testPlanReportData.getFailurTestCases();
                    status = TestPlanReportStatus.SUCCESS.name();
                    try {
                        JSONObject failurCaseObject = JSONObject.parseObject(failCaseString);
                        if (failurCaseObject.containsKey("apiTestCases") && failurCaseObject.getJSONArray("apiTestCases").size() >= 0) {
                            JSONArray array = failurCaseObject.getJSONArray("apiTestCases");
                            if (array.size() > 0) {
                                status = TestPlanReportStatus.FAILED.name();
                                return status;
                            }
                        }
                        if (failurCaseObject.containsKey("loadTestCases") && failurCaseObject.getJSONArray("loadTestCases").size() >= 0) {
                            JSONArray array = failurCaseObject.getJSONArray("loadTestCases");
                            if (array.size() > 0) {
                                status = TestPlanReportStatus.FAILED.name();
                                return status;
                            }
                        }
                        if (failurCaseObject.containsKey("scenarioTestCases") && failurCaseObject.getJSONArray("scenarioTestCases").size() >= 0) {
                            JSONArray array = failurCaseObject.getJSONArray("scenarioTestCases");
                            if (array.size() > 0) {
                                status = TestPlanReportStatus.FAILED.name();
                                return status;
                            }
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

    public TestPlanReport update(TestPlanReport report) {
        if (!report.getIsApiCaseExecuting() && !report.getIsPerformanceExecuting() && !report.getIsScenarioExecuting())
            try {
                //更新TestPlan状态为完成
                TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(report.getTestPlanId());
                if (testPlan != null) {
                    testPlanService.checkStatus(testPlan);
                }
                if (testPlan != null && StringUtils.equalsAny(report.getTriggerMode(),
                        ReportTriggerMode.MANUAL.name(),
                        ReportTriggerMode.API.name(),
                        ReportTriggerMode.SCHEDULE.name())
                ) {
                    //发送通知
                    sendMessage(report, testPlan.getProjectId());
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        testPlanReportMapper.updateByPrimaryKey(report);
        return report;
    }

    public void sendMessage(TestPlanReport testPlanReport, String projectId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
        assert testPlan != null;
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/track/testPlan/reportList";
        String subject = "";
        String event = "";
        String successContext = "${operator}执行的 ${name} 测试计划运行成功, 报告: ${planShareUrl}";
        String failedContext = "${operator}执行的 ${name} 测试计划运行失败, 报告: ${planShareUrl}";
        String context = "${operator}完成了测试计划: ${name}";
        if (StringUtils.equals(testPlanReport.getTriggerMode(), ReportTriggerMode.API.name())) {
            subject = Translator.get("task_notification_jenkins");
        } else {
            subject = Translator.get("task_notification");
        }

        if (StringUtils.equals(TestPlanReportStatus.FAILED.name(), testPlanReport.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        } else {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        String creator = testPlanReport.getCreator();
        UserDTO userDTO = userService.getUserDTO(creator);

        Map paramMap = new HashMap();
        paramMap.put("type", "testPlan");
        paramMap.put("url", url);
        paramMap.put("projectId", projectId);
        if (userDTO != null) {
            paramMap.put("operator", userDTO.getName());
        }
        paramMap.putAll(new BeanMap(testPlan));

        String successfulMailTemplate = "TestPlanSuccessfulNotification";
        String errfoMailTemplate = "TestPlanFailedNotification";

        String testPlanShareUrl = shareInfoService.getTestPlanShareUrl(testPlanReport.getId());
        paramMap.put("planShareUrl", baseSystemConfigDTO.getUrl() + "/sharePlanReport" + testPlanShareUrl);

        NoticeModel noticeModel = NoticeModel.builder()
                .operator(creator)
                .context(context)
                .successContext(successContext)
                .successMailTemplate(successfulMailTemplate)
                .failedContext(failedContext)
                .failedMailTemplate(errfoMailTemplate)
                .mailTemplate("track/TestPlanComplete")
                .testId(testPlan.getId())
                .status(testPlanReport.getStatus())
                .event(event)
                .subject(subject)
                .paramMap(paramMap)
                .build();

        if (StringUtils.equals(testPlanReport.getTriggerMode(), ReportTriggerMode.MANUAL.name())) {
            noticeModel.setEvent(NoticeConstants.Event.COMPLETE);
            noticeSendService.send(projectService.getProjectById(projectId), NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
        }

        if (StringUtils.equalsAny(testPlanReport.getTriggerMode(), ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())) {
            noticeSendService.send(testPlanReport.getTriggerMode(), NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
        }
    }

    public TestPlanReport getTestPlanReport(String planId) {
        return testPlanReportMapper.selectByPrimaryKey(planId);
    }

    /**
     * 更新TestPlanReportData的PerformanceInfo
     *
     * @param testPlanReport
     */
    public void updatePerformanceInfo(TestPlanReport testPlanReport, Map<String, String> performaneReportIDMap, String triggerMode) {

        /**
         * 虽然kafka已经设置了topic推送，但是当执行机器性能不够时会影响到报告状态当修改
         * 同时如果执行过程中报告删除，那么此时也应当记为失败。
         */
        Map<String, String> finishLoadTestId = new HashMap<>();
        Map<String, String> caseReportMap = new HashMap<>();
        executorService.submit(() -> {
            //错误数据检查集合。 如果错误数据出现超过20次，则取消该条数据的检查
            Map<String, Integer> errorDataCheckMap = new HashMap<>();
            List<String> performaneReportIDList = new ArrayList<>(performaneReportIDMap.keySet());
            while (performaneReportIDList.size() > 0) {
                List<String> selectList = new ArrayList<>(performaneReportIDList);
                testPlanLog.info("TestPlanReportId[" + testPlanReport.getId() + "] SELECT performance BATCH START:" + JSONArray.toJSONString(selectList));
                for (String loadTestReportId : selectList) {
                    try {
                        LoadTestReportWithBLOBs loadTestReportFromDatabase = loadTestReportMapper.selectByPrimaryKey(loadTestReportId);
                        if (loadTestReportFromDatabase == null) {
                            testPlanLog.info("TestPlanReportId[" + testPlanReport.getId() + "] SELECT performance ID:" + loadTestReportId + ",RESULT IS NULL");
                            //检查错误数据
                            if (errorDataCheckMap.containsKey(loadTestReportId)) {
                                if (errorDataCheckMap.get(loadTestReportId) > 10) {
                                    performaneReportIDList.remove(loadTestReportId);
                                    if (performaneReportIDMap.containsKey(loadTestReportId)) {
                                        finishLoadTestId.put(performaneReportIDMap.get(loadTestReportId), TestPlanLoadCaseStatus.error.name());
                                        caseReportMap.put(performaneReportIDMap.get(loadTestReportId), loadTestReportId);
                                    }
                                } else {
                                    errorDataCheckMap.put(loadTestReportId, errorDataCheckMap.get(loadTestReportId) + 1);
                                }
                            } else {
                                errorDataCheckMap.put(loadTestReportId, 1);
                            }
                        } else {
                            testPlanLog.info("TestPlanReportId[" + testPlanReport.getId() + "] SELECT performance ID:" + loadTestReportId + ",RESULT :" + loadTestReportFromDatabase.getStatus());
                            if (StringUtils.equalsAny(loadTestReportFromDatabase.getStatus(),
                                    PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
                                finishLoadTestId.put(performaneReportIDMap.get(loadTestReportId), TestPlanLoadCaseStatus.success.name());
                                caseReportMap.put(performaneReportIDMap.get(loadTestReportId), loadTestReportId);
                                performaneReportIDList.remove(loadTestReportId);
                            }
                        }
                    } catch (Exception e) {
                        performaneReportIDList.remove(loadTestReportId);
                        finishLoadTestId.put(performaneReportIDMap.get(loadTestReportId), TestPlanLoadCaseStatus.error.name());
                        caseReportMap.put(performaneReportIDMap.get(loadTestReportId), loadTestReportId);
                        testPlanLog.error(e.getMessage());
                    }
                }
                testPlanLog.info("TestPlanReportId[" + testPlanReport.getId() + "] SELECT performance BATCH OVER:" + JSONArray.toJSONString(selectList));
                if (performaneReportIDList.isEmpty()) {
                    testPlanLog.info("TestPlanReportId[" + testPlanReport.getId() + "] performance EXECUTE OVER. TRIGGER_MODE:" + triggerMode + ",REsult:" + JSONObject.toJSONString(finishLoadTestId));
                    if (StringUtils.equalsAnyIgnoreCase(triggerMode, ReportTriggerMode.API.name() ,ReportTriggerMode.MANUAL.name())) {
                        for (String string : finishLoadTestId.keySet()) {
                            String reportId = caseReportMap.get(string);
                            TestPlanLoadCaseWithBLOBs updateDTO = new TestPlanLoadCaseWithBLOBs();
                            updateDTO.setId(string);
                            updateDTO.setStatus(finishLoadTestId.get(string));
                            updateDTO.setLoadReportId(reportId);
                            testPlanLoadCaseMapper.updateByPrimaryKeySelective(updateDTO);
                        }
                    }
                    TestPlanReportExecuteCatch.updateApiTestPlanExecuteInfo(testPlanReport.getId(), null, null, finishLoadTestId);
                    TestPlanReportExecuteCatch.updateTestPlanThreadInfo(testPlanReport.getId(), null, null, caseReportMap);
                } else {
                    try {
                        //查询定时任务是否关闭
                        Thread.sleep(1000 * 10);// 检查 loadtest 的状态
                    } catch (InterruptedException e) {
                    }
                }
            }
            return true;
        });
    }

    public void updatePerformanceTestStatus(TestPlanLoadCaseEventDTO eventDTO) {
        List<String> testPlanReportId = extTestPlanMapper.findIdByPerformanceReportId(eventDTO.getReportId());
        if (StringUtils.equals(eventDTO.getTriggerMode(), ReportTriggerMode.API.name())) {
            this.updateReport(testPlanReportId, ApiRunMode.JENKINS_PERFORMANCE_TEST.name(), eventDTO.getTriggerMode());
        } else {
            this.updateReport(testPlanReportId, ApiRunMode.SCHEDULE_PERFORMANCE_TEST.name(), eventDTO.getTriggerMode());
        }
    }

    public void delete(List<String> testPlanReportIdList) {
        for (String testPlanReportId : testPlanReportIdList) {
            testPlanReportMapper.deleteByPrimaryKey(testPlanReportId);
            TestPlanReportDataExample example = new TestPlanReportDataExample();
            example.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId);
            testPlanReportDataMapper.deleteByExample(example);
            TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
            contentExample.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId);
            testPlanReportContentMapper.deleteByExample(contentExample);
        }
    }

    public List<TestPlanReport> getReports(List<String> ids) {
        TestPlanReportExample example = new TestPlanReportExample();
        example.createCriteria().andIdIn(ids);
        return testPlanReportMapper.selectByExample(example);
    }

    public void delete(QueryTestPlanReportRequest request) {
        List<String> deleteReportIds = request.getDataIds();
        if (request.isSelectAllDate()) {
            deleteReportIds = this.getAllApiIdsByFontedSelect(request.getFilters(), request.getName(), request.getProjectId(), request.getUnSelectIds());
        }
        if (CollectionUtils.isNotEmpty(deleteReportIds)) {
            TestPlanReportExample deleteReportExample = new TestPlanReportExample();
            deleteReportExample.createCriteria().andIdIn(deleteReportIds);
            testPlanReportMapper.deleteByExample(deleteReportExample);


            TestPlanReportDataExample example = new TestPlanReportDataExample();
            example.createCriteria().andTestPlanReportIdIn(deleteReportIds);
            testPlanReportDataMapper.deleteByExample(example);

            TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
            contentExample.createCriteria().andTestPlanReportIdIn(deleteReportIds);
            testPlanReportContentMapper.deleteByExample(contentExample);
        }
    }

    private List<String> getAllApiIdsByFontedSelect(Map<String, List<String>> filters, String name, String projectId, List<String> unSelectIds) {
        QueryTestPlanReportRequest request = new QueryTestPlanReportRequest();
        request.setFilters(filters);
        request.setName(name);
        request.setProjectId(projectId);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
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

    public synchronized TestPlanReport updateExecuteApis(String planReportId) {

        TestPlanExecuteInfo executeInfo = TestPlanReportExecuteCatch.getTestPlanExecuteInfo(planReportId);
        if (executeInfo == null) {
            return null;
        }
        Map<String, String> executeApiCaseIdMap = executeInfo.getApiCaseExecInfo();
        Map<String, String> executeScenarioCaseIdMap = executeInfo.getApiScenarioCaseExecInfo();
        Map<String, String> executePerformanceIdMap = executeInfo.getLoadCaseExecInfo();
        if (executeApiCaseIdMap == null) {
            executeApiCaseIdMap = new HashMap<>();
        }
        if (executeScenarioCaseIdMap == null) {
            executeScenarioCaseIdMap = new HashMap<>();
        }
        if (executePerformanceIdMap == null) {
            executePerformanceIdMap = new HashMap<>();
        }

        testPlanLog.info("ReportId[" + planReportId + "] Executed. api :" + JSONObject.toJSONString(executeApiCaseIdMap) + "; scenario:" + JSONObject.toJSONString(executeScenarioCaseIdMap) + "; performance:" + JSONObject.toJSONString(executePerformanceIdMap));

        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId);
        List<TestPlanReportContentWithBLOBs> reportDataList = testPlanReportContentMapper.selectByExampleWithBLOBs(example);

        TestPlanReport report = null;
        if (!reportDataList.isEmpty()) {
            TestPlanReportExecuteCatch.setReportDataCheckResult(planReportId, true);
            TestPlanReportContentWithBLOBs reportData = reportDataList.get(0);
            report = testPlanReportMapper.selectByPrimaryKey(planReportId);
            report = this.updateReport(report, reportData, executeInfo);
        } else {
            TestPlanReportExecuteCatch.setReportDataCheckResult(planReportId, false);
        }

        return report;
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

    public void countReport(String planReportId) {
        TestPlanReportExecuteCheckResultDTO checkResult = this.checkTestPlanReportIsTimeOut(planReportId);
        testPlanLog.info("Check PlanReport:" + planReportId + "; result: "+ JSON.toJSONString(checkResult));
        if (checkResult.isTimeOut()) {
            //判断是否超时。超时时强行停止任务
            TestPlanReportExecuteCatch.finishAllTask(planReportId);
            checkResult.setFinishedCaseChanged(true);
        }
        if(checkResult.isFinishedCaseChanged()){
            this.updateExecuteApis(planReportId);
        }
    }

    public TestPlanSimpleReportDTO getReport(String reportId) {
        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(reportId);
        List<TestPlanReportContentWithBLOBs> testPlanReportContents = testPlanReportContentMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(testPlanReportContents)) {
            return null;
        }
        TestPlanReportContentWithBLOBs testPlanReportContent = testPlanReportContents.get(0);
        //更新测试报告对应的最后执行结果
        this.updateReportExecResult(testPlanReportContent);
        TestPlanSimpleReportDTO testPlanReportDTO = new TestPlanSimpleReportDTO();
        BeanUtils.copyBean(testPlanReportDTO, testPlanReportContent);
        if (StringUtils.isNotBlank(testPlanReportContent.getFunctionResult())) {
            testPlanReportDTO.setFunctionResult(JSONObject.parseObject(testPlanReportContent.getFunctionResult(), TestPlanFunctionResultReportDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getApiResult())) {
            testPlanReportDTO.setApiResult(JSONObject.parseObject(testPlanReportContent.getApiResult(), TestPlanApiResultReportDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getLoadResult())) {
            testPlanReportDTO.setLoadResult(JSONObject.parseObject(testPlanReportContent.getLoadResult(), TestPlanLoadResultReportDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getFunctionAllCases())) {
            testPlanReportDTO.setFunctionAllCases(JSONObject.parseArray(testPlanReportContent.getFunctionAllCases(), TestPlanCaseDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getFunctionFailureCases())) {
            testPlanReportDTO.setFunctionFailureCases(JSONObject.parseArray(testPlanReportContent.getFunctionFailureCases(), TestPlanCaseDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getIssueList())) {
            testPlanReportDTO.setIssueList(JSONObject.parseArray(testPlanReportContent.getIssueList(), IssuesDao.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getApiAllCases())) {
            testPlanReportDTO.setApiAllCases(JSONObject.parseArray(testPlanReportContent.getApiAllCases(), TestPlanFailureApiDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getApiFailureCases())) {
            testPlanReportDTO.setApiFailureCases(JSONObject.parseArray(testPlanReportContent.getApiFailureCases(), TestPlanFailureApiDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getScenarioAllCases())) {
            testPlanReportDTO.setScenarioAllCases(JSONObject.parseArray(testPlanReportContent.getScenarioAllCases(), TestPlanFailureScenarioDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getScenarioFailureCases())) {
            testPlanReportDTO.setScenarioFailureCases(JSONObject.parseArray(testPlanReportContent.getScenarioFailureCases(), TestPlanFailureScenarioDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getLoadAllCases())) {
            testPlanReportDTO.setLoadAllCases(JSONObject.parseArray(testPlanReportContent.getLoadAllCases(), TestPlanLoadCaseDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getLoadFailureCases())) {
            testPlanReportDTO.setLoadFailureCases(JSONObject.parseArray(testPlanReportContent.getLoadFailureCases(), TestPlanLoadCaseDTO.class));
        }
        testPlanReportDTO.setId(reportId);
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportContent.getTestPlanReportId());
        testPlanReportDTO.setName(testPlanReport.getName());
        return testPlanReportDTO;
    }

    private void updateReportExecResult(TestPlanReportContentWithBLOBs testPlanReportContent) {
        boolean isUpdate = false;
        boolean isTaskRunning = false;
        boolean reportHasData = false;
        if (StringUtils.isNotBlank(testPlanReportContent.getApiAllCases())) {
            reportHasData = true;
            List<TestPlanFailureApiDTO> allCases = JSONObject.parseArray(testPlanReportContent.getApiAllCases(), TestPlanFailureApiDTO.class);
            for (TestPlanFailureApiDTO dto : allCases) {
                String status = dto.getExecResult();
                if (StringUtils.equalsAnyIgnoreCase(status, "Running", "Waiting")) {
                    isUpdate = true;
                    ApiDefinitionExecResult definitionExecResult = apiDefinitionExecResultMapper.selectByPrimaryKey(dto.getReportId());
                    if (definitionExecResult != null) {
                        dto.setExecResult(definitionExecResult.getStatus());
                    }
                }

                if (StringUtils.equalsAnyIgnoreCase(dto.getExecResult(), "Running", "Waiting")) {
                    isTaskRunning = true;
                }
            }
            testPlanReportContent.setApiAllCases(JSONArray.toJSONString(allCases));
        }

        if (StringUtils.isNotBlank(testPlanReportContent.getScenarioAllCases())) {
            reportHasData = true;
            List<TestPlanFailureScenarioDTO> allCases = JSONObject.parseArray(testPlanReportContent.getScenarioAllCases(), TestPlanFailureScenarioDTO.class);
            for (TestPlanFailureScenarioDTO dto : allCases) {
                String lastResult = dto.getLastResult();
                if (StringUtils.equalsAnyIgnoreCase(lastResult, "Running", "Waiting", "Underway")) {
                    isUpdate = true;
                    ApiScenarioReport apiReport = apiScenarioReportMapper.selectByPrimaryKey(dto.getReportId());
                    if (apiReport != null) {
                        dto.setLastResult(apiReport.getStatus());
                        dto.setStatus(apiReport.getStatus());
                    }
                } else if (StringUtils.equalsAnyIgnoreCase("Error", lastResult)) {
                    isUpdate = true;
                    dto.setLastResult("Fail");
                    dto.setStatus("Fail");
                }

                if (StringUtils.equalsAnyIgnoreCase(dto.getLastResult(), "Running", "Waiting", "Underway")) {
                    isTaskRunning = true;
                }
            }
            testPlanReportContent.setScenarioAllCases(JSONArray.toJSONString(allCases));
        }

        if (StringUtils.isNotBlank(testPlanReportContent.getLoadAllCases())) {
            List<TestPlanLoadCaseDTO> allCases = JSONObject.parseArray(testPlanReportContent.getLoadAllCases(), TestPlanLoadCaseDTO.class);
            if(!allCases.isEmpty()){
                isTaskRunning = true;
            }
        }
        if (isUpdate) {
            testPlanReportContentMapper.updateByPrimaryKeyWithBLOBs(testPlanReportContent);
        }

        if (!isTaskRunning && reportHasData) {
            this.finishTestPlanReport(testPlanReportContent.getTestPlanReportId());
        }
    }

    public void finishReport(TestPlanReport testPlanReport) {
        long endTime = System.currentTimeMillis();
        testPlanReport.setEndTime(endTime);
        testPlanReport.setUpdateTime(endTime);

        testPlanReport.setStatus(TestPlanReportStatus.FAILED.name());
        testPlanReportMapper.updateByPrimaryKeySelective(testPlanReport);

        TestPlanReportContentWithBLOBs bloBs = new TestPlanReportContentWithBLOBs();
        bloBs.setEndTime(endTime);
        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(testPlanReport.getId());
        testPlanReportContentMapper.updateByExampleSelective(bloBs,example);
    }

    private TestPlanReportExecuteCheckResultDTO checkTestPlanReportIsTimeOut(String planReportId) {
        //同步数据库更新状态信息
        try {
            this.syncReportStatus(planReportId);
        } catch (Exception e) {
            LogUtil.info("联动数据库同步执行状态失败! " + e.getMessage());
            LogUtil.error(e);
        }
        TestPlanExecuteInfo executeInfo = TestPlanReportExecuteCatch.getTestPlanExecuteInfo(planReportId);
        TestPlanReportExecuteCheckResultDTO checkResult = executeInfo.countUnFinishedNum();
        return checkResult;
    }

    private void syncReportStatus(String planReportId) {
        if (TestPlanReportExecuteCatch.containsReport(planReportId)) {
            TestPlanExecuteInfo executeInfo = TestPlanReportExecuteCatch.getTestPlanExecuteInfo(planReportId);
            if (executeInfo != null) {
                //同步接口案例结果
                Map<String, String> updateCaseStatusMap = new HashMap<>();
                Map<String, String> apiCaseReportMap = executeInfo.getRunningApiCaseReportMap();
                if (MapUtils.isNotEmpty(apiCaseReportMap)) {
                    List<ApiDefinitionExecResult> execList = extApiDefinitionExecResultMapper.selectStatusByIdList(apiCaseReportMap.keySet());
                    for (ApiDefinitionExecResult report : execList) {
                        String reportId = report.getId();
                        String status = report.getStatus();
                        if (!StringUtils.equalsAnyIgnoreCase(status, "Running", "Waiting")) {
                            String planCaseId = apiCaseReportMap.get(reportId);
                            if (StringUtils.isNotEmpty(planCaseId)) {
                                updateCaseStatusMap.put(planCaseId, status);
                            }
                        }
                    }
                }
                //同步场景结果
                Map<String, String> updateScenarioStatusMap = new HashMap<>();
                Map<String, String> scenarioReportMap = executeInfo.getRunningScenarioReportMap();
                if (MapUtils.isNotEmpty(scenarioReportMap)) {
                    List<ApiScenarioReport> reportList = extApiScenarioReportMapper.selectStatusByIds(scenarioReportMap.keySet());
                    for (ApiScenarioReport report : reportList) {
                        String reportId = report.getId();
                        String status = report.getStatus();
                        if (!StringUtils.equalsAnyIgnoreCase(status, "Running", "Waiting")) {
                            String planScenarioId = scenarioReportMap.get(reportId);
                            if (StringUtils.isNotEmpty(planScenarioId)) {
                                updateScenarioStatusMap.put(planScenarioId, status);
                            }
                        }
                    }
                }
                testPlanLog.info("ReportID:"+planReportId+" 本次数据库同步,案例ID："+JSON.toJSONString(apiCaseReportMap.keySet())+";场景ID："+JSON.toJSONString(scenarioReportMap.keySet())+"; 同步结果,案例:"+JSON.toJSONString(updateCaseStatusMap)+";场景："+JSON.toJSONString(updateScenarioStatusMap));
                TestPlanReportExecuteCatch.updateApiTestPlanExecuteInfo(planReportId, updateCaseStatusMap, updateScenarioStatusMap, null);
            }else {
                testPlanLog.info("同步数据库查询执行信息失败! 报告ID在缓存中未找到！"+planReportId);
            }
        }
    }

    private void finishTestPlanReport(String planReportId) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(planReportId);
        if (testPlanReport != null && StringUtils.equalsIgnoreCase("Running", testPlanReport.getStatus())) {
            this.finishReport(testPlanReport);
            testPlanLog.info("结束测试计划报告：[" + planReportId + "]");
        }
        TestPlanReportExecuteCatch.remove(planReportId);
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
                delete(ids);
            }
        }
    }
}
