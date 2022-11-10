package io.metersphere.plan.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanReportMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.excel.constants.TestPlanTestCaseStatus;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.request.QueryTestPlanRequest;
import io.metersphere.plan.request.TestPlanReportSaveRequest;
import io.metersphere.plan.request.api.TestPlanRunRequest;
import io.metersphere.plan.service.remote.api.*;
import io.metersphere.plan.service.remote.performance.PlanLoadTestReportService;
import io.metersphere.plan.service.remote.performance.PlanTestPlanLoadCaseService;
import io.metersphere.plan.service.remote.ui.PlanTestPlanUiScenarioCaseService;
import io.metersphere.plan.utils.TestPlanRequestUtil;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.request.report.QueryTestPlanReportRequest;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.utils.DiscoveryUtil;
import io.metersphere.xpack.track.dto.IssuesDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
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
    PlanTestPlanLoadCaseService planTestPlanLoadCaseService;
    @Resource
    PlanLoadTestReportService planLoadTestReportService;
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
    private TestPlanPrincipalMapper testPlanPrincipalMapper;
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    //    @Resource
    //    private PerformanceTestService performanceTestService;
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
                            TestPlanStatusCalculator.buildStatusResultMap(extTestPlanTestCaseMapper.selectForPlanReport(planId), statusResultMap, report, TestPlanTestCaseStatus.Pass.name());

                            // 测试计划报告各用例集合
                            List<PlanReportCaseDTO> planReportCaseDTOS;

                            if (testPlanExecuteReportDTO == null) {
                                try {
                                    // 接口用例
                                    planReportCaseDTOS = planTestPlanApiCaseService.selectStatusForPlanReport(planId);
                                    TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
                                } catch (Exception e) {
                                    LogUtil.error(e);
                                }

                                try {
                                    // 场景用例
                                    planReportCaseDTOS = planTestPlanScenarioCaseService.selectStatusForPlanReport(planId);
                                    TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
                                } catch (Exception e) {
                                    LogUtil.error(e);
                                }

                                try {
                                    // 性能用例
                                    planReportCaseDTOS = planTestPlanLoadCaseService.selectStatusForPlanReport(planId);
                                    TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
                                } catch (Exception e) {
                                    LogUtil.error(e);
                                }
                            } else {
                                // 报告 ID 集合
                                List<String> reportIds = null;
                                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap())) {
                                    // 接口用例
                                    try {
                                        reportIds = new ArrayList<>(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap().values());
                                        planReportCaseDTOS = planApiDefinitionExecResultService.selectForPlanReport(reportIds);
                                        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
                                    } catch (Exception e) {
                                        LogUtil.error(e);
                                    }
                                }
                                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
                                    try {
                                        // 场景用例
                                        reportIds = new ArrayList<>(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap().values());
                                        planReportCaseDTOS = planApiScenarioReportService.selectForPlanReport(reportIds);
                                        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
                                    } catch (Exception e) {
                                        LogUtil.error(e);
                                    }
                                }
                                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanUiScenarioIdAndReportIdMap())) {
                                    try {
                                        // 场景用例
                                        reportIds = new ArrayList<>(testPlanExecuteReportDTO.getTestPlanUiScenarioIdAndReportIdMap().values());
                                        planReportCaseDTOS = planApiScenarioReportService.selectForPlanReport(reportIds);
                                        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
                                    } catch (Exception e) {
                                        LogUtil.error(e);
                                    }
                                }
                                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap())) {
                                    try {
                                        // 性能用例
                                        reportIds = new ArrayList<>(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap().values());
                                        planReportCaseDTOS = planLoadTestReportService.getPlanReportCaseDTO(reportIds);
                                        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
                                    } catch (Exception e) {
                                        LogUtil.error(e);
                                    }
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
            TestPlanApiReportInfoDTO testPlanApiReportInfoDTO = planTestPlanScenarioCaseService.genApiReportInfoForSchedule(planId, runModeConfigDTO);
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


        if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
            Map<String, String> performanceIdMap = new LinkedHashMap<>();
            List<TestPlanLoadCaseDTO> testPlanLoadCaseDTOList = planTestPlanLoadCaseService.list(planId);
            for (TestPlanLoadCaseDTO dto : testPlanLoadCaseDTOList) {
                performanceIdMap.put(dto.getId(), dto.getLoadCaseId());
            }
            saveRequest.setPerformanceIsExecuting(!performanceIdMap.isEmpty());
            saveRequest.setPerformanceIdMap(performanceIdMap);

            returnDTO.setPerformanceIdMap(performanceIdMap);
        }


        if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
            Map<String, String> uiScenarioIdMap = new LinkedHashMap<>();
            List<TestPlanUiScenario> testPlanUiScenarioList = planTestPlanUiScenarioCaseService.list(planId);
            for (TestPlanUiScenario dto : testPlanUiScenarioList) {
                uiScenarioIdMap.put(dto.getId(), dto.getUiScenarioId());
            }
            saveRequest.setUiScenarioIsExecuting(!uiScenarioIdMap.isEmpty());
            saveRequest.setUiScenarioIdMap(uiScenarioIdMap);

            returnDTO.setUiScenarioIdMap(uiScenarioIdMap);
        }


        if (testPlanReport == null) {
            returnDTO = this.genTestPlanReport(saveRequest, runInfoDTO);
        }
        returnDTO.setPlanScenarioIdMap(saveRequest.getScenarioIdMap());
        returnDTO.setApiTestCaseDataMap(saveRequest.getApiCaseIdMap());
        returnDTO.setPerformanceIdMap(saveRequest.getPerformanceIdMap());
        returnDTO.setUiScenarioIdMap(saveRequest.getUiScenarioIdMap());
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

        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();

        if (saveRequest.isCountResources()) {
            String planId = saveRequest.getPlanId();

            if (serviceIdSet.contains(MicroServiceName.API_TEST)) {

                testPlanReport.setIsApiCaseExecuting(planTestPlanApiCaseService.isCaseExecuting(planId));
                testPlanReport.setIsScenarioExecuting(planTestPlanScenarioCaseService.isCaseExecuting(planId));
            }


            if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
                testPlanReport.setIsUiScenarioExecuting(planTestPlanUiScenarioCaseService.isCaseExecuting(planId));
            }


            if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
                testPlanReport.setIsPerformanceExecuting(planTestPlanLoadCaseService.isCaseExecuting(planId, testPlan.getProjectId()));
            }

        } else {
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

    public TestPlanReportContentWithBLOBs updateReport(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs reportContent) {
        if (testPlanReport == null || reportContent == null) {
            return null;
        }
        TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        TestPlanReportBuildResultDTO reportBuildResult = testPlanService.buildPlanReport(testPlanReport, reportContent);
        TestPlanSimpleReportDTO reportDTO = reportBuildResult.getTestPlanSimpleReportDTO();
        reportDTO.setStartTime(testPlanReport.getStartTime());
        reportContent = parseReportDaoToReportContent(reportDTO, reportContent);
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
        if (testPlanReport != null && StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), "stopped")) {
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
            testPlanReport.setComponents(JSON.toJSONString(componentIndexArr));
            //如果测试案例没有未结束的功能用例，则更新最后结束日期。
            TestPlanTestCaseMapper testPlanTestCaseMapper = CommonBeanFactory.getBean(TestPlanTestCaseMapper.class);
            TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
            testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(testPlanReport.getTestPlanId()).andStatusNotEqualTo("Prepare");
            long endTime = System.currentTimeMillis();
            long testCaseCount = testPlanTestCaseMapper.countByExample(testPlanTestCaseExample);
            boolean updateTestPlanTime = testCaseCount > 0;
            if (updateTestPlanTime && !StringUtils.equalsAnyIgnoreCase(testPlanReport.getStatus(), APITestStatus.Rerunning.name())) {
                testPlanReport.setEndTime(endTime);
                testPlanReport.setUpdateTime(endTime);
            }

            TestPlanReportContentExample contentExample = new TestPlanReportContentExample();
            contentExample.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId);
            List<TestPlanReportContentWithBLOBs> contents = testPlanReportContentMapper.selectByExampleWithBLOBs(contentExample);
            if (CollectionUtils.isNotEmpty(contents)) {
                content = contents.get(0);
                content.setApiBaseCount(null);
                content.setPassRate(null);
                extTestPlanReportMapper.setApiBaseCountAndPassRateIsNullById(content.getId());
            }

            //更新测试计划并发送通知
            testPlanReport.setIsApiCaseExecuting(false);
            testPlanReport.setIsScenarioExecuting(false);
            testPlanReport.setIsPerformanceExecuting(false);
            testPlanReport.setIsUiScenarioExecuting(false);
            //计算测试计划状态

            try {
                if (StringUtils.equalsIgnoreCase(status, TestPlanReportStatus.COMPLETED.name())) {
                    testPlanReport.setStatus(TestPlanReportStatus.SUCCESS.name());
                    HttpHeaderUtils.runAsUser("admin");
                    testPlanService.checkStatus(testPlanReport.getTestPlanId());
                } else {
                    testPlanReport.setStatus(status);
                }

                if (content != null) {
                    //更新content表对结束日期
                    if (!StringUtils.equalsAnyIgnoreCase(testPlanReport.getStatus(), APITestStatus.Rerunning.name())) {
                        content.setStartTime(testPlanReport.getStartTime());
                        content.setEndTime(endTime);
                    }
                    this.initTestPlanReportBaseCount(testPlanReport, content);
                    testPlanReportContentMapper.updateByExampleSelective(content, contentExample);
                }
            } finally {
                HttpHeaderUtils.clearUser();
            }

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
                if (CollectionUtils.isEmpty(planExecutionQueueList)) {
                    return testPlanReport;
                }
                TestPlanExecutionQueue testPlanExecutionQueue = planExecutionQueueList.get(0);
                TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanExecutionQueue.getTestPlanId());
                Map jsonObject = JSON.parseMap(testPlan.getRunModeConfig());
                TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                TestPlanRunRequest runRequest = JSON.parseObject(JSON.toJSONString(jsonObject), TestPlanRunRequest.class);
                runRequest.setReportId(testPlanExecutionQueue.getReportId());
                testPlanService.runPlan(runRequest);
            }
            testPlanReportMapper.updateByPrimaryKey(testPlanReport);
        }
        //发送通知
        testPlanMessageService.checkTestPlanStatusAndSendMessage(testPlanReport, content, isSendMessage);
        return testPlanReport;
    }

    private void initTestPlanReportBaseCount(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs reportContent) {
        if (testPlanReport != null && reportContent != null) {
            TestPlanReportBuildResultDTO reportBuildResultDTO = testPlanService.buildPlanReport(testPlanReport, reportContent);
            reportContent.setApiBaseCount(JSON.toJSONString(reportBuildResultDTO.getTestPlanSimpleReportDTO()));
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
        testPlanReportMapper.updateByPrimaryKey(testPlanReport);
        testPlanMessageService.checkTestPlanStatusAndSendMessage(testPlanReport, null, false);
    }

    public TestPlanReportContentWithBLOBs parseReportDaoToReportContent(TestPlanSimpleReportDTO reportDTO, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        String id = testPlanReportContentWithBLOBs.getId();
        String testPlanReportId = testPlanReportContentWithBLOBs.getTestPlanReportId();
        if (testPlanReportContentWithBLOBs.getEndTime() != null) {
            reportDTO.setEndTime(testPlanReportContentWithBLOBs.getEndTime());
        }
        BeanUtils.copyBean(testPlanReportContentWithBLOBs, reportDTO);
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
    private String getTestPlanReportStatus(TestPlanReport testPlanReport, TestPlanSimpleReportDTO reportDTO) {
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

            //            //删除关联资源对应的报告ID
            //            apiDefinitionExecResultService.deleteByRelevanceTestPlanReportIds(testPlanReportIdList);
            //            apiScenarioReportService.deleteByRelevanceTestPlanReportIds(testPlanReportIdList);
            //            performanceTestService.deleteByRelevanceTestPlanReportIds(testPlanReportIdList);

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
        this.delete(deleteReportIds);
    }

    private void deleteReportBatch(List<String> reportIds) {
        int handleCount = 5000;
        List<String> handleIdList;

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanReportMapper planReportMapper = sqlSession.getMapper(TestPlanReportMapper.class);
        TestPlanReportDataMapper planReportDataMapper = sqlSession.getMapper(TestPlanReportDataMapper.class);
        TestPlanReportContentMapper planReportContentMapper = sqlSession.getMapper(TestPlanReportContentMapper.class);

        //        ApiDefinitionExecResultMapper batchDefinitionExecResultMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
        //        ApiScenarioReportMapper batchScenarioReportMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
        //        LoadTestReportMapper batchLoadTestReportMapper = sqlSession.getMapper(LoadTestReportMapper.class);

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

    public TestPlanSimpleReportDTO getShareDbReport(ShareInfo shareInfo, String reportId) {
        HttpHeaderUtils.runAsUser(shareInfo.getCreateUserId());
        try {
            return getReport(reportId);
        } finally {
            HttpHeaderUtils.clearUser();
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
        if (testPlanReportContent == null) {
            return null;
        }
        if (this.isDynamicallyGenerateReports(testPlanReportContent)) {
            testPlanReportContent = this.dynamicallyGenerateReports(testPlanReportContent);
        }
        TestPlanSimpleReportDTO testPlanReportDTO = new TestPlanSimpleReportDTO();
        BeanUtils.copyBean(testPlanReportDTO, testPlanReportContent);
        this.generateEnvironmentInfo(testPlanReportDTO, reportId);

        testPlanReportDTO.setFunctionResult(
                getReportContentResultObject(testPlanReportContent.getFunctionResult(), TestPlanFunctionResultReportDTO.class)
        );

        testPlanReportDTO.setApiResult(
                getReportContentResultObject(testPlanReportContent.getApiResult(), TestPlanApiResultReportDTO.class)
        );

        testPlanReportDTO.setLoadResult(
                getReportContentResultObject(testPlanReportContent.getLoadResult(), TestPlanLoadResultReportDTO.class)
        );

        testPlanReportDTO.setFunctionAllCases(
                getReportContentResultArray(testPlanReportContent.getFunctionAllCases(), TestPlanCaseDTO.class)
        );

        testPlanReportDTO.setIssueList(
                getReportContentResultArray(testPlanReportContent.getIssueList(), IssuesDao.class)
        );

        testPlanReportDTO.setApiAllCases(
                getReportContentResultArray(testPlanReportContent.getApiAllCases(), TestPlanFailureApiDTO.class)
        );

        testPlanReportDTO.setApiFailureCases(
                getReportContentResultArray(testPlanReportContent.getApiFailureCases(), TestPlanFailureApiDTO.class)
        );

        testPlanReportDTO.setScenarioAllCases(
                getReportContentResultArray(testPlanReportContent.getScenarioAllCases(), TestPlanFailureScenarioDTO.class)
        );

        testPlanReportDTO.setScenarioFailureCases(
                getReportContentResultArray(testPlanReportContent.getScenarioFailureCases(), TestPlanFailureScenarioDTO.class)
        );

        testPlanReportDTO.setLoadAllCases(
                getReportContentResultArray(testPlanReportContent.getLoadAllCases(), TestPlanLoadCaseDTO.class)
        );

        testPlanReportDTO.setLoadFailureCases(
                getReportContentResultArray(testPlanReportContent.getLoadFailureCases(), TestPlanLoadCaseDTO.class)
        );

        testPlanReportDTO.setErrorReportCases(
                getReportContentResultArray(testPlanReportContent.getErrorReportCases(), TestPlanFailureApiDTO.class)
        );

        testPlanReportDTO.setErrorReportScenarios(
                getReportContentResultArray(testPlanReportContent.getErrorReportScenarios(), TestPlanFailureScenarioDTO.class)
        );

        testPlanReportDTO.setUnExecuteCases(
                getReportContentResultArray(testPlanReportContent.getUnExecuteCases(), TestPlanFailureApiDTO.class)
        );

        testPlanReportDTO.setUnExecuteScenarios(
                getReportContentResultArray(testPlanReportContent.getUnExecuteScenarios(), TestPlanFailureScenarioDTO.class)
        );

        testPlanReportDTO.setUiResult(
                getReportContentResultObject(testPlanReportContent.getUiResult(), TestPlanUiResultReportDTO.class)
        );

        testPlanReportDTO.setUiAllCases(
                getReportContentResultArray(testPlanReportContent.getUiAllCases(), TestPlanUiScenarioDTO.class)
        );

        testPlanReportDTO.setUiFailureCases(
                getReportContentResultArray(testPlanReportContent.getUiFailureCases(), TestPlanUiScenarioDTO.class)
        );

        testPlanReportDTO.setId(reportId);
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportContent.getTestPlanReportId());
        testPlanReportDTO.setName(testPlanReport.getName());
        return testPlanReportDTO;
    }

    private <T> T getReportContentResultObject(String contentStr, Class<T> clazz) {
        if (StringUtils.isNotBlank(contentStr)) {
            return JSON.parseObject(contentStr, clazz);
        }
        return null;

    }

    private <T> List<T> getReportContentResultArray(String contentStr, Class<T> clazz) {
        if (StringUtils.isNotBlank(contentStr)) {
            return JSON.parseArray(contentStr, clazz);
        }
        return null;
    }


    private void generateEnvironmentInfo(TestPlanSimpleReportDTO testPlanReportDTO, String reportId) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(reportId);
        try {
            if (DiscoveryUtil.hasService(MicroServiceName.API_TEST)) {
                TestPlanEnvInfoDTO testPlanEnvInfo = planTestPlanScenarioCaseService.generateEnvironmentInfo(testPlanReport);
                BeanUtils.copyBean(testPlanReportDTO, testPlanEnvInfo);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private boolean isDynamicallyGenerateReports(TestPlanReportContentWithBLOBs testPlanReportContent) {
        return testPlanReportContent != null &&
                (StringUtils.isNotEmpty(testPlanReportContent.getPlanApiCaseReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanScenarioReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanLoadCaseReportStruct()) || StringUtils.isNotEmpty(testPlanReportContent.getPlanUiScenarioReportStruct()));
    }

    private TestPlanReportContentWithBLOBs dynamicallyGenerateReports(TestPlanReportContentWithBLOBs testPlanReportContent) {
        TestPlanReport report = testPlanReportMapper.selectByPrimaryKey(testPlanReportContent.getTestPlanReportId());
        testPlanReportContent = this.updateReport(report, testPlanReportContent);
        return testPlanReportContent;
    }

    public void createTestPlanReportContentReportIds(String testPlanReportID, Map<String, String> apiCaseReportMap, Map<String, String> scenarioReportIdMap, Map<String, String> loadCaseReportIdMap, Map<String, String> uiScenarioReportMap) {
        TestPlanReportContentWithBLOBs content = new TestPlanReportContentWithBLOBs();
        content.setId(UUID.randomUUID().toString());
        content.setTestPlanReportId(testPlanReportID);

        if (MapUtils.isNotEmpty(apiCaseReportMap)) {
            List<TestPlanFailureApiDTO> apiTestCases = planTestPlanApiCaseService.getFailureListByIds(apiCaseReportMap.keySet());
            for (TestPlanFailureApiDTO dto : apiTestCases) {
                dto.setReportId(apiCaseReportMap.get(dto.getId()));
            }
            content.setPlanApiCaseReportStruct(JSON.toJSONString(apiTestCases));
        }
        if (MapUtils.isNotEmpty(scenarioReportIdMap)) {
            List<TestPlanFailureScenarioDTO> apiTestCases =
                    planTestPlanScenarioCaseService.getFailureListByIds(scenarioReportIdMap.keySet());
            for (TestPlanFailureScenarioDTO dto : apiTestCases) {
                dto.setReportId(scenarioReportIdMap.get(dto.getId()));
            }
            content.setPlanScenarioReportStruct(JSON.toJSONString(apiTestCases));
        }
        if (MapUtils.isNotEmpty(loadCaseReportIdMap)) {
            content.setPlanLoadCaseReportStruct(JSON.toJSONString(loadCaseReportIdMap));
        }
        if (MapUtils.isNotEmpty(uiScenarioReportMap)) {
            List<TestPlanUiScenarioDTO> uiScenarios =
                    planTestPlanUiScenarioCaseService.getFailureListByIds(uiScenarioReportMap.keySet());
            for (TestPlanUiScenarioDTO dto : uiScenarios) {
                dto.setReportId(uiScenarioReportMap.get(dto.getId()));
            }
            content.setPlanUiScenarioReportStruct(JSON.toJSONString(uiScenarios));
        }
        testPlanReportContentMapper.insert(content);
    }

    public TestPlanExecuteReportDTO genTestPlanExecuteReportDTOByTestPlanReportContent(TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        Map<String, String> testPlanApiCaseIdAndReportIdMap = new LinkedHashMap<>();
        Map<String, String> testPlanScenarioIdAndReportIdMap = new LinkedHashMap<>();
        Map<String, String> testPlanUiScenarioIdAndReportIdMap = new LinkedHashMap<>();
        Map<String, String> testPlanLoadCaseIdAndReportIdMap = new LinkedHashMap<>();
        Map<String, TestPlanFailureApiDTO> apiCaseInfoDTOMap = new LinkedHashMap<>();
        Map<String, TestPlanFailureScenarioDTO> scenarioInfoDTOMap = new LinkedHashMap<>();
        Map<String, TestPlanUiScenarioDTO> uiScenarioInfoDTOMap = new LinkedHashMap<>();

        if (testPlanReportContentWithBLOBs != null) {
            if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct())) {
                List<TestPlanFailureApiDTO> apiCaseInfoDTOList = null;
                try {
                    apiCaseInfoDTOList = JSON.parseArray(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct(), TestPlanFailureApiDTO.class);
                } catch (Exception ignored) {
                }
                if (apiCaseInfoDTOList == null) {
                    try {
                        testPlanApiCaseIdAndReportIdMap = JSON.parseObject(testPlanReportContentWithBLOBs.getPlanApiCaseReportStruct(), Map.class);
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
                    scenarioInfoDTOList = JSON.parseArray(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct(), TestPlanFailureScenarioDTO.class);
                } catch (Exception ignored) {
                }
                if (scenarioInfoDTOList == null) {
                    try {
                        testPlanScenarioIdAndReportIdMap = JSON.parseObject(testPlanReportContentWithBLOBs.getPlanScenarioReportStruct(), Map.class);
                    } catch (Exception ignored) {
                    }
                } else {
                    for (TestPlanFailureScenarioDTO item : scenarioInfoDTOList) {
                        testPlanScenarioIdAndReportIdMap.put(item.getId(), item.getReportId());
                        scenarioInfoDTOMap.put(item.getId(), item);
                    }
                }
            }
            if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanUiScenarioReportStruct())) {
                List<TestPlanUiScenarioDTO> scenarioInfoDTOList = null;
                try {
                    scenarioInfoDTOList = JSON.parseArray(testPlanReportContentWithBLOBs.getPlanUiScenarioReportStruct(), TestPlanUiScenarioDTO.class);
                } catch (Exception ignored) {
                }
                if (scenarioInfoDTOList == null) {
                    try {
                        testPlanUiScenarioIdAndReportIdMap = JSON.parseObject(testPlanReportContentWithBLOBs.getPlanUiScenarioReportStruct(), Map.class);
                    } catch (Exception ignored) {
                    }
                } else {
                    for (TestPlanUiScenarioDTO item : scenarioInfoDTOList) {
                        testPlanUiScenarioIdAndReportIdMap.put(item.getId(), item.getReportId());
                        uiScenarioInfoDTOMap.put(item.getId(), item);

                    }
                }
            }
            if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getPlanLoadCaseReportStruct())) {
                try {
                    testPlanLoadCaseIdAndReportIdMap = JSON.parseObject(testPlanReportContentWithBLOBs.getPlanLoadCaseReportStruct(), Map.class);
                } catch (Exception ignore) {
                }
            }
        }
        TestPlanExecuteReportDTO returnDTO = new TestPlanExecuteReportDTO(testPlanApiCaseIdAndReportIdMap, testPlanScenarioIdAndReportIdMap, testPlanUiScenarioIdAndReportIdMap, testPlanLoadCaseIdAndReportIdMap, apiCaseInfoDTOMap, scenarioInfoDTOMap, uiScenarioInfoDTOMap);
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
                    List<TestPlanFailureApiDTO> apiTestCases = JSON.parseArray(content.getPlanApiCaseReportStruct(), TestPlanFailureApiDTO.class);
                    List<String> reportIdList = new ArrayList<>();
                    apiTestCases.forEach(item -> {
                        if (StringUtils.isNotEmpty(item.getReportId())) {
                            reportIdList.add(item.getReportId());
                        }
                    });
                    Map<String, String> reportResult = planApiDefinitionExecResultService.selectReportResultByReportIds(reportIdList);
                    String defaultStatus = ApiReportStatus.ERROR.name();
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
                    List<TestPlanFailureScenarioDTO> scenarioCases = JSON.parseArray(content.getPlanScenarioReportStruct(), TestPlanFailureScenarioDTO.class);
                    List<String> reportIdList = new ArrayList<>();
                    scenarioCases.forEach(item -> {
                        if (StringUtils.isNotEmpty(item.getReportId())) {
                            reportIdList.add(item.getReportId());
                        }
                    });
                    String defaultStatus = ApiReportStatus.ERROR.name();
                    Map<String, String> reportStatus = planApiScenarioReportService.getReportStatusByReportIds(reportIdList);

                    for (TestPlanFailureScenarioDTO dto : scenarioCases) {
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
    }
}
