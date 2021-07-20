package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanReportMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.Factory.ReportComponentFactory;
import io.metersphere.track.domain.ReportComponent;
import io.metersphere.track.domain.ReportResultComponent;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.report.QueryTestPlanReportRequest;
import io.metersphere.track.request.report.TestPlanReportSaveRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

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
public class TestPlanReportService {
    @Resource
    TestPlanReportMapper testPlanReportMapper;
    @Resource
    TestPlanReportDataMapper testPlanReportDataMapper;
    @Resource
    ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    TestPlanApiScenarioMapper testPlanScenarioCaseMapper;
    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    ExtTestPlanReportMapper extTestPlanReportMapper;
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    ExtTestPlanMapper extTestPlanMapper;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    //    @Resource
//    TestPlanLoadCaseService testPlanLoadCaseService;
//    @Resource
//    TestPlanService testPlanService;
    @Resource
    LoadTestReportMapper loadTestReportMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    public List<TestPlanReportDTO> list(QueryTestPlanReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        String projectId = SessionUtils.getCurrentProjectId();
        if (StringUtils.isNotBlank(projectId)) {
            request.setProjectId(projectId);
        }
        return extTestPlanReportMapper.list(request);
    }

    public TestPlanScheduleReportInfoDTO genTestPlanReportBySchedule(String projectID, String planId, String userId, String triggerMode) {
        Map<String, String> planScenarioIdMap = new LinkedHashMap<>();
        Map<String, String> apiTestCaseIdMap = new LinkedHashMap<>();
        Map<String, String> performanceIdMap = new LinkedHashMap<>();

        TestPlanApiScenarioExample scenarioCaseExample = new TestPlanApiScenarioExample();
        scenarioCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiScenario> testPlanApiScenarioList = testPlanScenarioCaseMapper.selectByExample(scenarioCaseExample);
        for (TestPlanApiScenario model : testPlanApiScenarioList) {
            planScenarioIdMap.put(model.getApiScenarioId(), model.getId());
        }
        TestPlanApiCaseExample apiCaseExample = new TestPlanApiCaseExample();
        apiCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCaseList = testPlanApiCaseMapper.selectByExample(apiCaseExample);
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

        List<Map<String, String>> apiCaseInfoList = new ArrayList<>();
        for (String id : apiTestCaseIdMap.keySet()) {
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("status", TestPlanApiExecuteStatus.PREPARE.name());
            apiCaseInfoList.add(map);
        }
        List<Map<String, String>> scenarioInfoList = new ArrayList<>();
        for (String id : planScenarioIdMap.keySet()) {
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("status", TestPlanApiExecuteStatus.PREPARE.name());
            scenarioInfoList.add(map);
        }
        List<Map<String, String>> performanceInfoList = new ArrayList<>();
        for (String id : performanceIdMap.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("status", TestPlanApiExecuteStatus.PREPARE.name());
            performanceInfoList.add(map);
        }
        TestPlanReportSaveRequest saveRequest = new TestPlanReportSaveRequest(planReportId, planId, userId, triggerMode,
                apiTestCaseIdMap.size() > 0, planScenarioIdMap.size() > 0, performanceIdMap.size() > 0,
                JSONArray.toJSONString(apiCaseInfoList), JSONArray.toJSONString(scenarioInfoList), JSONArray.toJSONString(performanceInfoList));
        TestPlanReport report = this.genTestPlanReport(saveRequest);

        TestPlanScheduleReportInfoDTO returnDTO = new TestPlanScheduleReportInfoDTO();
        returnDTO.setTestPlanReport(report);
        returnDTO.setPlanScenarioIdMap(planScenarioIdMap);
        returnDTO.setApiTestCaseIdMap(apiTestCaseIdMap);
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
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(saveRequest.getPlanId());
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

        TestPlanReportDataWithBLOBs testPlanReportData = new TestPlanReportDataWithBLOBs();
        testPlanReportData.setId(UUID.randomUUID().toString());
        testPlanReportData.setTestPlanReportId(testPlanReportID);

        if (saveRequest.isCountResources()) {
            TestPlanApiCaseExample apiExample = new TestPlanApiCaseExample();
            apiExample.createCriteria().andTestPlanIdEqualTo(saveRequest.getPlanId());
            List<String> apiCaseIdList = testPlanApiCaseMapper.selectByExample(apiExample)
                    .stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
            testPlanReport.setIsApiCaseExecuting(!apiCaseIdList.isEmpty());

            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andTestPlanIdEqualTo(saveRequest.getPlanId());
            List<String> scenarioIdList = testPlanScenarioCaseMapper.selectByExample(example)
                    .stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList());
            testPlanReport.setIsScenarioExecuting(!scenarioIdList.isEmpty());

            LoadCaseRequest loadCaseRequest = new LoadCaseRequest();
            loadCaseRequest.setTestPlanId(saveRequest.getPlanId());
            loadCaseRequest.setProjectId(testPlan.getProjectId());
            List<String> performanceIdList = extTestPlanLoadCaseMapper.selectTestPlanLoadCaseList(loadCaseRequest)
                    .stream().map(TestPlanLoadCaseDTO::getLoadCaseId).collect(Collectors.toList());
            testPlanReport.setIsPerformanceExecuting(!performanceIdList.isEmpty());

            List<Map<String, String>> apiCaseInfoList = new ArrayList<>();
            for (String id : apiCaseIdList) {
                Map<String, String> map = new HashMap<>();
                map.put("id", id);
                map.put("status", TestPlanApiExecuteStatus.PREPARE.name());
                apiCaseInfoList.add(map);
            }
            List<Map<String, String>> scenarioInfoList = new ArrayList<>();
            for (String id : scenarioIdList) {
                Map<String, String> map = new HashMap<>();
                map.put("id", id);
                map.put("status", TestPlanApiExecuteStatus.PREPARE.name());
                scenarioInfoList.add(map);
            }
            List<Map<String, String>> performanceInfoList = new ArrayList<>();
            for (String id : performanceIdList) {
                Map<String, String> map = new HashMap<>();
                map.put("id", id);
                map.put("status", TestPlanApiExecuteStatus.PREPARE.name());
                performanceInfoList.add(map);
            }
            testPlanReportData.setApiCaseInfo(JSONArray.toJSONString(apiCaseInfoList));
            testPlanReportData.setScenarioInfo(JSONArray.toJSONString(scenarioInfoList));
            testPlanReportData.setPerformanceInfo(JSONArray.toJSONString(performanceInfoList));
        } else {
            testPlanReport.setIsApiCaseExecuting(saveRequest.isApiCaseIsExecuting());
            testPlanReport.setIsScenarioExecuting(saveRequest.isScenarioIsExecuting());
            testPlanReport.setIsPerformanceExecuting(saveRequest.isPerformanceIsExecuting());

            testPlanReportData.setApiCaseInfo(saveRequest.getApiCaseIdListJSON());
            testPlanReportData.setScenarioInfo(saveRequest.getScenarioIdListJSON());
            testPlanReportData.setPerformanceInfo(saveRequest.getPerformanceIdListJSON());
        }

