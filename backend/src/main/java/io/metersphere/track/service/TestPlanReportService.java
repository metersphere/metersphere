package io.metersphere.track.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.Factory.ReportComponentFactory;
import io.metersphere.track.domain.ReportComponent;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.report.QueryTestPlanReportRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song.tianyang
 * @Date 2021/1/8 4:34 下午
 * @Description
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanReportService {
    @Resource
    TestPlanReportMapper testPlanReportMapper;
    @Resource
    TestPlanReportDataMapper testPlanReportDataMapper;
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
    @Resource
    TestPlanLoadCaseService testPlanLoadCaseService;

    @Resource
    TestPlanService testPlanService;

    public List<TestPlanReportDTO> list(QueryTestPlanReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        String projectId = SessionUtils.getCurrentProjectId();
        if (StringUtils.isNotBlank(projectId)) {
            request.setProjectId(projectId);
        }
        List<TestPlanReportDTO> returnList = extTestPlanReportMapper.list(request);
        return returnList;
    }

    public TestPlanReport genTestPlanReport(String planId, String userId) {
        TestPlanReportDTO returnDTO = new TestPlanReportDTO();

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);

        TestPlanApiCaseExample apiExample = new TestPlanApiCaseExample();
        apiExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<String> apiCaseIdList = testPlanApiCaseMapper.selectByExample(apiExample)
                .stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());

        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<String> scenarioIdList = testPlanScenarioCaseMapper.selectByExample(example)
                .stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList());

        LoadCaseRequest loadCaseRequest = new LoadCaseRequest();
        loadCaseRequest.setTestPlanId(planId);
        loadCaseRequest.setProjectId(testPlan.getProjectId());
        List<String> performanceIdList = testPlanLoadCaseService.list(loadCaseRequest)
                .stream().map(TestPlanLoadCaseDTO::getLoadCaseId).collect(Collectors.toList());

        String testPlanReportID = UUID.randomUUID().toString();
        TestPlanReport testPlanReport = new TestPlanReport();
        testPlanReport.setTestPlanId(planId);
        testPlanReport.setId(testPlanReportID);
        testPlanReport.setCreateTime(System.currentTimeMillis());
        testPlanReport.setUpdateTime(System.currentTimeMillis());
        try {
            testPlanReport.setName(testPlan.getName() + "-" + DateUtils.getTimeString(new Date()));
        } catch (Exception e) {
        }
        testPlanReport.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
        testPlanReport.setCreator(userId);
        testPlanReport.setStartTime(System.currentTimeMillis());
        testPlanReport.setEndTime(System.currentTimeMillis());
        if (apiCaseIdList.isEmpty()) {
            testPlanReport.setIsApiCaseExecuting(false);
        } else {
            testPlanReport.setIsApiCaseExecuting(true);
        }
        if (scenarioIdList.isEmpty()) {
            testPlanReport.setIsScenarioExecuting(false);
        } else {
            testPlanReport.setIsScenarioExecuting(true);
        }
        if (performanceIdList.isEmpty()) {
            testPlanReport.setIsPerformanceExecuting(false);
        } else {
            testPlanReport.setIsPerformanceExecuting(true);
        }
        testPlanReport.setPrincipal(testPlan.getPrincipal());
        testPlanReportMapper.insert(testPlanReport);

        TestPlanReportDataWithBLOBs testPlanReportData = new TestPlanReportDataWithBLOBs();
        testPlanReportData.setId(UUID.randomUUID().toString());
        testPlanReportData.setTestPlanReportId(testPlanReportID);

        testPlanReportData.setApiCaseInfo(JSONArray.toJSONString(apiCaseIdList));
        testPlanReportData.setScenarioInfo(JSONArray.toJSONString(scenarioIdList));
        testPlanReportData.setPerformanceInfo(JSONArray.toJSONString(performanceIdList));
        testPlanReportDataMapper.insert(testPlanReportData);

        //更新TestPlan状态，改为进行中
        testPlan.setStatus(TestPlanStatus.Underway.name());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);

        return testPlanReport;
    }

    public TestPlanReportDTO updateTestPlanReport(String planId) throws Exception {
        TestPlanReportDTO returnDTO = new TestPlanReportDTO();

        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(planId);
        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);

        JSONObject content = JSONObject.parseObject("{\"components\":[1,2,3,4,5]}");
        JSONArray componentIds = content.getJSONArray("components");

        List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);
        List<Issues> issues = testPlanService.buildFunctionalCaseReport(planId, components);
        testPlanService.buildApiCaseReport(planId, components);
        testPlanService.buildScenarioCaseReport(planId, components);

        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        components.forEach(component -> {
            component.afterBuild(testCaseReportMetricDTO);
        });

        String testPlanReportID = UUID.randomUUID().toString();
        TestPlanReport testPlanReport = new TestPlanReport();
        testPlanReport.setTestPlanId(planId);
        testPlanReport.setId(testPlanReportID);
        testPlanReport.setCreateTime(System.currentTimeMillis());
        testPlanReport.setUpdateTime(System.currentTimeMillis());
        testPlanReport.setName(testPlan.getName() + "-" + DateUtils.getTimeString(new Date()));
        testPlanReport.setStatus("error");
        testPlanReport.setTriggerMode(ReportTriggerMode.MANUAL.name());
        testPlanReport.setCreator(SessionUtils.getUser().getId());
        testPlanReport.setStartTime(System.currentTimeMillis());
        testPlanReport.setEndTime(System.currentTimeMillis());
        testPlanReport.setIsApiCaseExecuting(false);
        testPlanReport.setIsScenarioExecuting(false);
        testPlanReport.setIsPerformanceExecuting(false);
        testPlanReport.setPrincipal(testPlan.getPrincipal());

        testPlanReportMapper.insert(testPlanReport);


        TestPlanReportDataWithBLOBs testPlanReportData = new TestPlanReportDataWithBLOBs();
        testPlanReportData.setId(UUID.randomUUID().toString());
        testPlanReportData.setTestPlanReportId(testPlanReportID);
        testPlanReportData.setExecuteResult(JSONObject.toJSONString(testCaseReportMetricDTO.getExecuteResult()));
        testPlanReportData.setFailurTestCases(JSONObject.toJSONString(testCaseReportMetricDTO.getFailureTestCases()));
        testPlanReportData.setModuleExecuteResult(JSONArray.toJSONString(testCaseReportMetricDTO.getModuleExecuteResult()));

        TestPlanApiCaseExample apiExample = new TestPlanApiCaseExample();
        apiExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<String> apiCaseIdList = testPlanApiCaseMapper.selectByExample(apiExample)
                .stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());

        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<String> scenarioIdList = testPlanScenarioCaseMapper.selectByExample(example)
                .stream().map(TestPlanApiScenario::getApiScenarioId).collect(Collectors.toList());
        List<String> performanceIdList = new ArrayList<>();
        testPlanReportData.setApiCaseInfo(JSONArray.toJSONString(apiCaseIdList));
        testPlanReportData.setScenarioInfo(JSONArray.toJSONString(scenarioIdList));
        testPlanReportData.setPerformanceInfo(JSONArray.toJSONString(performanceIdList));
        testPlanReportData.setIssuesInfo(JSONArray.toJSONString(issues));

        testPlanReportDataMapper.insert(testPlanReportData);

        return returnDTO;
    }

