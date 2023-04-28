package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.api.service.ApiDefinitionExecResultService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.ShareInfoService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.TestPlanExecuteReportDTO;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.service.ProjectService;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.report.QueryTestPlanReportRequest;
import io.metersphere.track.request.report.TestPlanReportSaveRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.TestPlanRunRequest;
import io.metersphere.track.service.utils.TestPlanRequestUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    @Lazy
    @Resource
    TestPlanService testPlanService;
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
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Lazy
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private TestPlanExecutionQueueMapper testPlanExecutionQueueMapper;
    @Resource
    private TestPlanMessageService testPlanMessageService;

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

        // 设置测试计划报告成功率
        setTestPlanReportPassRate(list);
        return list;
    }

    public void setTestPlanReportPassRate(List<TestPlanReportDTO> list) {
        for (TestPlanReportDTO testPlanReportDTO : list) {
            // 如果数据库查询成功率字段为空或 0 则重新计算一次
            if (testPlanReportDTO.getPassRate() == null || testPlanReportDTO.getPassRate() == 0) {
                TestPlanReportContentExample example = new TestPlanReportContentExample();
                example.createCriteria().andTestPlanReportIdEqualTo(testPlanReportDTO.getId());
                List<TestPlanReportContentWithBLOBs> testPlanReportContents = testPlanReportContentMapper.selectByExampleWithBLOBs(example);

                if (CollectionUtils.isNotEmpty(testPlanReportContents)) {
                    TestPlanReportContentWithBLOBs testPlanReportContent = testPlanReportContents.get(0);
                    if (testPlanReportContent != null) {
                        if (this.isDynamicallyGenerateReports(testPlanReportContent)) {
                            String planId = testPlanReportDTO.getTestPlanId();
                            TestPlanSimpleReportDTO report = new TestPlanSimpleReportDTO();
                            Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

                            TestPlanExecuteReportDTO testPlanExecuteReportDTO = genTestPlanExecuteReportDTOByTestPlanReportContent(testPlanReportContent);
                            // 功能用例
                            TestPlanUtils.buildStatusResultMap(extTestPlanTestCaseMapper.selectForPlanReport(planId), statusResultMap, report, TestPlanTestCaseStatus.Pass.name());

                            // 测试计划报告各用例集合
                            List<PlanReportCaseDTO> planReportCaseDTOS = null;

                            if (testPlanExecuteReportDTO == null) {
                                // 接口用例
                                planReportCaseDTOS = extTestPlanApiCaseMapper.selectForPlanReport(planId);
                                TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, "success");
                                // 场景用例
                                planReportCaseDTOS = extTestPlanScenarioCaseMapper.selectForPlanReport(planId);
                                TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, "Success");
                                // 性能用例
                                planReportCaseDTOS = extTestPlanLoadCaseMapper.selectForPlanReport(planId);
                                TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, TestPlanLoadCaseStatus.success.name());
                            } else {
                                // 报告 ID 集合
                                List<String> reportIds = null;
                                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap())) {
                                    // 接口用例
                                    reportIds = new ArrayList<>(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap().values());
                                    planReportCaseDTOS = extApiDefinitionExecResultMapper.selectForPlanReport(reportIds);
                                    TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, "success");
                                }
                                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
                                    // 场景用例
                                    reportIds = new ArrayList<>(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap().values());
                                    planReportCaseDTOS = extApiScenarioReportMapper.selectForPlanReport(reportIds);
                                    TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, "Success");
                                }
                                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap())) {
                                    // 性能用例
                                    reportIds = new ArrayList<>(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap().values());
                                    planReportCaseDTOS = extLoadTestReportMapper.selectForPlanReport(reportIds);
                                    // 性能测试的报告状态跟用例的执行状态不一样
                                    planReportCaseDTOS.forEach(item -> {
                                        if (item.getStatus().equals(PerformanceTestStatus.Completed.name())) {
                                            item.setStatus(TestPlanLoadCaseStatus.success.name());
                                        } else if (item.getStatus().equals(PerformanceTestStatus.Error.name())) {
                                            item.setStatus(TestPlanLoadCaseStatus.error.name());
                                        } else {
                                            item.setStatus(TestPlanLoadCaseStatus.run.name());
                                        }
                                    });
                                    TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, TestPlanLoadCaseStatus.success.name());
                                }
                            }

                            report.setExecuteRate(0.0);
                            report.setPassRate(0.0);

                            // 设置成功率
                            if (report.getCaseCount() != null && report.getCaseCount() != 0) {
                                report.setExecuteRate(report.getExecuteCount() * 1.0 / report.getCaseCount());
                                report.setPassRate(report.getPassCount() * 1.0 / report.getCaseCount());
                            }
                            testPlanReportDTO.setPassRate(report.getPassRate());
                        }
                    }
                }
            }
        }
    }

    public TestPlanScheduleReportInfoDTO genTestPlanReportBySchedule(String planReportId, String planId, String userId, String triggerMode) {
        TestPlanReport testPlanReport = this.getTestPlanReport(planReportId);
        TestPlanScheduleReportInfoDTO returnDTO = new TestPlanScheduleReportInfoDTO();
        if (testPlanReport != null) {
            returnDTO.setTestPlanReport(testPlanReport);
        }

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
        List<TestPlanLoadCaseDTO> testPlanLoadCaseDTOList = extTestPlanLoadCaseMapper.selectTestPlanLoadCaseList(loadCaseRequest);
        for (TestPlanLoadCaseDTO dto : testPlanLoadCaseDTOList) {
            performanceIdMap.put(dto.getId(), dto.getLoadCaseId());
        }

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

        if (testPlanReport == null) {
            returnDTO = this.genTestPlanReport(saveRequest);
        }
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
    public TestPlanScheduleReportInfoDTO genTestPlanReport(TestPlanReportSaveRequest saveRequest) {
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
        } else {
            testPlanReport.setIsApiCaseExecuting(saveRequest.isApiCaseIsExecuting());
            testPlanReport.setIsScenarioExecuting(saveRequest.isScenarioIsExecuting());
            testPlanReport.setIsPerformanceExecuting(saveRequest.isPerformanceIsExecuting());
        }

        if (testPlanReport.getIsScenarioExecuting() || testPlanReport.getIsApiCaseExecuting() || testPlanReport.getIsPerformanceExecuting()) {
            testPlanReport.setStatus(TestPlanReportStatus.RUNNING.name());
        } else {
            testPlanReport.setStatus(TestPlanReportStatus.COMPLETED.name());
        }

        testPlanReportMapper.insert(testPlanReport);

        //更新TestPlan状态，改为进行中
        testPlan.setStatus(TestPlanStatus.Underway.name());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);

        TestPlanScheduleReportInfoDTO returnDTO = new TestPlanScheduleReportInfoDTO();
        returnDTO.setTestPlanReport(testPlanReport);
        return returnDTO;
    }

    public void genTestPlanReportContent(TestPlanScheduleReportInfoDTO returnDTO) {
        TestPlanReportContentWithBLOBs testPlanReportContent = new TestPlanReportContentWithBLOBs();
        testPlanReportContent.setId(UUID.randomUUID().toString());
        testPlanReportContent.setTestPlanReportId(returnDTO.getTestPlanReport().getId());
        if (testPlanReportContent.getStartTime() == null) {
            testPlanReportContent.setStartTime(System.currentTimeMillis());
        }
        if (testPlanReportContent.getEndTime() == null) {
            testPlanReportContent.setEndTime(System.currentTimeMillis());
        }
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

    public TestPlanReportContentWithBLOBs updateReport(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs reportContent) {
        if (testPlanReport == null || reportContent == null) {
            return null;
        }
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        TestPlanReportBuildResultDTO reportBuildResult = testPlanService.buildPlanReport(testPlanReport, reportContent);
        TestPlanSimpleReportDTO reportDTO = reportBuildResult.getTestPlanSimpleReportDTO();
        reportDTO.setStartTime(testPlanReport.getStartTime());
        reportContent = parseReportDaoToReportContent(testPlanReport.getStatus(), reportDTO, reportContent);
        this.updatePassRateAndApiBaseInfoFromReportContent(testPlanReport.getStatus(), reportDTO, reportContent, reportBuildResult.isApiBaseInfoChanged());
        return reportContent;
    }

    private void updatePassRateAndApiBaseInfoFromReportContent(String status, TestPlanSimpleReportDTO reportDTO, TestPlanReportContentWithBLOBs reportContent, boolean apiBaseInfoChanged) {
        // 如果报告已结束，则更新测试计划报告通过率字段 passRate
        if (!StringUtils.equalsIgnoreCase(status, "running") && (Double.compare(reportContent.getPassRate(), reportDTO.getPassRate()) != 0 || apiBaseInfoChanged)) {
            TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
            contentExample.createCriteria().andTestPlanReportIdEqualTo(reportContent.getTestPlanReportId());
            TestPlanReportContentWithBLOBs content = new TestPlanReportContentWithBLOBs();
            content.setPassRate(reportDTO.getPassRate());
            if (apiBaseInfoChanged) {
                content.setApiBaseCount(reportContent.getApiBaseCount());
            }
            testPlanReportContentMapper.updateByExampleSelective(content, contentExample);
        }

    }

    public TestPlanReport finishedTestPlanReport(String testPlanReportId, String status) {
        TestPlanReport testPlanReport = this.getTestPlanReport(testPlanReportId);
        if (testPlanReport != null && StringUtils.equalsAnyIgnoreCase(testPlanReport.getStatus(), "stopped",
                TestPlanReportStatus.COMPLETED.name())) {
            return testPlanReport;
        }
        boolean isSendMessage = false;
        TestPlanReportContentWithBLOBs content = null;
        if (testPlanReport != null) {
            if (StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), ExecuteResult.TEST_PLAN_RUNNING.toString())) {
                isSendMessage = true;
            }
            //初始化测试计划包含组件信息
            int[] componentIndexArr = new int[]{1, 3, 4};
            testPlanReport.setComponents(JSONArray.toJSONString(componentIndexArr));
            //如果测试案例没有未结束的功能用例，则更新最后结束日期。
            TestPlanTestCaseMapper testPlanTestCaseMapper = CommonBeanFactory.getBean(TestPlanTestCaseMapper.class);
            TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
            testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(testPlanReport.getTestPlanId()).andStatusNotEqualTo("Prepare");
            long endTime = System.currentTimeMillis();
            long testCaseCount = testPlanTestCaseMapper.countByExample(testPlanTestCaseExample);
            boolean updateTestPlanTime = testCaseCount > 0;
            if (updateTestPlanTime) {
                testPlanReport.setEndTime(endTime);
                testPlanReport.setUpdateTime(endTime);
            }

            TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
            contentExample.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId);
            List<TestPlanReportContentWithBLOBs> contents = testPlanReportContentMapper.selectByExampleWithBLOBs(contentExample);
            if (CollectionUtils.isNotEmpty(contents)) {
                content = contents.get(0);
            }

            //计算测试计划状态
            if (StringUtils.equalsIgnoreCase(status, TestPlanReportStatus.COMPLETED.name())) {
                testPlanReport.setStatus(TestPlanReportStatus.SUCCESS.name());
            } else {
                testPlanReport.setStatus(status);
            }
            //更新测试计划并发送通知
            testPlanReport.setIsApiCaseExecuting(false);
            testPlanReport.setIsScenarioExecuting(false);
            testPlanReport.setIsPerformanceExecuting(false);

            if (content != null) {
                //更新content表对结束日期,并计算报表信息
                content.setStartTime(testPlanReport.getStartTime());
                content.setEndTime(endTime);
                this.initTestPlanReportBaseCount(testPlanReport, content);
                testPlanReportContentMapper.updateByExampleSelective(content, contentExample);
            }
            this.executeTestPlanInQueue(testPlanReportId);
            testPlanReportMapper.updateByPrimaryKey(testPlanReport);
        }
        //发送通知
        testPlanMessageService.checkTestPlanStatusAndSendMessage(testPlanReport, content, isSendMessage);
        return testPlanReport;
    }

    private void executeTestPlanInQueue(String testPlanReportId) {
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
                JSONObject jsonObject = JSONObject.parseObject(testPlan.getRunModeConfig());
                TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                TestPlanRunRequest runRequest = JSON.toJavaObject(jsonObject, TestPlanRunRequest.class);
                runRequest.setReportId(testPlanExecutionQueue.getReportId());
                testPlanService.runPlan(runRequest);
            }
        }
    }

    private void initTestPlanReportBaseCount(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs reportContent) {
        if (testPlanReport != null && reportContent != null) {
            TestPlanReportBuildResultDTO reportBuildResultDTO = testPlanService.buildPlanReport(testPlanReport, reportContent);
            //如果场景报告中出现了 Waiting 或者 Running 则不保存
            boolean isReportAllFinished = testPlanService.checkAllReportFinished(reportBuildResultDTO.getTestPlanSimpleReportDTO());
            if (isReportAllFinished) {
                reportContent.setApiBaseCount(JSONObject.toJSONString(reportBuildResultDTO.getTestPlanSimpleReportDTO()));
                reportContent.setPassRate(reportBuildResultDTO.getTestPlanSimpleReportDTO().getPassRate());
            } else {
                reportContent.setApiBaseCount(null);
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
        TestPlanSimpleReportDTO reportDTO = testPlanService.buildPlanReport(testPlan.getId(), false);
        if (!testPlanReportContentList.isEmpty()) {
            testPlanReportContent = testPlanReportContentList.get(0);
            testPlanReportContentMapper.updateByPrimaryKeySelective(parseReportDaoToReportContent(testPlanReport.getStatus(), reportDTO, testPlanReportContent));
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
        testPlanMessageService.checkTestPlanStatusAndSendMessage(testPlanReport, null, false);
    }

    public TestPlanReportContentWithBLOBs parseReportDaoToReportContent(String testPlanReportStatus, TestPlanSimpleReportDTO reportDTO, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        String id = testPlanReportContentWithBLOBs.getId();
        String testPlanReportId = testPlanReportContentWithBLOBs.getTestPlanReportId();
        if (testPlanReportContentWithBLOBs.getEndTime() != null) {
            reportDTO.setEndTime(testPlanReportContentWithBLOBs.getEndTime());
        }
        BeanUtils.copyBean(testPlanReportContentWithBLOBs, reportDTO);
        testPlanReportContentWithBLOBs.setId(id);
        testPlanReportContentWithBLOBs.setTestPlanReportId(testPlanReportId);
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
        if (reportDTO.getErrorReportCases() != null) {
            testPlanReportContentWithBLOBs.setErrorReportCases(JSONObject.toJSONString(reportDTO.getErrorReportCases()));
        }
        if (reportDTO.getErrorReportScenarios() != null) {
            testPlanReportContentWithBLOBs.setErrorReportScenarios(JSONObject.toJSONString(reportDTO.getErrorReportScenarios()));
        }
        if (reportDTO.getUnExecuteCases() != null) {
            testPlanReportContentWithBLOBs.setUnExecuteCases(JSONObject.toJSONString(reportDTO.getUnExecuteCases()));
        }
        if (reportDTO.getUnExecuteScenarios() != null) {
            testPlanReportContentWithBLOBs.setUnExecuteScenarios(JSONObject.toJSONString(reportDTO.getUnExecuteScenarios()));
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

    public TestPlanReport getTestPlanReport(String planId) {
        return testPlanReportMapper.selectByPrimaryKey(planId);
    }

    public List<String> getTestPlanReportIdsByLoadTestReportId(String loadTestReportId) {
        List<String> testPlanReportId = extTestPlanMapper.findIdByPerformanceReportId(loadTestReportId);
        return testPlanReportId;
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
            deleteReportIds = this.getAllApiIdsByFrontedSelect(request.getFilters(), request.getName(), request.getProjectId(), request.getUnSelectIds(), request.getCombine());
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

    public TestPlanSimpleReportDTO getReport(String reportId) {
        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(reportId);
        List<TestPlanReportContentWithBLOBs> testPlanReportContents = testPlanReportContentMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(testPlanReportContents)) {
            return null;
        }
        TestPlanReportContentWithBLOBs testPlanReportContent = testPlanReportContents.get(0);
        if (testPlanReportContent == null) {
            return null;
        }
        if (this.isDynamicallyGenerateReports(testPlanReportContent)) {
            LogUtil.info("----> GenerateReports: " + JSONObject.toJSONString(testPlanReportContent));
            testPlanReportContent = this.dynamicallyGenerateReports(testPlanReportContent);
            LogUtil.info("----> GenerateReports OVER: " + JSONObject.toJSONString(testPlanReportContent));
        }
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
        if (StringUtils.isNotBlank(testPlanReportContent.getErrorReportCases())) {
            testPlanReportDTO.setErrorReportCases(JSONObject.parseArray(testPlanReportContent.getErrorReportCases(), TestPlanFailureApiDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getErrorReportScenarios())) {
            testPlanReportDTO.setErrorReportScenarios(JSONObject.parseArray(testPlanReportContent.getErrorReportScenarios(), TestPlanFailureScenarioDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getUnExecuteCases())) {
            testPlanReportDTO.setUnExecuteCases(JSONObject.parseArray(testPlanReportContent.getUnExecuteCases(), TestPlanFailureApiDTO.class));
        }
        if (StringUtils.isNotBlank(testPlanReportContent.getUnExecuteScenarios())) {
            testPlanReportDTO.setUnExecuteScenarios(JSONObject.parseArray(testPlanReportContent.getUnExecuteScenarios(), TestPlanFailureScenarioDTO.class));
        }
        testPlanReportDTO.setId(reportId);
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportContent.getTestPlanReportId());
        testPlanReportDTO.setName(testPlanReport.getName());
        return testPlanReportDTO;
    }

    private boolean isDynamicallyGenerateReports(TestPlanReportContentWithBLOBs testPlanReportContent) {
        return testPlanReportContent != null &&
                (StringUtils.isNotEmpty(testPlanReportContent.getPlanApiCaseReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanScenarioReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanLoadCaseReportStruct()));
    }

    private TestPlanReportContentWithBLOBs dynamicallyGenerateReports(TestPlanReportContentWithBLOBs testPlanReportContent) {
        TestPlanReport report = testPlanReportMapper.selectByPrimaryKey(testPlanReportContent.getTestPlanReportId());
        testPlanReportContent = this.updateReport(report, testPlanReportContent);
        return testPlanReportContent;
    }

    public void createTestPlanReportContentReportIds(String testPlanReportID, Map<String, String> apiCaseReportMap, Map<String, String> scenarioReportIdMap, Map<String, String> loadCaseReportIdMap) {
        TestPlanReportContentWithBLOBs content = new TestPlanReportContentWithBLOBs();
        content.setId(UUID.randomUUID().toString());
        content.setTestPlanReportId(testPlanReportID);

        if (MapUtils.isNotEmpty(apiCaseReportMap)) {
            List<TestPlanFailureApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureListByIds(apiCaseReportMap.keySet(), null);
            for (TestPlanFailureApiDTO dto : apiTestCases) {
                dto.setReportId(apiCaseReportMap.get(dto.getId()));
            }
            content.setPlanApiCaseReportStruct(JSONObject.toJSONString(apiTestCases));
        }
        if (MapUtils.isNotEmpty(scenarioReportIdMap)) {
            List<TestPlanFailureScenarioDTO> apiTestCases =
                    extTestPlanScenarioCaseMapper.getFailureListByIds(scenarioReportIdMap.keySet(), null);
            for (TestPlanFailureScenarioDTO dto : apiTestCases) {
                dto.setReportId(scenarioReportIdMap.get(dto.getId()));
            }
            content.setPlanScenarioReportStruct(JSONObject.toJSONString(apiTestCases));
        }
        if (MapUtils.isNotEmpty(loadCaseReportIdMap)) {
            content.setPlanLoadCaseReportStruct(JSONObject.toJSONString(loadCaseReportIdMap));
        }
        testPlanReportContentMapper.insert(content);
    }

    public TestPlanExecuteReportDTO genTestPlanExecuteReportDTOByTestPlanReportContent(TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        Map<String, String> testPlanApiCaseIdAndReportIdMap = new LinkedHashMap<>();
        Map<String, String> testPlanScenarioIdAndReportIdMap = new LinkedHashMap<>();
        Map<String, String> testPlanLoadCaseIdAndReportIdMap = new LinkedHashMap<>();
        Map<String, TestPlanFailureApiDTO> apiCaseInfoDTOMap = new LinkedHashMap<>();
        Map<String, TestPlanFailureScenarioDTO> scenarioInfoDTOMap = new LinkedHashMap<>();

        if (testPlanReportContentWithBLOBs != null) {
            if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct())) {
                List<TestPlanFailureApiDTO> apiCaseInfoDTOList = null;
                try {
                    apiCaseInfoDTOList = JSONArray.parseArray(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct(), TestPlanFailureApiDTO.class);
                } catch (Exception ignored) {
                }
                if (apiCaseInfoDTOList == null) {
                    try {
                        testPlanApiCaseIdAndReportIdMap = JSONObject.parseObject(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct(), Map.class);
                    } catch (Exception ignored) {
                    }
                } else {
                    for (TestPlanFailureApiDTO item : apiCaseInfoDTOList) {
                        testPlanApiCaseIdAndReportIdMap.put(item.getId(), item.getReportId());
                        apiCaseInfoDTOMap.put(item.getId(), item);
                    }
                }
            }
            if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct())) {
                List<TestPlanFailureScenarioDTO> scenarioInfoDTOList = null;
                try {
                    scenarioInfoDTOList = JSONArray.parseArray(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct(), TestPlanFailureScenarioDTO.class);
                } catch (Exception ignored) {
                }
                if (scenarioInfoDTOList == null) {
                    try {
                        testPlanScenarioIdAndReportIdMap = JSONObject.parseObject(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct(), Map.class);
                    } catch (Exception ignored) {
                    }
                } else {
                    for (TestPlanFailureScenarioDTO item : scenarioInfoDTOList) {
                        testPlanScenarioIdAndReportIdMap.put(item.getId(), item.getReportId());
                        scenarioInfoDTOMap.put(item.getId(), item);
                    }
                }
            }
            if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanLoadCaseReportStruct())) {
                try {
                    testPlanLoadCaseIdAndReportIdMap = JSONObject.parseObject(testPlanReportContentWithBLOBs.getPlanLoadCaseReportStruct(), Map.class);
                } catch (Exception ignore) {
                }
            }
        }
        TestPlanExecuteReportDTO returnDTO = new TestPlanExecuteReportDTO(testPlanApiCaseIdAndReportIdMap, testPlanScenarioIdAndReportIdMap, testPlanLoadCaseIdAndReportIdMap, apiCaseInfoDTOMap, scenarioInfoDTOMap);
        return returnDTO;
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
            boolean hasErrorCase = this.isTestPlanReportHasErrorCase(testPlanReportContent);
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
                    List<TestPlanFailureApiDTO> apiTestCases = JSONArray.parseArray(content.getPlanApiCaseReportStruct(), TestPlanFailureApiDTO.class);
                    List<String> reportIdList = new ArrayList<>();
                    apiTestCases.forEach(item -> {
                        if (StringUtils.isNotEmpty(item.getReportId())) {
                            reportIdList.add(item.getReportId());
                        }
                    });
                    Map<String, String> reportResult = apiDefinitionExecResultService.selectReportResultByReportIds(reportIdList);
                    String defaultStatus = "error";
                    for (TestPlanFailureApiDTO dto : apiTestCases) {
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
                        if (!StringUtils.equalsAnyIgnoreCase(dto.getExecResult(), "success")) {
                            hasErrorCase = true;
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("Parse test plan report api case error! ", e);
                }
            }

            if (!hasErrorCase && StringUtils.isNotEmpty(content.getPlanScenarioReportStruct())) {
                try {
                    List<TestPlanFailureScenarioDTO> scenarioCases = JSONArray.parseArray(content.getPlanScenarioReportStruct(), TestPlanFailureScenarioDTO.class);
                    List<String> reportIdList = new ArrayList<>();
                    scenarioCases.forEach(item -> {
                        if (StringUtils.isNotEmpty(item.getReportId())) {
                            reportIdList.add(item.getReportId());
                        }
                    });
                    String defaultStatus = "Fail";
                    Map<String, String> reportStatus = apiScenarioReportService.getReportStatusByReportIds(reportIdList);

                    for (TestPlanFailureScenarioDTO dto : scenarioCases) {
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
                    LogUtil.error("Parse test plan report cenario case error!", e);
                }
            }
        }
        return hasErrorCase;
    }
}