        testPlanReport.setPrincipal(testPlan.getPrincipal());
        if (testPlanReport.getIsScenarioExecuting() || testPlanReport.getIsApiCaseExecuting() || testPlanReport.getIsPerformanceExecuting()) {
            testPlanReport.setStatus(APITestStatus.Starting.name());
        } else {
            testPlanReport.setStatus(APITestStatus.Completed.name());
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        TestPlanReportMapper insertReportMapper = sqlSession.getMapper(TestPlanReportMapper.class);
        TestPlanReportDataMapper insertReportDataMapper = sqlSession.getMapper(TestPlanReportDataMapper.class);
        insertReportMapper.insert(testPlanReport);
        insertReportDataMapper.insert(testPlanReportData);
        sqlSession.commit();
        sqlSession.flushStatements();

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
                returnDTO.setPrincipal(report.getPrincipal());
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

    public TestCaseReportMetricDTO countReportData(TestPlanDTO testPlan, TestPlanReportDataWithBLOBs testPlanReportData) {
        TestCaseReportMetricDTO returnDTO = new TestCaseReportMetricDTO();
        ReportResultComponent reportResultComponent = new ReportResultComponent(testPlan);
        reportResultComponent.afterBuild(returnDTO);

        TestCaseReportAdvanceStatusResultDTO statusDTO = new TestCaseReportAdvanceStatusResultDTO();
        String apiCaseExecuteMapJson = testPlanReportData.getApiCaseInfo();
        String scenarioExecuteMapJson = testPlanReportData.getScenarioInfo();
        String loadExecuteMapJson = testPlanReportData.getPerformanceInfo();

        List<TestCaseReportStatusResultDTO> apiResult = new ArrayList<>();
        List<TestCaseReportStatusResultDTO> scenarioResult = new ArrayList<>();
        List<TestCaseReportStatusResultDTO> loadResult = new ArrayList<>();

        List<String> faliureApiCaseIdList = new ArrayList<>();
        List<String> faliureScenarioCaseIdList = new ArrayList<>();
        List<String> faliureLoadCaseIdList = new ArrayList<>();

        JSONArray apiCaseExecuteArr = new JSONArray();
        JSONArray scenarioExecuteArr = new JSONArray();
        JSONArray loadExecuteArr = new JSONArray();
        if (StringUtils.isNotEmpty(apiCaseExecuteMapJson)) {
            try {
                apiCaseExecuteArr = JSONArray.parseArray(apiCaseExecuteMapJson);
            } catch (Exception e) {
            }
        }

        if (StringUtils.isNotEmpty(scenarioExecuteMapJson)) {
            try {
                scenarioExecuteArr = JSONArray.parseArray(scenarioExecuteMapJson);
            } catch (Exception e) {
            }
        }

        if (StringUtils.isNotEmpty(loadExecuteMapJson)) {
            try {
                loadExecuteArr = JSONArray.parseArray(loadExecuteMapJson);
            } catch (Exception e) {
            }
        }

        for (int i = 0; i < apiCaseExecuteArr.size(); i++) {
            JSONObject jsonObj = apiCaseExecuteArr.getJSONObject(i);

            Map<String, Integer> countMap = new HashMap<>();
            if (jsonObj.containsKey("status")) {
                String status = jsonObj.getString("status");
                if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.SUCCESS.name())) {
                    if (countMap.containsKey("Pass")) {
                        countMap.put("Pass", countMap.get("Pass") + 1);
                    } else {
                        countMap.put("Pass", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.FAILD.name())) {
                    if (jsonObj.containsKey("id")) {
                        faliureApiCaseIdList.add(jsonObj.getString("id"));
                    }
                    if (countMap.containsKey("Failure")) {
                        countMap.put("Failure", countMap.get("Failure") + 1);
                    } else {
                        countMap.put("Failure", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.PREPARE.name())) {
                    if (jsonObj.containsKey("id")) {
                        faliureApiCaseIdList.add(jsonObj.getString("id"));
                    }
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

        for (int i = 0; i < scenarioExecuteArr.size(); i++) {
            JSONObject jsonObj = scenarioExecuteArr.getJSONObject(i);
            Map<String, Integer> countMap = new HashMap<>();
            if (jsonObj.containsKey("status")) {
                String status = jsonObj.getString("status");
                if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.SUCCESS.name())) {
                    if (countMap.containsKey("Pass")) {
                        countMap.put("Pass", countMap.get("Pass") + 1);
                    } else {
                        countMap.put("Pass", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.FAILD.name())) {
                    if (jsonObj.containsKey("id")) {
                        faliureScenarioCaseIdList.add(jsonObj.getString("id"));
                    }
                    if (countMap.containsKey("Failure")) {
                        countMap.put("Failure", countMap.get("Failure") + 1);
                    } else {
                        countMap.put("Failure", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.PREPARE.name())) {
                    if (jsonObj.containsKey("id")) {
                        faliureScenarioCaseIdList.add(jsonObj.getString("id"));
                    }
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

        for (int i = 0; i < loadExecuteArr.size(); i++) {
            JSONObject jsonObj = loadExecuteArr.getJSONObject(i);
            Map<String, Integer> countMap = new HashMap<>();
            if (jsonObj.containsKey("status")) {
                String status = jsonObj.getString("status");
                if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.SUCCESS.name())) {
                    if (countMap.containsKey("Pass")) {
                        countMap.put("Pass", countMap.get("Pass") + 1);
                    } else {
                        countMap.put("Pass", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.FAILD.name())) {
                    if (jsonObj.containsKey("id")) {
                        faliureLoadCaseIdList.add(jsonObj.getString("id"));
                    }
                    if (countMap.containsKey("Failure")) {
                        countMap.put("Failure", countMap.get("Failure") + 1);
                    } else {
                        countMap.put("Failure", 1);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.PREPARE.name())) {
                    if (jsonObj.containsKey("id")) {
                        faliureLoadCaseIdList.add(jsonObj.getString("id"));
                    }
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

    public void updateReport(TestPlanReportDataWithBLOBs testPlanReportData, boolean apiCaseIsOk, boolean scenarioIsOk, boolean performanceIsOk) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportData.getTestPlanReportId());
        if (testPlanReport == null) {
            return;
        }

        testPlanReport.setEndTime(System.currentTimeMillis());
        testPlanReport.setUpdateTime(System.currentTimeMillis());
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


        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(testPlanReport.getTestPlanId());
        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);


        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        JSONArray componentIds = JSONArray.parseArray(testPlanReport.getComponents());
        List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);
        testPlanService.buildApiCaseReport(testPlanReport.getTestPlanId(), components);
        testPlanService.buildScenarioCaseReport(testPlanReport.getTestPlanId(), components);
        testPlanService.buildLoadCaseReport(testPlanReport.getTestPlanId(), components);


        TestCaseReportMetricDTO testCaseReportMetricDTO = this.countReportData(testPlan, testPlanReportData);

        //统计执行的场景ID
        testPlanReportData.setExecuteResult(JSONObject.toJSONString(testCaseReportMetricDTO.getExecuteResult()));
        testPlanReportData.setFailurTestCases(JSONObject.toJSONString(testCaseReportMetricDTO.getFailureTestCases()));
        testPlanReportData.setModuleExecuteResult(JSONArray.toJSONString(testCaseReportMetricDTO.getModuleExecuteResult()));
        testPlanReportDataMapper.updateByPrimaryKeyWithBLOBs(testPlanReportData);


        String testPlanStatus = this.getTestPlanReportStatus(testPlanReport, testPlanReportData);
        testPlanReport.setStatus(testPlanStatus);
        this.update(testPlanReport);
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
        String issuesInfo = null;

        //因为接口案例的定时任务是单个案例开线程运行， 所以要检查是否都执行完成。全部执行完成时才会进行统一整理
        if (StringUtils.equalsAny(triggerMode, ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())
                && StringUtils.equalsAny(resourceRunMode, ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
            List<String> statusList = extTestPlanApiCaseMapper.getStatusByTestPlanId(testPlan.getId());
            for (String status : statusList) {
                if (status == null) {
                    return;
                }
            }
        } else if (StringUtils.equals(ReportTriggerMode.TEST_PLAN_SCHEDULE.name(), triggerMode)) {

        }

        testPlanReport.setEndTime(System.currentTimeMillis());
        testPlanReport.setUpdateTime(System.currentTimeMillis());

        //手动触发的需要保存手工执行的信息
        int[] componentIndexArr = null;
        if (StringUtils.equals(ReportTriggerMode.MANUAL.name(), triggerMode)) {
            componentIndexArr = new int[]{1, 2, 3, 4, 5};
        } else {
            componentIndexArr = new int[]{1, 3, 4};
        }
        testPlanReport.setComponents(JSONArray.toJSONString(componentIndexArr));

        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        JSONArray componentIds = JSONArray.parseArray(testPlanReport.getComponents());
        List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);
        testPlanService.buildApiCaseReport(testPlanReport.getTestPlanId(), components);
        testPlanService.buildScenarioCaseReport(testPlanReport.getTestPlanId(), components);
        testPlanService.buildLoadCaseReport(testPlanReport.getTestPlanId(), components);

        if (StringUtils.equals(ReportTriggerMode.MANUAL.name(), triggerMode)) {
            List<IssuesDao> issues = testPlanService.buildFunctionalCaseReport(testPlanReport.getTestPlanId(), components);
            issuesInfo = JSONArray.toJSONString(issues);
        }


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
        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        components.forEach(component -> {
            component.afterBuild(testCaseReportMetricDTO);
        });

        if (StringUtils.equalsAny(triggerMode, ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())
                && StringUtils.equalsAny(resourceRunMode, ApiRunMode.SCHEDULE_PERFORMANCE_TEST.name(), ApiRunMode.JENKINS_PERFORMANCE_TEST.name())) {
            //如果是性能测试作为触发，由于延迟原因可能会出现报告已经结束但是状态还是进行中的状态
            List<TestCaseReportStatusResultDTO> loadResult = testCaseReportMetricDTO.getExecuteResult().getLoadResult();
            for (TestCaseReportStatusResultDTO dto : loadResult) {
                if (StringUtils.equals(dto.getStatus(), TestPlanTestCaseStatus.Underway.name())) {
                    dto.setStatus(TestPlanTestCaseStatus.Pass.name());
                }
            }
            testCaseReportMetricDTO.getExecuteResult().setLoadResult(loadResult);
        }

        TestPlanReportDataExample example = new TestPlanReportDataExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId);
        List<TestPlanReportDataWithBLOBs> testPlanReportDataList = testPlanReportDataMapper.selectByExampleWithBLOBs(example);

        TestPlanReportDataWithBLOBs testPlanReportData = null;
        if (!testPlanReportDataList.isEmpty()) {
            testPlanReportData = testPlanReportDataList.get(0);

            if (CollectionUtils.isNotEmpty(scenarioIdList)
                    && StringUtils.equalsAny(triggerMode, ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())
                    && StringUtils.equalsAny(resourceRunMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                try {
                    List<String> scenarioListArr = JSONArray.parseArray(testPlanReportData.getScenarioInfo(), String.class);
                    TestCaseReportAdvanceStatusResultDTO savedDTO = JSONObject.parseObject(testPlanReportData.getExecuteResult(), TestCaseReportAdvanceStatusResultDTO.class);
                    List<String> executeScenarioList = new ArrayList<>();
                    if (savedDTO != null) {
                        if (savedDTO.getExecutedScenarioIds() != null) {
                            executeScenarioList = savedDTO.getExecutedScenarioIds();
                        }
                    }
                    for (String scenarioId : scenarioIdList) {
                        if (!executeScenarioList.contains(scenarioId)) {
                            executeScenarioList.add(scenarioId);
                        }
                    }
                    if (testCaseReportMetricDTO.getExecuteResult() == null) {
                        TestCaseReportAdvanceStatusResultDTO executeResultDTO = new TestCaseReportAdvanceStatusResultDTO();
                        testCaseReportMetricDTO.setExecuteResult(executeResultDTO);
                    }
                    testCaseReportMetricDTO.getExecuteResult().setExecutedScenarioIds(executeScenarioList);

                    if (!CollectionUtils.isEqualCollection(scenarioListArr, executeScenarioList)) {
                        testPlanReport.setIsScenarioExecuting(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //统计执行的场景ID
            testPlanReportData.setExecuteResult(JSONObject.toJSONString(testCaseReportMetricDTO.getExecuteResult()));
            testPlanReportData.setFailurTestCases(JSONObject.toJSONString(testCaseReportMetricDTO.getFailureTestCases()));
            testPlanReportData.setModuleExecuteResult(JSONArray.toJSONString(testCaseReportMetricDTO.getModuleExecuteResult()));
            if (issuesInfo != null) {
                testPlanReportData.setIssuesInfo(issuesInfo);
            }
            testPlanReportDataMapper.updateByPrimaryKeyWithBLOBs(testPlanReportData);
        }

        String testPlanStatus = this.getTestPlanReportStatus(testPlanReport, testPlanReportData);
        testPlanReport.setStatus(testPlanStatus);
        this.update(testPlanReport);
    }

    /**
     * 计算测试计划的状态
     *
     * @param testPlanReport
     * @return
     */
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

    public void update(TestPlanReport report) {
        if (!report.getIsApiCaseExecuting() && !report.getIsPerformanceExecuting() && !report.getIsScenarioExecuting()) {
            try {
                //更新TestPlan状态为完成
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(report.getTestPlanId());
                if (testPlan != null) {
                    testPlan.setStatus(TestPlanStatus.Completed.name());
                    testPlanMapper.updateByPrimaryKeySelective(testPlan);
                }
                if (StringUtils.equalsAny(report.getTriggerMode(), ReportTriggerMode.API.name(), ReportTriggerMode.SCHEDULE.name())) {
                    //发送通知
                    sendMessage(report);
                }
            } catch (Exception e) {

            }
        } else {
        }

        testPlanReportMapper.updateByPrimaryKey(report);
    }

    public void sendMessage(TestPlanReport testPlanReport) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
        assert testPlan != null;
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/track/testPlan/reportList";
        String successContext = "";
        String failedContext = "";
        String subject = "";
        String event = "";
        if (StringUtils.equals(testPlanReport.getTriggerMode(), ReportTriggerMode.API.name())) {
            successContext = "测试计划jenkins任务通知:'" + testPlan.getName() + "'执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            failedContext = "测试计划jenkins任务通知:'" + testPlan.getName() + "'执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            subject = Translator.get("task_notification_jenkins");
        } else {
            successContext = "测试计划定时任务通知:'" + testPlan.getName() + "'执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            failedContext = "测试计划定时任务通知:'" + testPlan.getName() + "'执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            subject = Translator.get("task_notification");
        }


        if (StringUtils.equals(TestPlanReportStatus.FAILED.name(), testPlanReport.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        } else {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("testName", testPlan.getName());
        paramMap.put("id", testPlanReport.getId());
        paramMap.put("type", "testPlan");
        paramMap.put("url", url);
        paramMap.put("status", testPlanReport.getStatus());

        String successfulMailTemplate = "";
        String errfoMailTemplate = "";

        if (StringUtils.equalsAny(testPlanReport.getTriggerMode(), ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())) {
            successfulMailTemplate = "TestPlanSuccessfulNotification";
            errfoMailTemplate = "TestPlanFailedNotification";
        }

        NoticeModel noticeModel = NoticeModel.builder()
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
        noticeSendService.send(testPlanReport.getTriggerMode(), noticeModel);
    }

    public TestPlanReport getTestPlanReport(String planId) {
        return testPlanReportMapper.selectByPrimaryKey(planId);
    }

    /**
     * 更新TestPlanReportData的PerformanceInfo
     *
     * @param testPlanReport
     * @param performaneReportIDList
     */
    public void updatePerformanceInfo(TestPlanReport testPlanReport, List<String> performaneReportIDList, String triggerMode) {
//        TestPlanReportDataExample example = new TestPlanReportDataExample();
//        example.createCriteria().andTestPlanReportIdEqualTo(testPlanReport.getId());
//        List<TestPlanReportDataWithBLOBs> reportDataList = testPlanReportDataMapper.selectByExampleWithBLOBs(example);
//        for (TestPlanReportDataWithBLOBs models : reportDataList) {
//            models.setPerformanceInfo(JSONArray.toJSONString(performaneReportIDList));
//            testPlanReportDataMapper.updateByPrimaryKeyWithBLOBs(models);
//        }

        /**
         * 虽然kafka已经设置了topic推送，但是当执行机器性能不够时会影响到报告状态当修改
         * 同时如果执行过程中报告删除，那么此时也应当记为失败。
         */
        List<String> updatePerformaneReportIDList = new ArrayList<>(performaneReportIDList);
        Map<String, String> finishLoadTestId = new HashMap<>();
        executorService.submit(() -> {
            //错误数据检查集合。 如果错误数据出现超过20次，则取消该条数据的检查
            Map<String, Integer> errorDataCheckMap = new HashMap<>();
            while (performaneReportIDList.size() > 0) {
                List<String> selectList = new ArrayList<>(performaneReportIDList);
                for (String loadTestReportId : selectList) {
                    LoadTestReportWithBLOBs loadTestReportFromDatabase = loadTestReportMapper.selectByPrimaryKey(loadTestReportId);
                    if (loadTestReportFromDatabase == null) {
                        //检查错误数据
                        if (errorDataCheckMap.containsKey(loadTestReportId)) {
                            if (errorDataCheckMap.get(loadTestReportId) > 10) {
                                performaneReportIDList.remove(loadTestReportId);
                            } else {
                                errorDataCheckMap.put(loadTestReportId, errorDataCheckMap.get(loadTestReportId) + 1);
                            }
                        } else {
                            errorDataCheckMap.put(loadTestReportId, 1);
                        }
                    } else if (StringUtils.equalsAny(loadTestReportFromDatabase.getStatus(),
                            PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
                        finishLoadTestId.put(loadTestReportFromDatabase.getTestId(), TestPlanApiExecuteStatus.SUCCESS.name());
                        performaneReportIDList.remove(loadTestReportId);
                    }
                }
                if (performaneReportIDList.isEmpty()) {
                    if (StringUtils.equals(triggerMode, ReportTriggerMode.API.name())) {
                        for (String string : updatePerformaneReportIDList) {
                            TestPlanLoadCaseEventDTO eventDTO = new TestPlanLoadCaseEventDTO();
                            eventDTO.setReportId(string);
                            eventDTO.setTriggerMode(triggerMode);
                            eventDTO.setStatus(PerformanceTestStatus.Completed.name());
                            this.updatePerformanceTestStatus(eventDTO);
                        }
                    } else {
                        this.updateExecuteApis(testPlanReport.getId(), null, null, finishLoadTestId);
                    }

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
        }
    }

    public void delete(QueryTestPlanReportRequest request) {
        List<String> deleteReportIds = request.getDataIds();
        if (request.isSelectAllDate()) {
            deleteReportIds = this.getAllApiIdsByFontedSelect(request.getFilters(), request.getName(), request.getProjectId(), request.getUnSelectIds());
        }
        TestPlanReportExample deleteReportExample = new TestPlanReportExample();
        deleteReportExample.createCriteria().andIdIn(deleteReportIds);
        testPlanReportMapper.deleteByExample(deleteReportExample);

        TestPlanReportDataExample example = new TestPlanReportDataExample();
        example.createCriteria().andTestPlanReportIdIn(deleteReportIds);
        testPlanReportDataMapper.deleteByExample(example);
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

    public synchronized void updateExecuteApis(String planReportId, Map<String, String> executeApiCaseIdMap, Map<String, String> executeScenarioCaseIdMap, Map<String, String> executePerformanceIdMap) {
        TestPlanReportDataExample example = new TestPlanReportDataExample();
        if(executeApiCaseIdMap == null){
            executeApiCaseIdMap = new HashMap<>();
        }
        if(executeScenarioCaseIdMap == null){
            executeScenarioCaseIdMap = new HashMap<>();
        }
        if(executePerformanceIdMap == null){
            executePerformanceIdMap = new HashMap<>();
        }
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId);
        List<TestPlanReportDataWithBLOBs> reportDataList = testPlanReportDataMapper.selectByExampleWithBLOBs(example);
        if (!reportDataList.isEmpty()) {
            TestPlanReportDataWithBLOBs reportData = reportDataList.get(0);

            boolean apiCaseExecuteOk = true;

            try {
                JSONArray executeArr = JSONArray.parseArray(reportData.getApiCaseInfo());
                JSONArray newArr = new JSONArray();
                for (int i = 0; i < executeArr.size(); i++) {
                    JSONObject caseInfo = executeArr.getJSONObject(i);
                    if (caseInfo.containsKey("id") && caseInfo.containsKey("status")) {
                        if (MapUtils.isNotEmpty(executeApiCaseIdMap)) {
                            String id = caseInfo.getString("id");
                            String status = caseInfo.getString("status");
                            String updateStatus = executeApiCaseIdMap.get(id);
                            if (StringUtils.isNotEmpty(updateStatus)
                                    && !StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.SUCCESS.name(), TestPlanApiExecuteStatus.FAILD.name())) {
                                caseInfo.put("status", updateStatus);
                            }
                        }
                        if (StringUtils.equals(TestPlanApiExecuteStatus.RUNNING.name(), caseInfo.getString("status"))) {
                            apiCaseExecuteOk = false;
                        }
                        newArr.add(caseInfo);
                    }
                }
                reportData.setApiCaseInfo(newArr.toJSONString());
            } catch (Exception e) {
            }

            boolean scenarioExecuteOk = true;

            try {
                JSONArray executeArr = JSONArray.parseArray(reportData.getScenarioInfo());
                JSONArray newArr = new JSONArray();
                for (int i = 0; i < executeArr.size(); i++) {
                    JSONObject caseInfo = executeArr.getJSONObject(i);
                    if (caseInfo.containsKey("id") && caseInfo.containsKey("status")) {
                        if (MapUtils.isNotEmpty(executeScenarioCaseIdMap)) {
                            String id = caseInfo.getString("id");
                            String status = caseInfo.getString("status");
                            String updateStatus = executeScenarioCaseIdMap.get(id);
                            if (StringUtils.isNotEmpty(updateStatus)
                                    && !StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.SUCCESS.name(), TestPlanApiExecuteStatus.FAILD.name())) {
                                caseInfo.put("status", updateStatus);
                            }
                        }
                        if (StringUtils.equals(TestPlanApiExecuteStatus.RUNNING.name(), caseInfo.getString("status"))) {
                            scenarioExecuteOk = false;
                        }
                        newArr.add(caseInfo);
                    }
                }
                reportData.setScenarioInfo(newArr.toJSONString());
            } catch (Exception e) {
            }
            boolean performanceExecuteOk = true;

            try {
                JSONArray executeArr = JSONArray.parseArray(reportData.getPerformanceInfo());
                JSONArray newArr = new JSONArray();
                for (int i = 0; i < executeArr.size(); i++) {
                    JSONObject caseInfo = executeArr.getJSONObject(i);
                    if (caseInfo.containsKey("id") && caseInfo.containsKey("status")) {
                        if (MapUtils.isNotEmpty(executePerformanceIdMap)) {
                            String id = caseInfo.getString("id");
                            String status = caseInfo.getString("status");
                            String updateStatus = executePerformanceIdMap.get(id);
                            if (StringUtils.isNotEmpty(updateStatus)
                                    && !StringUtils.equalsAnyIgnoreCase(status, TestPlanApiExecuteStatus.SUCCESS.name(), TestPlanApiExecuteStatus.FAILD.name())) {
                                caseInfo.put("status", updateStatus);
                            }
                        }

                        if (StringUtils.equals(TestPlanApiExecuteStatus.RUNNING.name(), caseInfo.getString("status"))) {
                            performanceExecuteOk = false;
                        }
                        newArr.add(caseInfo);
                    }
                }
                reportData.setPerformanceInfo(newArr.toJSONString());
            } catch (Exception e) {
            }

            testPlanReportDataMapper.updateByPrimaryKeySelective(reportData);
            this.updateReport(reportData, apiCaseExecuteOk, scenarioExecuteOk, performanceExecuteOk);
        }
    }
}