//    public TestCaseReport getTestCaseReport(String id) {
//        return null;
//    }

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

                String testProject = testPlanService.findTestProjectNameByTestPlanID(report.getTestPlanId());
                returnDTO.setProjectName(testProject);
            }
        }
        return returnDTO;
    }

    public synchronized void updateReport(List<String> testPlanReportIdList, String runMode) {
        planReportForeach:
        for (String planReportId : testPlanReportIdList) {
            TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(planReportId);

            QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
            queryTestPlanRequest.setId(testPlanReport.getTestPlanId());
            TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);

            //因为接口案例是单个案例开线程运行， 所以要检查是否都执行完成。全部执行完成时才会进行统一整理
            if (StringUtils.equalsAny(runMode, ApiRunMode.SCHEDULE_API_PLAN.name())) {
                List<String> statusList = extTestPlanApiCaseMapper.getStatusByTestPlanId(testPlan.getId());
                for (String status : statusList) {
                    if (status == null) {
                        continue planReportForeach;
                    }
                }
            }

            testPlanReport.setEndTime(System.currentTimeMillis());
            testPlanReport.setUpdateTime(System.currentTimeMillis());

            JSONObject content = JSONObject.parseObject("{\"components\":[1,2,3,4,5]}");
            JSONArray componentIds = content.getJSONArray("components");
            List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);
            testPlanService.buildApiCaseReport(testPlanReport.getTestPlanId(), components);
            testPlanService.buildScenarioCaseReport(testPlanReport.getTestPlanId(), components);
            testPlanService.buildLoadCaseReport(testPlanReport.getTestPlanId(), components);

            if (StringUtils.equals(runMode, ApiRunMode.SCHEDULE_API_PLAN.name())) {
                testPlanReport.setIsApiCaseExecuting(false);
            } else if (StringUtils.equals(runMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name())) {
                testPlanReport.setIsScenarioExecuting(false);
            } else if (StringUtils.equals(runMode, ApiRunMode.SCHEDULE_PERFORMANCE_TEST.name())) {
                testPlanReport.setIsPerformanceExecuting(false);
            }
            TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
            components.forEach(component -> {
                component.afterBuild(testCaseReportMetricDTO);
            });

            this.update(testPlanReport);

            TestPlanReportDataExample example = new TestPlanReportDataExample();
            example.createCriteria().andTestPlanReportIdEqualTo(planReportId);
            List<TestPlanReportDataWithBLOBs> testPlanReportDataList = testPlanReportDataMapper.selectByExampleWithBLOBs(example);
            if (!testPlanReportDataList.isEmpty()) {
                TestPlanReportDataWithBLOBs testPlanReportData = testPlanReportDataList.get(0);
                testPlanReportData.setExecuteResult(JSONObject.toJSONString(testCaseReportMetricDTO.getExecuteResult()));
                testPlanReportData.setFailurTestCases(JSONObject.toJSONString(testCaseReportMetricDTO.getFailureTestCases()));
                testPlanReportData.setModuleExecuteResult(JSONArray.toJSONString(testCaseReportMetricDTO.getModuleExecuteResult()));
                testPlanReportDataMapper.updateByPrimaryKeyWithBLOBs(testPlanReportData);
            }


        }
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
                //发送通知
                sendMessage(report);
            } catch (Exception e) {

            }
        }
        testPlanReportMapper.updateByPrimaryKey(report);
    }

    public void sendMessage(TestPlanReport testPlanReport) {
        TestPlan testPlan = testPlanService.getTestPlan(testPlanReport.getTestPlanId());
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

        successContext = "接口测试定时任务通知:'" + testPlan.getName() + "'执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
        failedContext = "接口测试定时任务通知:'" + testPlan.getName() + "'执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
        subject = Translator.get("task_notification");

        if (StringUtils.equals("Success", testPlanReport.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        } else {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("testName", testPlan.getName());
        paramMap.put("id", testPlanReport.getId());
        paramMap.put("type", "api");
        paramMap.put("url", url);
        paramMap.put("status", testPlanReport.getStatus());
        NoticeModel noticeModel = NoticeModel.builder()
                .successContext(successContext)
                .successMailTemplate("ApiSuccessfulNotification")
                .failedContext(failedContext)
                .failedMailTemplate("ApiFailedNotification")
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
    public void updatePerformanceInfo(TestPlanReport testPlanReport, List<String> performaneReportIDList) {
        TestPlanReportDataExample example = new TestPlanReportDataExample();
        example.createCriteria().andTestPlanReportIdEqualTo(testPlanReport.getId());
        List<TestPlanReportDataWithBLOBs> reportDataList = testPlanReportDataMapper.selectByExampleWithBLOBs(example);
        for (TestPlanReportDataWithBLOBs models : reportDataList) {
            models.setPerformanceInfo(JSONArray.toJSONString(performaneReportIDList));
            testPlanReportDataMapper.updateByPrimaryKeyWithBLOBs(models);
        }
    }

    public void updatePerformanceTestStatus(String reportId) {
        List<String> testPlanReportId = extTestPlanMapper.findIdByPerformanceReportId(reportId);
        this.updateReport(testPlanReportId, ApiRunMode.SCHEDULE_PERFORMANCE_TEST.name());
    }

    public void delete(List<String> testPlanReportIdList) {
        for (String testPlanReportId : testPlanReportIdList) {
            testPlanReportMapper.deleteByPrimaryKey(testPlanReportId);
            TestPlanReportDataExample example = new TestPlanReportDataExample();
            example.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId);
            testPlanReportDataMapper.deleteByExample(example);
        }
    }
}
