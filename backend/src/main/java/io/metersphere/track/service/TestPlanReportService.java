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
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.track.Factory.ReportComponentFactory;
import io.metersphere.track.domain.ReportComponent;
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
    ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    LoadTestReportMapper loadTestReportMapper;
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

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    public List<TestPlanReportDTO> list(QueryTestPlanReportRequest request) {
        List<TestPlanReportDTO> list = new ArrayList<>();
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        if (StringUtils.isBlank(request.getProjectId())) {
            return list;
        }
        list = extTestPlanReportMapper.list(request);
        return list;
    }

//    private void checkReport(List<TestPlanReportDTO> list) {
//        if(CollectionUtils.isNotEmpty(list)){
//            for (TestPlanReportDTO dto : list){
//                if(StringUtils.equalsIgnoreCase(dto.getStatus(),TestPlanApiExecuteStatus.RUNNING.name())){
//                    TestPlanReport model = this.updateTestPlanReportById(dto.getId());
//                    if(model != null && model.getStatus() != null){
//                        dto.setStatus(model.getStatus());
//                    }
//                }
//            }
//        }
//    }

    public TestPlanScheduleReportInfoDTO genTestPlanReportBySchedule(String projectID, String planId, String userId, String triggerMode) {
        Map<String, String> planScenarioIdMap = new LinkedHashMap<>();
        Map<String, String> apiTestCaseIdMap = new LinkedHashMap<>();
        Map<ApiTestCaseWithBLOBs, String> apiTestCaseDataMap = new LinkedHashMap<>();
        Map<String, String> performanceIdMap = new LinkedHashMap<>();

        List<TestPlanApiScenario> testPlanApiScenarioList = extTestPlanScenarioCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiScenario model : testPlanApiScenarioList) {
            planScenarioIdMap.put(model.getApiScenarioId(), model.getId());
        }
        List<TestPlanApiCase> testPlanApiCaseList = extTestPlanApiCaseMapper.selectLegalDataByTestPlanId(planId);
        for (TestPlanApiCase model :
                testPlanApiCaseList) {
            apiTestCaseIdMap.put(model.getApiCaseId(), model.getId());
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
        if (!apiTestCaseIdMap.isEmpty()) {
            ApiTestCaseExample apiTestCaseExample = new ApiTestCaseExample();
            apiTestCaseExample.createCriteria().andIdIn(new ArrayList<>(apiTestCaseIdMap.keySet()));
            List<ApiTestCaseWithBLOBs> apiCaseList = apiTestCaseMapper.selectByExampleWithBLOBs(apiTestCaseExample);
            Map<String, ApiTestCaseWithBLOBs> apiCaseDataMap = new HashMap<>();
            if (!apiCaseList.isEmpty()) {
                apiCaseDataMap = apiCaseList.stream().collect(Collectors.toMap(ApiTestCaseWithBLOBs::getId, k -> k));
                for (String id : apiCaseDataMap.keySet()) {
                    apiCaseInfoMap.put(id, TestPlanApiExecuteStatus.PREPARE.name());
                    String testPlanApiCaseId = apiTestCaseIdMap.get(id);
                    if (StringUtils.isNotEmpty(testPlanApiCaseId)) {
                        apiTestCaseDataMap.put(apiCaseDataMap.get(id), testPlanApiCaseId);
                    }
                }
            }
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
                apiTestCaseIdMap.size() > 0, planScenarioIdMap.size() > 0, performanceIdMap.size() > 0,
                apiCaseInfoMap, scenarioInfoMap, performanceInfoMap);
        TestPlanReport report = this.genTestPlanReport(saveRequest);

        TestPlanScheduleReportInfoDTO returnDTO = new TestPlanScheduleReportInfoDTO();
        returnDTO.setTestPlanReport(report);
        returnDTO.setPlanScenarioIdMap(planScenarioIdMap);
        returnDTO.setApiTestCaseDataMap(apiTestCaseDataMap);
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

//        testPlanReport.setPrincipal(testPlan.getPrincipal());
        if (testPlanReport.getIsScenarioExecuting() || testPlanReport.getIsApiCaseExecuting() || testPlanReport.getIsPerformanceExecuting()) {
            testPlanReport.setStatus(APITestStatus.Running.name());
        } else {
            testPlanReport.setStatus(APITestStatus.Completed.name());
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

    public synchronized void updateReport(List<String> testPlanReportIdList, String runMode, String triggerMode, List<String> scenarioIdList) {
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

    public TestPlanReport updateReport(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs reportContent, boolean updateTime, TestPlanExecuteInfo executeInfo) {

        if (testPlanReport == null || executeInfo == null) {
            return null;
        }

        if (updateTime) {
            testPlanReport.setEndTime(System.currentTimeMillis());
            testPlanReport.setUpdateTime(System.currentTimeMillis());
        }

        boolean apiCaseIsOk = executeInfo.isApiCaseAllExecuted();
        boolean scenarioIsOk = executeInfo.isScenarioAllExecuted();
        boolean performanceIsOk = executeInfo.isLoadCaseAllExecuted();

        testPlanLog.info("ReportId[" + testPlanReport.getId() + "] count over. Testplan Execute Result:  Api is over ->" + apiCaseIsOk + "; scenario is over ->" + scenarioIsOk + "; performance is over ->" + performanceIsOk);


        if (apiCaseIsOk) {
            testPlanReport.setIsApiCaseExecuting(false);
        }
        if (scenarioIsOk) {
            testPlanReport.setIsScenarioExecuting(false);
        }
        if (performanceIsOk) {
            testPlanReport.setIsPerformanceExecuting(false);
        }

        if (apiCaseIsOk && scenarioIsOk && performanceIsOk) {
            TestPlanReportExecuteCatch.remove(testPlanReport.getId());
        }

        int[] componentIndexArr = new int[]{1, 3, 4};
        testPlanReport.setComponents(JSONArray.toJSONString(componentIndexArr));


        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(testPlanReport.getTestPlanId());
        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);

        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        JSONArray componentIds = JSONArray.parseArray(testPlanReport.getComponents());
        List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);
        testPlanService.buildApiCaseReport(testPlanReport.getTestPlanId(), components);
        testPlanService.buildScenarioCaseReport(testPlanReport.getTestPlanId(), components);
        testPlanService.buildLoadCaseReport(testPlanReport.getTestPlanId(), components);

        Map<String, Map<String, String>> testPlanExecuteResult = executeInfo.getExecutedResult();
        testPlanLog.info("ReportId[" + testPlanReport.getId() + "] COUNT OVER. COUNT RESULT :" + JSONObject.toJSONString(testPlanExecuteResult));

        TestPlanSimpleReportDTO reportDTO = testPlanService.buildPlanReport(executeInfo, testPlanReport.getTestPlanId(), false);
        //更新执行时间
        reportDTO.setStartTime(testPlanReport.getStartTime());
        reportDTO.setEndTime(System.currentTimeMillis());
        testPlanReportContentMapper.updateByPrimaryKeySelective(parseReportDaoToReportContent(reportDTO, reportContent));

        String testPlanStatus = this.getTestPlanReportStatus(testPlanReport, reportDTO);
        testPlanReport.setStatus(testPlanStatus);

        testPlanReport = this.update(testPlanReport);
        return testPlanReport;
    }

    public void checkTestPlanStatus(String planReportId) {
        try {
            TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(planReportId);

            TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
            testPlanService.checkStatus(testPlanReport.getTestPlanId());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
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
        if (!report.getIsApiCaseExecuting() && !report.getIsPerformanceExecuting() && !report.getIsScenarioExecuting()) {
            try {
                //更新TestPlan状态为完成
                TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(report.getTestPlanId());
                if (testPlan != null) {
//                    testPlan.setStatus(TestPlanStatus.Completed.name());
                    testPlanMapper.updateByPrimaryKeySelective(testPlan);
                }
                if (testPlan != null && StringUtils.equalsAny(report.getTriggerMode(), ReportTriggerMode.API.name(), ReportTriggerMode.SCHEDULE.name())) {
                    //发送通知
                    sendMessage(report, testPlan.getProjectId());
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        } else {
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
        paramMap.putAll(new BeanMap(testPlanReport));

        String successfulMailTemplate = "";
        String errfoMailTemplate = "";

        if (StringUtils.equalsAny(testPlanReport.getTriggerMode(), ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())) {
            successfulMailTemplate = "TestPlanSuccessfulNotification";
            errfoMailTemplate = "TestPlanFailedNotification";
        }

        String testPlanShareUrl = shareInfoService.getTestPlanShareUrl(testPlanReport.getId());
        paramMap.put("planShareUrl", baseSystemConfigDTO.getUrl() + "/sharePlanReport" + testPlanShareUrl);

        NoticeModel noticeModel = NoticeModel.builder()
                .operator(creator)
                .successContext(successContext)
                .successMailTemplate(successfulMailTemplate)
                .failedContext(failedContext)
                .failedMailTemplate(errfoMailTemplate)
                .testId(testPlan.getId())
                .status(testPlanReport.getStatus())
                .event(event)
                .subject(subject)
                .paramMap(paramMap)
                .build();
        noticeSendService.send(testPlanReport.getTriggerMode(), NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
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
                                        finishLoadTestId.put(performaneReportIDMap.get(loadTestReportId), TestPlanApiExecuteStatus.FAILD.name());
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
                                finishLoadTestId.put(loadTestReportFromDatabase.getTestId(), TestPlanApiExecuteStatus.SUCCESS.name());
                                caseReportMap.put(loadTestReportFromDatabase.getTestId(), loadTestReportId);
                                performaneReportIDList.remove(loadTestReportId);
                            }
                        }
                    } catch (Exception e) {
                        performaneReportIDList.remove(loadTestReportId);
                        finishLoadTestId.put(performaneReportIDMap.get(loadTestReportId), TestPlanApiExecuteStatus.FAILD.name());
                        caseReportMap.put(performaneReportIDMap.get(loadTestReportId), loadTestReportId);
                        testPlanLog.error(e.getMessage());
                    }
                }
                testPlanLog.info("TestPlanReportId[" + testPlanReport.getId() + "] SELECT performance BATCH OVER:" + JSONArray.toJSONString(selectList));
                if (performaneReportIDList.isEmpty()) {
                    testPlanLog.info("TestPlanReportId[" + testPlanReport.getId() + "] performance EXECUTE OVER. TRIGGER_MODE:" + triggerMode + ",REsult:" + JSONObject.toJSONString(finishLoadTestId));
                    if (StringUtils.equals(triggerMode, ReportTriggerMode.API.name())) {
                        for (String string : finishLoadTestId.keySet()) {
                            TestPlanLoadCaseEventDTO eventDTO = new TestPlanLoadCaseEventDTO();
                            eventDTO.setReportId(string);
                            eventDTO.setTriggerMode(triggerMode);
                            eventDTO.setStatus(PerformanceTestStatus.Completed.name());
                            this.updatePerformanceTestStatus(eventDTO);
                        }
                    }
                    TestPlanReportExecuteCatch.updateApiTestPlanExecuteInfo(testPlanReport.getId(), null, null, finishLoadTestId);
                    TestPlanReportExecuteCatch.updateTestPlanExecuteResultInfo(testPlanReport.getId(), null, null, caseReportMap);
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
//            TestPlanReportResourceExample resourceExample = new TestPlanReportResourceExample();
//            resourceExample.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId);
//            testPlanReportResourceService.deleteByExample(resourceExample);
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

//            TestPlanReportResourceExample resourceExample = new TestPlanReportResourceExample();
//            resourceExample.createCriteria().andTestPlanReportIdIn(deleteReportIds);
//            testPlanReportResourceService.deleteByExample(resourceExample);
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

        boolean updateTime = MapUtils.isNotEmpty(executeApiCaseIdMap) || MapUtils.isNotEmpty(executeScenarioCaseIdMap) || MapUtils.isNotEmpty(executePerformanceIdMap);

        testPlanLog.info("ReportId[" + planReportId + "] Executed. api :" + JSONObject.toJSONString(executeApiCaseIdMap) + "; scenario:" + JSONObject.toJSONString(executeScenarioCaseIdMap) + "; performance:" + JSONObject.toJSONString(executePerformanceIdMap));

        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId);
        List<TestPlanReportContentWithBLOBs> reportDataList = testPlanReportContentMapper.selectByExampleWithBLOBs(example);

        TestPlanReport report = null;
        if (!reportDataList.isEmpty()) {
            TestPlanReportExecuteCatch.setReportDataCheckResult(planReportId, true);
            TestPlanReportContentWithBLOBs reportData = reportDataList.get(0);
            report = testPlanReportMapper.selectByPrimaryKey(planReportId);
            report = this.updateReport(report, reportData, updateTime, executeInfo);
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
        TestPlanExecuteInfo executeInfo = TestPlanReportExecuteCatch.getTestPlanExecuteInfo(planReportId);
        int unFinishNum = executeInfo.countUnFinishedNum();
        if (unFinishNum > 0) {
            //如果间隔超过5分钟没有案例执行完成，则把执行结果变成false
            long lastCountTime = executeInfo.getLastFinishedNumCountTime();
            long nowTime = System.currentTimeMillis();
            if (nowTime - lastCountTime > 300000) {
                TestPlanReportExecuteCatch.finishAllTask(planReportId);
            }
        }
        this.updateExecuteApis(planReportId);
    }

    public TestPlanSimpleReportDTO getReport(String reportId) {
        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(reportId);
        List<TestPlanReportContentWithBLOBs> testPlanReportContents = testPlanReportContentMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(testPlanReportContents)) {
            return null;
        }
        TestPlanReportContentWithBLOBs testPlanReportContent = testPlanReportContents.get(0);
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
}
