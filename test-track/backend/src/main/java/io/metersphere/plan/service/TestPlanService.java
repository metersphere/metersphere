package io.metersphere.plan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.DataStatus;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.excel.constants.TestPlanTestCaseStatus;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.log.vo.track.TestPlanReference;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.job.TestPlanTestJob;
import io.metersphere.plan.request.AddTestPlanRequest;
import io.metersphere.plan.request.BatchOperateRequest;
import io.metersphere.plan.request.QueryTestPlanRequest;
import io.metersphere.plan.request.ScheduleInfoRequest;
import io.metersphere.plan.request.api.ApiPlanReportRequest;
import io.metersphere.plan.request.api.RunScenarioRequest;
import io.metersphere.plan.request.api.SchedulePlanScenarioExecuteRequest;
import io.metersphere.plan.request.api.TestPlanRunRequest;
import io.metersphere.plan.request.function.PlanCaseRelevanceRequest;
import io.metersphere.plan.request.function.QueryTestPlanCaseRequest;
import io.metersphere.plan.request.performance.LoadPlanReportDTO;
import io.metersphere.plan.request.ui.RunUiScenarioRequest;
import io.metersphere.plan.request.ui.TestPlanUiExecuteReportDTO;
import io.metersphere.plan.request.ui.UiPlanReportRequest;
import io.metersphere.plan.service.execute.TestPlanExecuteService;
import io.metersphere.plan.service.remote.api.PlanApiAutomationService;
import io.metersphere.plan.service.remote.api.PlanTestPlanApiCaseService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.plan.service.remote.performance.PlanTestPlanLoadCaseService;
import io.metersphere.plan.service.remote.ui.PlanTestPlanUiScenarioCaseService;
import io.metersphere.plan.service.remote.ui.PlanUiAutomationService;
import io.metersphere.plan.utils.TestPlanReportUtil;
import io.metersphere.plan.utils.TestPlanRequestUtil;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ScheduleRequest;
import io.metersphere.service.*;
import io.metersphere.utils.DiscoveryUtil;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xpack.track.dto.IssuesDao;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    ExtTestPlanMapper extTestPlanMapper;
    @Resource
    BaseScheduleService baseScheduleService;
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    BaseUserService baseUserService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Lazy
    @Resource
    TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    ProjectMapper projectMapper;
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    BaseCustomFieldService baseCustomFieldService;
    @Resource
    private PlanTestPlanApiCaseService planTestPlanApiCaseService;
    @Resource
    private PlanTestPlanScenarioCaseService planTestPlanScenarioCaseService;
    @Resource
    private PlanTestPlanLoadCaseService planTestPlanLoadCaseService;
    @Resource
    private PlanTestPlanUiScenarioCaseService planTestPlanUiScenarioCaseService;
    @Resource
    private PlanApiAutomationService planApiAutomationService;
    @Resource
    private PlanUiAutomationService planUiAutomationService;
    @Resource
    private TestCaseTestMapper testCaseTestMapper;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Lazy
    @Resource
    private IssuesService issuesService;

    @Resource
    private TestPlanPrincipalService testPlanPrincipalService;
    @Resource
    private TestPlanPrincipalMapper testPlanPrincipalMapper;
    @Resource
    private TestPlanFollowService testPlanFollowService;
    @Resource
    private TestPlanFollowMapper testPlanFollowMapper;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private TestPlanExecutionQueueService testPlanExecutionQueueService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private ApiPoolDebugService apiPoolDebugService;
    @Resource
    private TestPlanExecuteService testPlanExecuteService;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;

    private static void buildCaseIdList
            (List<TestCaseTest> list, List<String> apiCaseIds, List<String> scenarioIds, List<String> performanceIds, List<String> uiScenarioIds) {
        for (TestCaseTest l : list) {
            if (StringUtils.equals(l.getTestType(), TestCaseTestStatus.performance.name())) {
                performanceIds.add(l.getTestId());
            }
            if (StringUtils.equals(l.getTestType(), TestCaseTestStatus.testcase.name())) {
                apiCaseIds.add(l.getTestId());
            }
            if (StringUtils.equals(l.getTestType(), TestCaseTestStatus.automation.name())) {
                scenarioIds.add(l.getTestId());
            }
            if (StringUtils.equals(l.getTestType(), TestCaseTestStatus.uiAutomation.name())) {
                uiScenarioIds.add(l.getTestId());
            }
        }
    }

    //获取下次执行时间（getFireTimeAfter，也可以下下次...）
    private static long getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return 0;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? 0 : time1.getTime();
    }

    public TestPlan addTestPlan(AddTestPlanRequest testPlan) {
        if (getTestPlanByName(testPlan.getName()).size() > 0) {
            MSException.throwException(Translator.get("plan_name_already_exists"));
        }
        testPlan.setStatus(TestPlanStatus.Prepare.name());
        testPlan.setCreateTime(System.currentTimeMillis());
        testPlan.setUpdateTime(System.currentTimeMillis());
        testPlan.setCreator(SessionUtils.getUser().getId());

        String planId = testPlan.getId();
        List<String> principals = testPlan.getPrincipals();
        if (!CollectionUtils.isEmpty(principals)) {
            for (String principal : principals) {
                TestPlanPrincipal testPlanPrincipal = new TestPlanPrincipal();
                testPlanPrincipal.setTestPlanId(planId);
                testPlanPrincipal.setPrincipalId(principal);
                testPlanPrincipalService.insert(testPlanPrincipal);
            }
        }

        List<String> follows = testPlan.getFollows();
        if (!CollectionUtils.isEmpty(follows)) {
            for (String follow : follows) {
                TestPlanFollow testPlanFollow = new TestPlanFollow();
                testPlanFollow.setTestPlanId(planId);
                testPlanFollow.setFollowId(follow);
                testPlanFollowService.insert(testPlanFollow);
            }
        }
        if (StringUtils.isBlank(testPlan.getProjectId())) {
            testPlan.setProjectId(SessionUtils.getCurrentProjectId());
        }
        testPlanMapper.insert(testPlan);

        return testPlan;
    }

    public List<TestPlan> getTestPlanByName(String name) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria()
                .andProjectIdEqualTo(SessionUtils.getCurrentProjectId())
                .andNameEqualTo(name);
        return testPlanMapper.selectByExample(example);
    }

    public TestPlan getTestPlan(String testPlanId) {
        return Optional.ofNullable(testPlanMapper.selectByPrimaryKey(testPlanId)).orElse(new TestPlanWithBLOBs());
    }

    public TestPlanWithBLOBs get(String testPlanId) {
        return testPlanMapper.selectByPrimaryKey(testPlanId);
    }

    public TestPlanWithBLOBs getTransferPlan(String testPlanId) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        if (testPlan != null) {
            testPlan.setStage(StatusReference.statusMap.containsKey(testPlan.getStage()) ? StatusReference.statusMap.get(testPlan.getStage()) : testPlan.getStage());
            testPlan.setStatus(StatusReference.statusMap.containsKey(testPlan.getStatus()) ? StatusReference.statusMap.get(testPlan.getStatus()) : testPlan.getStatus());
        }
        return testPlan;
    }

    public TestPlan editTestPlanWithRequest(AddTestPlanRequest request) {
        List<String> principals = request.getPrincipals();
        if (!CollectionUtils.isEmpty(principals)) {
            if (StringUtils.isNotBlank(request.getId())) {
                testPlanPrincipalService.deleteTestPlanPrincipalByPlanId(request.getId());
                for (String principal : principals) {
                    TestPlanPrincipal testPlanPrincipal = new TestPlanPrincipal();
                    testPlanPrincipal.setTestPlanId(request.getId());
                    testPlanPrincipal.setPrincipalId(principal);
                    testPlanPrincipalService.insert(testPlanPrincipal);
                }
            }
        }
        return this.editTestPlan(request);
    }

    public void editTestFollows(String planId, List<String> follows) {
        if (StringUtils.isNotBlank(planId)) {
            testPlanFollowService.deleteTestPlanFollowByPlanId(planId);
            if (!CollectionUtils.isEmpty(follows)) {
                for (String follow : follows) {
                    TestPlanFollow testPlanFollow = new TestPlanFollow();
                    testPlanFollow.setTestPlanId(planId);
                    testPlanFollow.setFollowId(follow);
                    testPlanFollowService.insert(testPlanFollow);
                }
            }
        }
    }

    public TestPlan editTestPlan(TestPlanWithBLOBs testPlan) {
        checkTestPlanExist(testPlan);
        TestPlan res = testPlanMapper.selectByPrimaryKey(testPlan.getId()); //  先查一次库
        testPlan.setUpdateTime(System.currentTimeMillis());
        if (!res.getStatus().equals(testPlan.getStatus())) {    //  若有改变才更新时间
            if (TestPlanStatus.Underway.name().equals(testPlan.getStatus())) {
                if (res.getStatus().equals(TestPlanStatus.Prepare.name())) {
                    testPlan.setActualStartTime(System.currentTimeMillis());
                }   //  未开始->进行中，写入实际开始时间
                else if (res.getStatus().equals(TestPlanStatus.Completed.name())) {
                    testPlan.setActualEndTime(null);
                }   //  已完成->进行中，结束时间置空
            } else if (!res.getStatus().equals(TestPlanStatus.Prepare.name()) &&
                    TestPlanStatus.Prepare.name().equals(testPlan.getStatus())) {
                testPlan.setActualStartTime(null);
                testPlan.setActualEndTime(null);
            }   //  非未开始->未开始，时间都置空
            else if (TestPlanStatus.Completed.name().equals(testPlan.getStatus()) &&
                    !TestPlanStatus.Completed.name().equals(res.getStatus())) {
                //已完成，写入实际完成时间
                testPlan.setActualEndTime(System.currentTimeMillis());
            } else if (!res.getStatus().equals(TestPlanStatus.Finished.name()) &&
                    TestPlanStatus.Finished.name().equals(testPlan.getStatus())) {
                testPlan.setActualEndTime(System.currentTimeMillis());
            }   //  非已结束->已结束，更新结束时间
        }

        // 如果状态是未开始，设置时间为null
        if (StringUtils.isNotBlank(testPlan.getStatus()) && testPlan.getStatus().equals(TestPlanStatus.Prepare.name())) {
            testPlan.setActualStartTime(null);
            testPlan.setActualEndTime(null);
        }

        // 如果当前状态已完成，没有结束时间，设置结束时间
        if (StringUtils.equalsAnyIgnoreCase(testPlan.getStatus(), TestPlanStatus.Finished.name(), TestPlanStatus.Completed.name())
                && res.getActualEndTime() == null) {
            testPlan.setActualEndTime(System.currentTimeMillis());
        }

        // 如果当前状态不是已完成，设置结束时间为null
        if (!StringUtils.equalsAnyIgnoreCase(testPlan.getStatus(), TestPlanStatus.Finished.name(), TestPlanStatus.Completed.name())
                && res.getActualEndTime() != null) {
            testPlan.setActualEndTime(null);
        }

        // 如果当前状态不是未开始，并且没有开始时间，设置开始时间
        if (!StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                && res.getActualStartTime() == null) {
            testPlan.setActualStartTime(System.currentTimeMillis());
        }
        if (testPlan.getName() == null) {
            //  若是点击该测试计划，则仅更新了updateTime，其它字段全为null，使用updateByPrimaryKeySelective
            testPlanMapper.updateByPrimaryKeySelective(testPlan);
        } else {
            //  有修改字段的调用，为保证将某些时间置null的情况，使用updateByPrimaryKey
            testPlanMapper.updateByPrimaryKeyWithBLOBs(testPlan); //  更新
        }
        return testPlanMapper.selectByPrimaryKey(testPlan.getId());
    }

    private void checkTestPlanExist(TestPlan testPlan) {
        if (testPlan.getName() != null) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria()
                    .andNameEqualTo(testPlan.getName())
                    .andProjectIdEqualTo(testPlan.getProjectId())
                    .andIdNotEqualTo(testPlan.getId());
            if (testPlanMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("plan_name_already_exists"));
            }
        }
    }

    public int deleteTestPlan(String planId) {

        if (testPlanReportService.hasRunningReport(planId)) {
            MSException.throwException(Translator.get("test_plan_delete_exec_error"));
        }

        // 发送删除通知
        try {
            kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC, objectMapper.writeValueAsString(List.of(planId)));
        } catch (JsonProcessingException e) {
            LogUtil.error("send msg to TEST_PLAN_DELETED_TOPIC error", e);
        }

        testPlanPrincipalService.deleteTestPlanPrincipalByPlanId(planId);
        testPlanFollowService.deleteTestPlanFollowByPlanId(planId);
        deleteTestCaseByPlanId(planId);

        testPlanReportService.deleteByPlanId(planId);

        //删除定时任务
        baseScheduleService.deleteByResourceId(planId, ScheduleGroup.TEST_PLAN_TEST.name());

        return testPlanMapper.deleteByPrimaryKey(planId);
    }

    public int deleteTestPlans(List<String> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return 0;
        }

        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andIdIn(planIds);
        int deletedSize = testPlanMapper.deleteByExample(testPlanExample);

        testPlanPrincipalService.deletePlanPrincipalByPlanIds(planIds);
        testPlanFollowService.deletePlanFollowByPlanIds(planIds);

        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdIn(planIds);
        testPlanTestCaseMapper.deleteByExample(testPlanTestCaseExample);


        testPlanReportService.deleteByPlanIds(planIds);
        //删除定时任务
        baseScheduleService.deleteByResourceIds(planIds, ScheduleGroup.TEST_PLAN_TEST.name());

        try {
            kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC, objectMapper.writeValueAsString(planIds));
        } catch (Exception e) {
            LogUtil.error("send msg to TEST_PLAN_DELETED_TOPIC error", e);
        }
        return deletedSize;
    }

    public void deleteTestCaseByPlanId(String testPlanId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(testPlanId);
        testPlanTestCaseMapper.deleteByExample(testPlanTestCaseExample);
    }

    public void calcTestPlanRate(List<TestPlanDTOWithMetric> testPlans) {
        testPlans.forEach(this::calcTestPlanRate);
    }

    public void calcTestPlanRate(TestPlanDTOWithMetric testPlan) {
        testPlan.setTested(0);
        testPlan.setPassed(0);
        testPlan.setTotal(0);
        List<CountMapDTO> statusCountMap = extTestPlanTestCaseMapper.getExecResultMapByPlanId(testPlan.getId());
        Integer functionalExecTotal = 0;
        for (CountMapDTO item : statusCountMap) {
            functionalExecTotal += item.getValue();
            if (!StringUtils.equals(item.getKey(), TestPlanTestCaseStatus.Prepare.name())
                    && !StringUtils.equals(item.getKey(), TestPlanTestCaseStatus.Underway.name())) {
                testPlan.setTested(testPlan.getTested() + item.getValue());
                if (StringUtils.equals(item.getKey(), TestPlanTestCaseStatus.Pass.name())) {
                    testPlan.setPassed(testPlan.getPassed() + item.getValue());
                }
            }
        }
        testPlan.setTotal(testPlan.getTotal() + functionalExecTotal);
        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();
        if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
            calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanApiCaseService::getExecResultByPlanId);
            calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanScenarioCaseService::getExecResultByPlanId);
        }
        if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
            calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanLoadCaseService::getExecResultByPlanId);
        }
        if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
            calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanUiScenarioCaseService::getExecResultByPlanId);
        }
        testPlan.setPassRate(MathUtils.getPercentWithDecimal(testPlan.getTested() == 0 ? 0 : testPlan.getPassed() * 1.0 / testPlan.getTotal()));
        testPlan.setTestRate(MathUtils.getPercentWithDecimal(testPlan.getTotal() == 0 ? 0 : testPlan.getTested() * 1.0 / testPlan.getTotal()));
    }

    public List<TestPlanDTOWithMetric> calcTestPlanRateByIdList(List<String> testPlanIdList) {
        List<TestPlanDTOWithMetric> returnList = new ArrayList<>();
        DecimalFormat rateFormat = new DecimalFormat("#0.00");
        rateFormat.setMinimumFractionDigits(2);
        rateFormat.setMaximumFractionDigits(2);

        for (String testPlanId : testPlanIdList) {
            TestPlanDTOWithMetric returnMetric = new TestPlanDTOWithMetric();
            returnMetric.setId(testPlanId);
            returnMetric.setTested(0);
            returnMetric.setPassed(0);
            returnMetric.setTotal(0);

            Integer functionalExecTotal = 0;

            List<CountMapDTO> statusCountMap = extTestPlanTestCaseMapper.getExecResultMapByPlanId(testPlanId);
            for (CountMapDTO item : statusCountMap) {
                functionalExecTotal += item.getValue();
                if (!StringUtils.equals(item.getKey(), TestPlanTestCaseStatus.Prepare.name())
                        && !StringUtils.equals(item.getKey(), TestPlanTestCaseStatus.Underway.name())) {
                    returnMetric.setTested(returnMetric.getTested() + item.getValue());
                    if (StringUtils.equals(item.getKey(), TestPlanTestCaseStatus.Pass.name())) {
                        returnMetric.setPassed(returnMetric.getPassed() + item.getValue());
                    }
                }
            }
            returnMetric.setTotal(functionalExecTotal);

            Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();
            if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
                calcExecResultStatus(testPlanId, returnMetric, planTestPlanApiCaseService::getExecResultByPlanId);
                calcExecResultStatus(testPlanId, returnMetric, planTestPlanScenarioCaseService::getExecResultByPlanId);
            }
            if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
                calcExecResultStatus(testPlanId, returnMetric, planTestPlanLoadCaseService::getExecResultByPlanId);
            }
            if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
                calcExecResultStatus(testPlanId, returnMetric, planTestPlanUiScenarioCaseService::getExecResultByPlanId);
            }

            if (returnMetric.getTotal() > 0) {
                int passCount = returnMetric.getPassed();
                double passRate = Double.parseDouble(rateFormat.format((double) passCount * 100 / (double) returnMetric.getTotal()));
                if (passRate == 100 && passCount < returnMetric.getTotal()) {
                    returnMetric.setPassRate(0.9999);
                } else {
                    returnMetric.setPassRate(passRate);
                }

                int testCount = returnMetric.getTested();
                double testRate = Double.parseDouble(rateFormat.format((double) testCount * 100 / (double) returnMetric.getTotal()));
                if (testRate == 100 && testCount < returnMetric.getTotal()) {
                    returnMetric.setTestRate(0.9999);
                } else {
                    returnMetric.setTestRate(testRate);
                }
            } else {
                returnMetric.setPassRate(0.00);
                returnMetric.setTestRate(0.00);
            }
            returnList.add(returnMetric);
        }
        return returnList;
    }

    /**
     * 出现异常不回滚，继续计算其他
     *
     * @param planId
     * @return
     */
    void calcExecResultStatus(String planId, TestPlanDTOWithMetric testPlan, Function<String, List<String>> getResultFunc) {
        try {
            List<String> execResults = getResultFunc.apply(planId);
            execResults.forEach(item -> {
                if (StringUtils.isNotBlank(item) && !StringUtils.equalsIgnoreCase(DataStatus.UNEXECUTE.getValue(), item) && !StringUtils.equalsIgnoreCase(DataStatus.PREPARE.getValue(), item)) {
                    testPlan.setTested(testPlan.getTested() + 1);
                    if (StringUtils.equalsIgnoreCase(item, APITestStatus.Success.name())) {
                        testPlan.setPassed(testPlan.getPassed() + 1);
                    }
                }
            });
            testPlan.setTotal(testPlan.getTotal() + execResults.size());
        } catch (MSException ignore) {
        }
    }

    public List<TestPlanDTOWithMetric> listTestPlan(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        if (StringUtils.isNotBlank(request.getProjectId())) {
            request.setProjectId(request.getProjectId());
        }
        List<TestPlanDTOWithMetric> testPlanList = extTestPlanMapper.list(request);

        //统计测试计划的测试用例数
        List<String> testPlanIdList = testPlanList.stream().map(TestPlanDTOWithMetric::getId).collect(Collectors.toList());
        Map<String, ParamsDTO> planTestCaseCountMap = extTestPlanMapper.testPlanTestCaseCount(testPlanIdList);
        Map<String, ParamsDTO> planApiCaseMap = extTestPlanMapper.testPlanApiCaseCount(testPlanIdList);
        Map<String, ParamsDTO> planApiScenarioMap = extTestPlanMapper.testPlanApiScenarioCount(testPlanIdList);
        Map<String, ParamsDTO> planUiScenarioMap = extTestPlanMapper.testPlanUiScenarioCount(testPlanIdList);
        Map<String, ParamsDTO> planLoadCaseMap = extTestPlanMapper.testPlanLoadCaseCount(testPlanIdList);
        for (TestPlanDTOWithMetric testPlanMetric : testPlanList) {
            testPlanMetric.setTestPlanTestCaseCount(planTestCaseCountMap.get(testPlanMetric.getId()) == null ? 0 : Integer.parseInt(planTestCaseCountMap.get(testPlanMetric.getId()).getValue() == null ? "0" : planTestCaseCountMap.get(testPlanMetric.getId()).getValue()));
            testPlanMetric.setTestPlanApiCaseCount(planApiCaseMap.get(testPlanMetric.getId()) == null ? 0 : Integer.parseInt(planApiCaseMap.get(testPlanMetric.getId()).getValue() == null ? "0" : planApiCaseMap.get(testPlanMetric.getId()).getValue()));
            testPlanMetric.setTestPlanApiScenarioCount(planApiScenarioMap.get(testPlanMetric.getId()) == null ? 0 : Integer.parseInt(planApiScenarioMap.get(testPlanMetric.getId()).getValue() == null ? "0" : planApiScenarioMap.get(testPlanMetric.getId()).getValue()));
            testPlanMetric.setTestPlanUiScenarioCount(planUiScenarioMap.get(testPlanMetric.getId()) == null ? 0 : Integer.parseInt(planUiScenarioMap.get(testPlanMetric.getId()).getValue() == null ? "0" : planUiScenarioMap.get(testPlanMetric.getId()).getValue()));
            testPlanMetric.setTestPlanLoadCaseCount(planLoadCaseMap.get(testPlanMetric.getId()) == null ? 0 : Integer.parseInt(planLoadCaseMap.get(testPlanMetric.getId()).getValue() == null ? "0" : planLoadCaseMap.get(testPlanMetric.getId()).getValue()));
        }

        if (CollectionUtils.isNotEmpty(testPlanList)) {
            List<String> changeToFinishedIds = new ArrayList<>();
            //检查定时任务的设置
            List<String> idList = testPlanList.stream().map(TestPlan::getId).collect(Collectors.toList());
            List<Schedule> scheduleByResourceIds = baseScheduleService.getScheduleByResourceIds(idList, ScheduleGroup.TEST_PLAN_TEST.name());
            Map<String, Schedule> scheduleMap = scheduleByResourceIds.stream().collect(Collectors.toMap(Schedule::getResourceId, Schedule -> Schedule));
            testPlanList.forEach(item -> {
                if (StringUtils.isNotBlank(item.getScheduleId())) {
                    if (item.isScheduleOpen()) {
                        item.setScheduleStatus(ScheduleStatus.OPEN.name());
                        Schedule schedule = scheduleMap.get(item.getId());
                        item.setScheduleCorn(schedule.getValue());
                        item.setScheduleExecuteTime(getNextTriggerTime(schedule.getValue()));
                    } else {
                        item.setScheduleStatus(ScheduleStatus.SHUT.name());
                    }
                } else {
                    item.setScheduleStatus(ScheduleStatus.NOTSET.name());
                }

                // 关注人这里查出来。 是因为编辑的时候需要有这个字段。
                List<User> planPrincipal = this.getPlanPrincipal(item.getId());
                item.setPrincipalUsers(planPrincipal);

                // 还没有结束的计划，如果设置了结束时间，并且已经到了结束时间，则将状态改为已结束
                if (!StringUtils.equalsAny(item.getStatus(), TestPlanStatus.Finished.name(), TestPlanStatus.Archived.name())
                        && item.getPlannedEndTime() != null && System.currentTimeMillis() > item.getPlannedEndTime()) {
                    item.setStatus(TestPlanStatus.Finished.name());
                    changeToFinishedIds.add(item.getId());
                }
            });

            if (CollectionUtils.isNotEmpty(changeToFinishedIds)) {
                // 使用代理对象，避免 @Async 失效
                testPlanService.changeToFinished(changeToFinishedIds);
            }
        }
        return testPlanList;
    }

    /**
     * 异步将测试计划的状态置为已结束
     *
     * @param changeToFinishedIds
     */
    @Async
    protected void changeToFinished(List<String> changeToFinishedIds) {
        if (CollectionUtils.isEmpty(changeToFinishedIds)) {
            return;
        }
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andIdIn(changeToFinishedIds);
        List<TestPlanWithBLOBs> testPlans = testPlanMapper.selectByExampleWithBLOBs(example);
        testPlans.forEach(item -> {
            item.setStatus(TestPlanStatus.Finished.name());
            editTestPlan(item);
        });
    }

    public List<TestPlanDTOWithMetric> selectTestPlanMetricById(List<String> idList) {
        List<TestPlanDTOWithMetric> testPlanMetricList = this.calcTestPlanRateByIdList(idList);
        for (TestPlanDTOWithMetric testPlanMetric : testPlanMetricList) {
            List<User> followUsers = this.getPlanFollow(testPlanMetric.getId());
            testPlanMetric.setFollowUsers(followUsers);
        }
        return testPlanMetricList;
    }

    public void checkTestPlanStatusWhenExecuteOver(String testPlanId) {
        TestPlan testPlan = this.testPlanMapper.selectByPrimaryKey(testPlanId);
        if (testPlan != null && !StringUtils.equalsIgnoreCase(testPlan.getStatus(), "Completed")) {
            this.checkTestPlanStatus(testPlanId);
        }
    }

    public void checkTestPlanStatus(String testPlanId) { //  检查执行结果，自动更新计划状态
        //如果目前存在执行中的报告，那么不更新计划状态
        if (extTestPlanMapper.countExecutingReportCount(testPlanId) > 0) {
            return;
        }
        List<String> statusList = extTestPlanTestCaseMapper.getExecResultByPlanId(testPlanId);

        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();

        if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
            statusList.addAll(planTestPlanApiCaseService.getExecResultByPlanId(testPlanId));
            statusList.addAll(planTestPlanScenarioCaseService.getExecResultByPlanId(testPlanId));
        }
        if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
            statusList.addAll(planTestPlanLoadCaseService.getExecResultByPlanId(testPlanId));
        }
        if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
            statusList.addAll(planTestPlanUiScenarioCaseService.getExecResultByPlanId(testPlanId));
        }

        TestPlanWithBLOBs testPlanWithBLOBs = testPlanMapper.selectByPrimaryKey(testPlanId);
        //如果测试计划是已归档状态，不处理
        if (testPlanWithBLOBs.getStatus().equals(TestPlanStatus.Archived.name())) {
            return;
        }
        testPlanWithBLOBs.setId(testPlanId);
        if (statusList.size() == 0) { //  原先status不是prepare, 但删除所有关联用例的情况
            testPlanWithBLOBs.setStatus(TestPlanStatus.Prepare.name());
            editTestPlan(testPlanWithBLOBs);
            return;
        }
        int passNum = 0, prepareNum = 0, failNum = 0;
        for (String res : statusList) {
            if (StringUtils.equals(res, TestPlanTestCaseStatus.Pass.name())
                    || StringUtils.equalsIgnoreCase(res, "success")) {
                passNum++;
            } else if (res == null || StringUtils.equals(TestPlanStatus.Prepare.name(), res)) {
                prepareNum++;
            } else {
                failNum++;
            }
        }

        // 到结束时间了，不管用例的状态都改为已结束
        if (testPlanWithBLOBs.getPlannedEndTime() != null && System.currentTimeMillis() > testPlanWithBLOBs.getPlannedEndTime()) {
            testPlanWithBLOBs.setStatus(TestPlanStatus.Finished.name());
            editTestPlan(testPlanWithBLOBs);
        } else if (prepareNum == 0 && passNum + failNum == statusList.size()) {
            // 如果全部都执行完了改为已完成，如果已完成之后到结束时间了，走上面逻辑改成已结束
            testPlanWithBLOBs.setStatus(TestPlanStatus.Completed.name());
            this.editTestPlan(testPlanWithBLOBs);
        } else if (prepareNum != 0) {
            //  用例没有执行完，并且结束时间没到，改为进行中
            testPlanWithBLOBs.setStatus(TestPlanStatus.Underway.name());
            editTestPlan(testPlanWithBLOBs);
        }
    }

    public List<TestPlanDTOWithMetric> listTestPlanByProject(QueryTestPlanRequest request) {
        List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.list(request);
        return testPlans;
    }

    public void testPlanRelevance(PlanCaseRelevanceRequest request) {
        LinkedHashMap<String, String> userMap;
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
        ServiceUtils.buildCombineTagsToSupportMultiple(request.getRequest());
        boolean isSelectAll = request.getRequest() != null && request.getRequest().isSelectAll();
        if (isSelectAll) {
            List<OrderRequest> orders = request.getRequest().getOrders();
            if (CollectionUtils.isNotEmpty(orders)) {
                orders.forEach(order -> {
                    order.setPrefix("test_case");
                });
            } else {
                List<OrderRequest> defaultOrders = getDefaultOrders();
                request.getRequest().setOrders(defaultOrders);
            }
            List<TestCase> maintainerMap;
            if (BooleanUtils.isTrue(testPlan.getRepeatCase())) {
                maintainerMap = extTestCaseMapper.getMaintainerMapForPlanRepeat(request.getRequest());
            } else {
                maintainerMap = extTestCaseMapper.getMaintainerMap(request.getRequest());
            }
            userMap = maintainerMap.stream()
                    .collect(LinkedHashMap::new, (m, v) -> m.put(v.getId(), v.getMaintainer()), LinkedHashMap::putAll);
        } else {
            TestCaseExample testCaseExample = new TestCaseExample();
            testCaseExample.createCriteria().andIdIn(request.getIds());
            List<TestCase> testCaseList = testCaseMapper.selectByExample(testCaseExample);
            userMap = testCaseList.stream()
                    .collect(LinkedHashMap::new, (m, v) -> m.put(v.getId(), v.getMaintainer()), LinkedHashMap::putAll);
        }

        Set<String> projectMemberSet = baseUserService.getProjectMemberOption(testPlan.getProjectId())
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanTestCaseMapper batchMapper = sqlSession.getMapper(TestPlanTestCaseMapper.class);

        List<String> testCaseIds = new ArrayList<>(userMap.keySet());

        if (testCaseIds.isEmpty()) {
            return;
        }

        // 保持关联顺序
        if (!isSelectAll) {
            testCaseIds = request.getIds();
        }

        // 尽量保持与用例顺序一致
        Collections.reverse(testCaseIds);

        AtomicReference<Long> nextOrder = new AtomicReference<>(ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanTestCaseMapper::getLastOrder));
        try {
            SubListUtil.dealForSubList(testCaseIds, 1000, (subList) -> {
                for (Object caseId : subList) {
                    TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
                    testPlanTestCase.setId(UUID.randomUUID().toString());
                    testPlanTestCase.setCreateUser(SessionUtils.getUserId());
                    String maintainer = userMap.get(caseId);
                    if (StringUtils.isBlank(maintainer) || !projectMemberSet.contains(maintainer)) {
                        maintainer = SessionUtils.getUserId();
                    }
                    testPlanTestCase.setExecutor(maintainer);
                    testPlanTestCase.setCaseId(caseId.toString());
                    testPlanTestCase.setCreateTime(System.currentTimeMillis());
                    testPlanTestCase.setUpdateTime(System.currentTimeMillis());
                    testPlanTestCase.setPlanId(request.getPlanId());
                    testPlanTestCase.setStatus(TestPlanStatus.Prepare.name());
                    testPlanTestCase.setIsDel(false);
                    testPlanTestCase.setOrder(nextOrder.get());
                    nextOrder.updateAndGet(v -> v + ServiceUtils.ORDER_STEP);
                    batchMapper.insert(testPlanTestCase);
                }
                sqlSession.flushStatements();
                caseTestRelevance(request, subList);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public void caseTestRelevance(PlanCaseRelevanceRequest request, List<String> testCaseIds) {
        //同步添加关联的接口和测试用例
        if (!request.getChecked()) {
            return;
        }
        if (CollectionUtils.isEmpty(testCaseIds)) {
            return;
        }
        List<TestCaseTest> list;
        TestCaseTestExample example = new TestCaseTestExample();
        example.createCriteria().andTestCaseIdIn(testCaseIds);
        list = testCaseTestMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> apiCaseIds = new ArrayList<>();
        List<String> scenarioIds = new ArrayList<>();
        List<String> performanceIds = new ArrayList<>();
        List<String> uiScenarioIds = new ArrayList<>();
        buildCaseIdList(list, apiCaseIds, scenarioIds, performanceIds, uiScenarioIds);
        startRelevance(request, apiCaseIds, scenarioIds, performanceIds, uiScenarioIds);
    }

    private void startRelevance(PlanCaseRelevanceRequest
                                        request, List<String> apiCaseIds, List<String> scenarioIds, List<String> performanceIds, List<String> uiScenarioIds) {
        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();
        if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
            if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                planTestPlanApiCaseService.relevanceByTestIds(apiCaseIds, request.getPlanId());
            }
            if (CollectionUtils.isNotEmpty(scenarioIds)) {
                planTestPlanScenarioCaseService.relevanceByTestIds(scenarioIds, request.getPlanId());
            }
        }
        if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST) && CollectionUtils.isNotEmpty(performanceIds)) {
            planTestPlanLoadCaseService.relevanceByTestIds(performanceIds, request.getPlanId());
        }
        if (serviceIdSet.contains(MicroServiceName.UI_TEST) && CollectionUtils.isNotEmpty(uiScenarioIds)) {
            planTestPlanUiScenarioCaseService.relevanceByTestIds(uiScenarioIds, request.getPlanId());
        }
    }

    private List<OrderRequest> getDefaultOrders() {
        List<OrderRequest> orderRequests = new ArrayList<>();
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setPrefix("test_case");
        orderRequest.setName("order");
        orderRequest.setType("desc");
        orderRequests.add(orderRequest);
        return orderRequests;
    }

    public List<TestPlan> recentTestPlans(String projectId) {
        return extTestPlanMapper.listRecent(SessionUtils.getUserId(), projectId);
    }

    public List<TestPlan> listTestAllPlan(QueryTestPlanRequest request) {
        String projectId = request.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            return new ArrayList<>();
        }

        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return testPlanMapper.selectByExample(example);
    }

    public List<TestPlanDTO> planListAll(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        if (StringUtils.isNotBlank(request.getProjectId())) {
            request.setProjectId(request.getProjectId());
        }
        List<TestPlanDTO> testPlanDTOS = extTestPlanMapper.planListAll(request);
        return testPlanDTOS;
    }

    public List<TestPlanCaseDTO> listTestCaseByPlanId(String planId) {
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        request.setProjectId(testPlanMapper.selectByPrimaryKey(planId).getProjectId());
        return testPlanTestCaseService.list(request);
    }

    public List<TestPlan> getTestPlanByIds(List<String> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return new ArrayList<>();
        }
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andIdIn(planIds);
        return testPlanMapper.selectByExample(example);
    }

    public void editTestPlanStatus(String planId) {
        TestPlanWithBLOBs testPlan = new TestPlanWithBLOBs();
        testPlan.setId(planId);
        String status = calcTestPlanStatus(planId);
        testPlan.setStatus(status);
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    private String calcTestPlanStatus(String planId) {
        // test-plan-functional-case status
        List<String> funcStatusList = extTestPlanTestCaseMapper.getStatusByPlanId(planId);
        for (String funcStatus : funcStatusList) {
            if (StringUtils.equals(funcStatus, TestPlanTestCaseStatus.Prepare.name())
                    || StringUtils.equals(funcStatus, TestPlanTestCaseStatus.Underway.name())) {
                return TestPlanStatus.Underway.name();
            }
        }

        // test-plan-api-case status
        List<String> apiStatusList = planTestPlanApiCaseService.getStatusByTestPlanId(planId);
        for (String apiStatus : apiStatusList) {
            if (apiStatus == null) {
                return TestPlanStatus.Underway.name();
            }
        }

        // test-plan-scenario-case status
        List<String> scenarioStatusList = planTestPlanScenarioCaseService.getExecResultByPlanId(planId);
        for (String scenarioStatus : scenarioStatusList) {
            if (scenarioStatus == null) {
                return TestPlanStatus.Underway.name();
            }
        }

        // test-plan-load-case status
        List<String> loadStatusList = planTestPlanLoadCaseService.getStatusByTestPlanId(planId);
        for (String loadStatus : loadStatusList) {
            if (loadStatus == null) {
                return TestPlanStatus.Underway.name();
            }
        }

        return TestPlanStatus.Completed.name();
    }

    public String getProjectNameByPlanId(String testPlanId) {
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(testPlanId);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);

        List<Project> projects = projectMapper.selectByExample(projectExample);
        StringBuilder stringBuilder = new StringBuilder();
        String projectName = StringUtils.EMPTY;

        if (projects.size() > 0) {
            for (Project project : projects) {
                stringBuilder.append(project.getName()).append("、");
            }
            if (StringUtils.isNotBlank(stringBuilder)) {
                projectName = stringBuilder.substring(0, stringBuilder.length() - 1);
            }
        }

        return projectName;
    }

    public List<MsExecResponseDTO> scenarioRunModeConfig(SchedulePlanScenarioExecuteRequest
                                                                 planScenarioExecuteRequest) {
        Map<String, Map<String, String>> testPlanScenarioIdMap = planScenarioExecuteRequest.getTestPlanScenarioIDMap();
        List<MsExecResponseDTO> list = new LinkedList<>();
        for (Map.Entry<String, Map<String, String>> entry : testPlanScenarioIdMap.entrySet()) {
            Map<String, String> scenarioMap = entry.getValue();

            RunScenarioRequest request = new RunScenarioRequest();
            request.setReportId(planScenarioExecuteRequest.getReportId());
            request.setTestPlanId(entry.getKey());
            request.setEnvironmentId(planScenarioExecuteRequest.getEnvironmentId());
            request.setTriggerMode(planScenarioExecuteRequest.getTriggerMode());
            request.setExecuteType(planScenarioExecuteRequest.getExecuteType());
            request.setRunMode(planScenarioExecuteRequest.getRunMode());
            request.setReportUserID(planScenarioExecuteRequest.getReportUserID());
            request.setConfig(planScenarioExecuteRequest.getConfig());
            request.setTestPlanScheduleJob(true);
            request.setTestPlanReportId(planScenarioExecuteRequest.getTestPlanReportId());
            request.setId(UUID.randomUUID().toString());
            request.setProjectId(planScenarioExecuteRequest.getProjectId());
            request.setRequestOriginator("TEST_PLAN");
            if ("api".equalsIgnoreCase(planScenarioExecuteRequest.getType())) {
                list.addAll(planApiAutomationService.run(request));
            } else {
                // 以下三个参数接口测试未使用到，暂时迁移到这里
                request.setIds(new ArrayList<>(scenarioMap.values()));//场景IDS
                request.setScenarioTestPlanIdMap(scenarioMap);//未知
                request.setPlanCaseIds(new ArrayList<>(testPlanScenarioIdMap.keySet()));

                RunUiScenarioRequest runUiScenarioRequest = new RunUiScenarioRequest();
                BeanUtils.copyBean(runUiScenarioRequest, request);
                RunModeConfigDTO configDTO = new RunModeConfigDTO();
                BeanUtils.copyBean(configDTO, Optional.ofNullable(request.getConfig()).orElse(new RunModeConfigDTO()));
                runUiScenarioRequest.setUiConfig(configDTO);
                list.addAll(planUiAutomationService.run(runUiScenarioRequest));
            }
        }
        return list;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public TestPlanScheduleReportInfoDTO genTestPlanReport(String planReportId, String planId, String userId, String
            triggerMode, RunModeConfigDTO runModeConfigDTO) {
        return testPlanReportService.genTestPlanReportBySchedule(planReportId, planId, userId, triggerMode, runModeConfigDTO);
    }

    public void verifyPool(String projectId, RunModeConfigDTO runConfig) {
        if (StringUtils.isNotBlank(runConfig.getResourcePoolId())) {
            //检查保存的资源池的合法性
            TestResourcePool testResourcePool = baseTestResourcePoolService.getResourcePool(runConfig.getResourcePoolId());
            if (!StringUtils.equalsIgnoreCase(testResourcePool.getStatus(), "VALID")) {
                MSException.throwException("保存的资源池无法使用，请重新选择资源池");
            }
        } else {
            // 检查是否禁用了本地执行
            apiPoolDebugService.verifyPool(projectId, runConfig);
        }
    }

    /**
     * 将测试计划运行时的triggerMode转化为性能测试中辨别更明确的值
     *
     * @param triggerMode
     * @return
     */
    public String transformationPerfTriggerMode(String triggerMode) {
        if (StringUtils.equalsIgnoreCase(triggerMode, ReportTriggerMode.SCHEDULE.name())) {
            return ReportTriggerMode.TEST_PLAN_SCHEDULE.name();
        } else if (StringUtils.equalsIgnoreCase(triggerMode, ReportTriggerMode.API.name())) {
            return ReportTriggerMode.TEST_PLAN_API.name();
        } else {
            return triggerMode;
        }
    }

    public Map<String, String> executeApiTestCase(String triggerMode, String planReportId, String userId, String
            testPlanId, RunModeConfigDTO runModeConfig) {
        BatchRunDefinitionRequest request = new BatchRunDefinitionRequest();
        request.setTriggerMode(triggerMode);
        request.setPlanReportId(planReportId);
        request.setConfig(runModeConfig);
        request.setUserId(userId);
        request.setTestPlanId(testPlanId);
        List<MsExecResponseDTO> dtoList = planTestPlanApiCaseService.run(request);
        return this.parseMsExecResponseDTOToTestIdReportMap(dtoList);
    }

    public Map<String, String> executeScenarioCase(String planReportId, String testPlanID, String
            projectID, RunModeConfigDTO runModeConfig, String triggerMode, String
                                                           userId, Map<String, String> planScenarioIdMap) {
        if (!planScenarioIdMap.isEmpty()) {
            SchedulePlanScenarioExecuteRequest scenarioRequest = new SchedulePlanScenarioExecuteRequest();
            String scenarioReportID = UUID.randomUUID().toString();
            scenarioRequest.setId(scenarioReportID);
            scenarioRequest.setReportId(scenarioReportID);
            scenarioRequest.setProjectId(projectID);
            if (StringUtils.equals(triggerMode, ReportTriggerMode.API.name())) {
                scenarioRequest.setTriggerMode(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name());
                scenarioRequest.setRunMode(ApiRunMode.JENKINS_SCENARIO_PLAN.name());
            } else if (StringUtils.equals(triggerMode, ReportTriggerMode.MANUAL.name())) {
                scenarioRequest.setTriggerMode(ReportTriggerMode.MANUAL.name());
                scenarioRequest.setRunMode(ApiRunMode.JENKINS_SCENARIO_PLAN.name());
            } else {
                scenarioRequest.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
                scenarioRequest.setRunMode(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name());
            }
            scenarioRequest.setExecuteType("Saved");
            Map<String, Map<String, String>> testPlanScenarioIdMap = new HashMap<>();
            testPlanScenarioIdMap.put(testPlanID, planScenarioIdMap);
            scenarioRequest.setTestPlanScenarioIDMap(testPlanScenarioIdMap);
            scenarioRequest.setReportUserID(userId);
            scenarioRequest.setTestPlanID(testPlanID);
            scenarioRequest.setTestPlanReportId(planReportId);
            scenarioRequest.setConfig(runModeConfig);
            scenarioRequest.setType("api");
            List<MsExecResponseDTO> dtoList = this.scenarioRunModeConfig(scenarioRequest);
            return this.parseMsExecResponseDTOToTestIdReportMap(dtoList);
        } else {
            return new HashMap<>();
        }
    }

    public Map<String, String> executeUiScenarioCase(String planReportId, String testPlanID, String
            projectID, RunModeConfigDTO runModeConfig, String triggerMode, String
                                                             userId, Map<String, String> planScenarioIdMap) {
        if (!planScenarioIdMap.isEmpty()) {
            SchedulePlanScenarioExecuteRequest scenarioRequest = new SchedulePlanScenarioExecuteRequest();
            String scenarioReportID = UUID.randomUUID().toString();
            scenarioRequest.setId(scenarioReportID);
            scenarioRequest.setReportId(scenarioReportID);
            scenarioRequest.setProjectId(projectID);
            if (StringUtils.equals(triggerMode, ReportTriggerMode.API.name())) {
                scenarioRequest.setTriggerMode(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name());
                scenarioRequest.setRunMode(ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name());
            } else if (StringUtils.equals(triggerMode, ReportTriggerMode.MANUAL.name())) {
                scenarioRequest.setTriggerMode(ReportTriggerMode.MANUAL.name());
                scenarioRequest.setRunMode(ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name());
            } else {
                scenarioRequest.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
                scenarioRequest.setRunMode(ApiRunMode.UI_SCHEDULE_SCENARIO_PLAN.name());
            }
            scenarioRequest.setExecuteType("Saved");
            Map<String, Map<String, String>> testPlanScenarioIdMap = new HashMap<>();
            testPlanScenarioIdMap.put(testPlanID, planScenarioIdMap);
            scenarioRequest.setTestPlanScenarioIDMap(testPlanScenarioIdMap);
            scenarioRequest.setReportUserID(userId);
            scenarioRequest.setTestPlanID(testPlanID);
            scenarioRequest.setTestPlanReportId(planReportId);
            scenarioRequest.setConfig(runModeConfig);
            scenarioRequest.setType("ui");
            List<MsExecResponseDTO> dtoList = this.scenarioRunModeConfig(scenarioRequest);
            return this.parseMsExecResponseDTOToTestIdReportMap(dtoList);
        } else {
            return new HashMap<>();
        }
    }

    private Map<String, String> parseMsExecResponseDTOToTestIdReportMap(List<MsExecResponseDTO> dtoList) {
        Map<String, String> returnMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(dtoList)) {
            dtoList.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getTestId()) && StringUtils.isNotEmpty(item.getReportId())) {
                    returnMap.put(item.getTestId(), item.getReportId());
                }
            });
        }
        return returnMap;
    }

    public String getLogDetails(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            TestPlanExample testPlanExample = new TestPlanExample();
            testPlanExample.createCriteria().andIdIn(ids);
            List<TestPlan> planList = testPlanMapper.selectByExample(testPlanExample);
            if (CollectionUtils.isNotEmpty(planList)) {
                List<String> names = planList.stream().map(TestPlan::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), planList.get(0).getProjectId(), String.join(",", names), planList.get(0).getCreator(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getDeleteBatchLogDetails(BatchOperateRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            QueryTestPlanRequest queryTestPlanRequest = request.getQueryTestPlanRequest();
            request.getQueryTestPlanRequest().setOrders(ServiceUtils.getDefaultOrder(queryTestPlanRequest.getOrders()));
            if (StringUtils.isNotBlank(queryTestPlanRequest.getProjectId())) {
                request.getQueryTestPlanRequest().setProjectId(queryTestPlanRequest.getProjectId());
            }
            List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.list(queryTestPlanRequest);
            ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
        }
        return getLogDetails(ids);
    }

    public String getLogDetails(String id) {
        TestPlan plan = testPlanMapper.selectByPrimaryKey(id);
        if (plan != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(plan, TestPlanReference.testPlanColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), plan.getProjectId(), plan.getName(), plan.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(PlanCaseRelevanceRequest request) {
        List<String> testCaseIds = request.getIds();
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdIn(testCaseIds);
        List<TestCase> cases = testCaseMapper.selectByExample(example);
        List<String> names = cases.stream().map(TestCase::getName).collect(Collectors.toList());
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
        if (testPlan != null) {
            List<DetailColumn> columns = new LinkedList<>();
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(testCaseIds), testPlan.getProjectId(), String.join(",", names) + " 关联到 " + "【" + testPlan.getName() + "】", testPlan.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public TestPlan copy(String planId) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(planId);
        if (testPlan == null) {
            return null;
        }
        String sourcePlanId = testPlan.getId();
        String targetPlanId = UUID.randomUUID().toString();

        TestPlanWithBLOBs targetPlan = new TestPlanWithBLOBs();
        BeanUtils.copyBean(targetPlan, testPlan);
        // 覆盖原内容
        targetPlan.setId(targetPlanId);
        targetPlan.setName(testPlan.getName() + "_" + UUID.randomUUID().toString().substring(0, 5) + "_COPY");
        if (targetPlan.getName().length() > 128) {
            targetPlan.setName(testPlan.getName().substring(0, 116) + "_" + UUID.randomUUID().toString().substring(0, 5) + "_COPY");
        }
        targetPlan.setStatus(TestPlanStatus.Prepare.name());
        targetPlan.setCreator(SessionUtils.getUserId());
        targetPlan.setCreateTime(System.currentTimeMillis());
        targetPlan.setUpdateTime(System.currentTimeMillis());
        testPlanMapper.insert(targetPlan);

        copyPlanPrincipal(targetPlanId, planId);
        copyPlanCase(sourcePlanId, targetPlanId);

        return targetPlan;
    }

    private void copyPlanPrincipal(String targetPlanId, String sourcePlanId) {
        TestPlanPrincipalExample example = new TestPlanPrincipalExample();
        example.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
        List<TestPlanPrincipal> testPlanPrincipals = testPlanPrincipalMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(testPlanPrincipals)) {
            for (TestPlanPrincipal tpp : testPlanPrincipals) {
                TestPlanPrincipal testPlanPrincipal = new TestPlanPrincipal();
                testPlanPrincipal.setPrincipalId(tpp.getPrincipalId());
                testPlanPrincipal.setTestPlanId(targetPlanId);
                testPlanPrincipalMapper.insert(testPlanPrincipal);
            }
        }
    }

    public void copyPlanCase(String sourcePlanId, String targetPlanId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(sourcePlanId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestPlanTestCaseMapper testCaseMapper = sqlSession.getMapper(TestPlanTestCaseMapper.class);
            if (!CollectionUtils.isEmpty(testPlanTestCases)) {
                Long nextTestCaseOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanTestCaseMapper::getLastOrder);
                for (TestPlanTestCase testCase : testPlanTestCases) {
                    if (BooleanUtils.isNotTrue(testCase.getIsDel())) {
                        TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
                        testPlanTestCase.setId(UUID.randomUUID().toString());
                        testPlanTestCase.setPlanId(targetPlanId);
                        testPlanTestCase.setCaseId(testCase.getCaseId());
                        testPlanTestCase.setStatus("Prepare");
                        testPlanTestCase.setExecutor(testCase.getExecutor());
                        testPlanTestCase.setCreateTime(System.currentTimeMillis());
                        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
                        testPlanTestCase.setCreateUser(SessionUtils.getUserId());
                        testPlanTestCase.setRemark(testCase.getRemark());
                        testPlanTestCase.setOrder(nextTestCaseOrder);
                        testPlanTestCase.setIsDel(false);
                        nextTestCaseOrder += 5000;
                        testCaseMapper.insert(testPlanTestCase);
                    }
                }
            }
            sqlSession.flushStatements();

            Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();

            if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
                planTestPlanApiCaseService.copyPlan(sourcePlanId, targetPlanId);
                planTestPlanScenarioCaseService.copyPlan(sourcePlanId, targetPlanId);
            }

            if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
                planTestPlanLoadCaseService.copyPlan(sourcePlanId, targetPlanId);
            }

            if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
                planTestPlanUiScenarioCaseService.copyPlan(sourcePlanId, targetPlanId);
            }

            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public String getShareReport() {
        Object microServices = DiscoveryUtil.getServices();
        return replaceSharReport(microServices);
    }

    /**
     * 获取微服务信息，替换前端变量
     * 实现跨服务访问报告
     */
    public String replaceSharReport(Object microServices) {
        try (InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("/public/share-plan-report.html"), StandardCharsets.UTF_8);) {
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder reportStr = new StringBuilder();
            String line;
            while (null != (line = bufferedReader.readLine())) {
                if (line.contains("\"#microService\"")) {
                    line = line.replace("\"#microService\"", new Gson().toJson(microServices));
                }
                line += StringUtils.LF;
                reportStr.append(line);
            }
            return reportStr.toString();
        } catch (Throwable e) {
            LogUtil.error(e);
            MSException.throwException(e);
        }
        return null;
    }

    public Map<String, List<String>> getApiCaseEnv(List<String> planApiCaseIds) {
        return planTestPlanApiCaseService.getApiCaseEnv(planApiCaseIds);
    }

    public Map<String, List<String>> getApiScenarioEnv(List<String> planApiScenarioIds) {
        return planTestPlanScenarioCaseService.getApiCaseEnv(planApiScenarioIds);
    }

    public void buildFunctionalReport(TestPlanReportDataStruct report, Map config, String planId) {
        if (checkReportConfig(config, "functional")) {
            List<TestPlanCaseDTO> allCases = null;
            List<String> statusList = getFunctionalReportStatusList(config);
            if (statusList != null) {
                // 不等于null，说明配置了用例，根据配置的状态查询用例
                allCases = testPlanTestCaseService.getAllCasesByStatusList(planId, statusList);
                report.setFunctionAllCases(allCases);
            }

            if (TestPlanReportUtil.checkReportConfig(config, "functional", "issue")) {
                List<IssuesDao> issueList = issuesService.getIssuesByPlanId(planId);
                report.setIssueList(issueList);
            }
        }
    }

    public void buildUiReport(TestPlanReportDataStruct report, Map config, String
            planId, TestPlanUiExecuteReportDTO testPlanExecuteReportDTO, boolean saveResponse) {
        UiPlanReportRequest request = new UiPlanReportRequest();
        request.setConfig(config);
        request.setPlanId(planId);
        request.setSaveResponse(saveResponse);
        request.setTestPlanExecuteReportDTO(testPlanExecuteReportDTO);
        if (DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            UiPlanReportDTO uiReport = planTestPlanUiScenarioCaseService.getUiReport(request);
            BeanUtils.copyBean(report, uiReport);
        }
    }

    /**
     * 如果配置了全部用例返回空的数组
     * 如果没有，则添加对应的状态
     * 都没配置就返回 null
     *
     * @param config
     * @return
     */
    public List<String> getFunctionalReportStatusList(Map config) {
        List<String> statusList = new ArrayList<>();
        if (TestPlanReportUtil.checkReportConfig(config, "functional", "all")) {
            return statusList;
        }
        if (TestPlanReportUtil.checkReportConfig(config, "functional", "failure")) {
            statusList.add(TestPlanTestCaseStatus.Failure.name());
        }
        if (TestPlanReportUtil.checkReportConfig(config, "functional", "blocking")) {
            statusList.add(TestPlanTestCaseStatus.Blocking.name());
        }
        if (TestPlanReportUtil.checkReportConfig(config, "functional", "skip")) {
            statusList.add(TestPlanTestCaseStatus.Skip.name());
        }
        return statusList.size() > 0 ? statusList : null;
    }

    public void buildApiReport(TestPlanReportDataStruct report, Map config, String planId, boolean saveResponse) {
        ApiPlanReportRequest request = new ApiPlanReportRequest();
        request.setConfig(config);
        request.setPlanId(planId);
        request.setSaveResponse(saveResponse);
        if (DiscoveryUtil.hasService(MicroServiceName.API_TEST)) {
            ApiPlanReportDTO apiReport = planTestPlanScenarioCaseService.getApiReport(request);
            BeanUtils.copyBean(report, apiReport);
        }
    }

    public void buildLoadReport(TestPlanReportDataStruct report, Map config, String planId, boolean saveResponse) {
        ApiPlanReportRequest request = new ApiPlanReportRequest();
        request.setConfig(config);
        request.setPlanId(planId);
        request.setSaveResponse(saveResponse);
        if (DiscoveryUtil.hasService(MicroServiceName.PERFORMANCE_TEST)) {
            LoadPlanReportDTO loadPlanReport = planTestPlanLoadCaseService.getLoadReport(request);
            BeanUtils.copyBean(report, loadPlanReport);
        }
    }

    /**
     * @param testPlanReport                 测试计划报告
     * @param testPlanReportContentWithBLOBs 测试计划报告内容
     */
    public TestPlanReportDataStruct generateReportStruct(TestPlanWithBLOBs testPlan,
                                                         TestPlanReport testPlanReport,
                                                         TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs,
                                                         boolean rebuildReport) {
        TestPlanReportDataStruct testPlanReportStruct = null;
        if (ObjectUtils.allNotNull(testPlanReport, testPlanReportContentWithBLOBs)) {
            Map config = null;
            if (StringUtils.isNotBlank(testPlan.getReportConfig())) {
                config = JSON.parseMap(testPlan.getReportConfig());
            }
            testPlanReportStruct = this.getTestPlanReportStructByCreated(testPlanReportContentWithBLOBs);
            //检查是否有已经生成过的测试计划报告内容。如若没有则进行动态计算
            if (rebuildReport || testPlanReportStruct == null) {
                //查询测试计划内的用例信息，然后进行测试计划报告的结果统计
                TestPlanCaseReportResultDTO testPlanExecuteReportDTO = testPlanReportService.selectCaseDetailByTestPlanReport(config, testPlan.getId(), testPlanReportContentWithBLOBs);
                testPlanReportStruct = initTestPlanReportStructData(
                        config,
                        testPlanReport,
                        testPlan, testPlanExecuteReportDTO);
            } else {
                //针对已经保存过的数据结构，增加对旧版本数据的支持
                if (testPlanReportContentWithBLOBs.getStartTime() != null && testPlanReportContentWithBLOBs.getStartTime() > 0) {
                    testPlanReportStruct.setStartTime(testPlanReportContentWithBLOBs.getStartTime());
                }
                if (testPlanReportContentWithBLOBs.getEndTime() != null && testPlanReportContentWithBLOBs.getEndTime() > 0) {
                    testPlanReportStruct.setEndTime(testPlanReportContentWithBLOBs.getEndTime());
                }

                this.dealOldVersionData(testPlanReportStruct);
            }
            //查找运行环境
            testPlanReportService.initRunInformation(testPlanReportStruct, testPlanReport);
        }
        return testPlanReportStruct == null ? new TestPlanReportDataStruct() : testPlanReportStruct;
    }

    /**
     * 处理旧版本数据（例如版本升级过程中由于统一了状态字段的数据） 或者是由旧版本fastJson解析的，无法被Jackson解析出来的数据
     */
    private void dealOldVersionData(TestPlanReportDataStruct testPlanReportStruct) {
        List<TestPlanScenarioDTO> validScenarioList = this.getValidScenarioList(testPlanReportStruct);
        testPlanReportStruct.setScenarioAllCases(validScenarioList);
        List<TestPlanScenarioDTO> errorScenarioList = this.getScenarioListByStatus(testPlanReportStruct, "error");
        List<TestPlanScenarioDTO> fakeErrorScenarioList = this.getScenarioListByStatus(testPlanReportStruct, "fakeError");
        List<TestPlanScenarioDTO> unExecuteScenarioList = this.getScenarioListByStatus(testPlanReportStruct, "unExecute");
        testPlanReportStruct.setScenarioFailureCases(errorScenarioList);
        testPlanReportStruct.setErrorReportScenarios(fakeErrorScenarioList);
        testPlanReportStruct.setUnExecuteScenarios(unExecuteScenarioList);

        List<TestPlanApiDTO> validApiList = this.getValidApiList(testPlanReportStruct);
        testPlanReportStruct.setApiAllCases(validApiList);
        List<TestPlanApiDTO> errorApiList = this.getApiListByStatus(testPlanReportStruct, "error");
        List<TestPlanApiDTO> fakeErrorApiList = this.getApiListByStatus(testPlanReportStruct, "fakeError");
        List<TestPlanApiDTO> unExecuteApiList = this.getApiListByStatus(testPlanReportStruct, "unExecute");
        testPlanReportStruct.setApiFailureCases(errorApiList);
        testPlanReportStruct.setErrorReportCases(fakeErrorApiList);
        testPlanReportStruct.setUnExecuteCases(unExecuteApiList);
    }

    private List<TestPlanApiDTO> getValidApiList(TestPlanReportDataStruct testPlanReportStruct) {
        if (CollectionUtils.isNotEmpty(testPlanReportStruct.getApiAllCases())) {
            List<TestPlanApiDTO> allApiList = new ArrayList<>(testPlanReportStruct.getApiAllCases().stream().filter(item -> item.getReportId() != null).toList());
            if (CollectionUtils.isNotEmpty(testPlanReportStruct.getApiFailureCases())) {
                for (TestPlanApiDTO item : testPlanReportStruct.getApiFailureCases()) {
                    if (StringUtils.isNotEmpty(item.getReportId()) && !this.isApiListContainsByReportId(allApiList, item)) {
                        allApiList.add(item);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(testPlanReportStruct.getErrorReportCases())) {
                testPlanReportStruct.getErrorReportCases().forEach(item -> {
                    if (StringUtils.isNotEmpty(item.getReportId()) && !this.isApiListContainsByReportId(allApiList, item)) {
                        allApiList.add(item);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(testPlanReportStruct.getUnExecuteScenarios())) {
                testPlanReportStruct.getUnExecuteCases().forEach(item -> {
                    if (StringUtils.isNotEmpty(item.getReportId()) && !this.isApiListContainsByReportId(allApiList, item)) {
                        allApiList.add(item);
                    }
                });
            }
            return allApiList;
        } else {
            return new ArrayList<>(0);
        }
    }

    private List<TestPlanScenarioDTO> getValidScenarioList(TestPlanReportDataStruct testPlanReportStruct) {
        //旧版本fastJson解析的数据结构不会保存所有的数据，内存地址相同的数据它会做一个引用。这里要补全数据结构
        if (CollectionUtils.isNotEmpty(testPlanReportStruct.getScenarioAllCases())) {
            List<TestPlanScenarioDTO> allTestPlanScenariODTOList = new ArrayList<>(testPlanReportStruct.getScenarioAllCases().stream().filter(item -> item.getReportId() != null).toList());

            if (CollectionUtils.isNotEmpty(testPlanReportStruct.getScenarioFailureCases())) {
                testPlanReportStruct.getScenarioFailureCases().forEach(item -> {
                    if (StringUtils.isNotEmpty(item.getReportId()) && !this.isScenarioListContainsByReportId(allTestPlanScenariODTOList, item)) {
                        allTestPlanScenariODTOList.add(item);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(testPlanReportStruct.getErrorReportScenarios())) {
                testPlanReportStruct.getErrorReportScenarios().forEach(item -> {
                    if (StringUtils.isNotEmpty(item.getReportId()) && !this.isScenarioListContainsByReportId(allTestPlanScenariODTOList, item)) {
                        allTestPlanScenariODTOList.add(item);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(testPlanReportStruct.getUnExecuteScenarios())) {
                testPlanReportStruct.getUnExecuteScenarios().forEach(item -> {
                    if (StringUtils.isNotEmpty(item.getReportId()) && !this.isScenarioListContainsByReportId(allTestPlanScenariODTOList, item)) {
                        allTestPlanScenariODTOList.add(item);
                    }
                });
            }
            return allTestPlanScenariODTOList;
        } else {
            return new ArrayList<>(0);
        }
    }

    private List<TestPlanApiDTO> getApiListByStatus(TestPlanReportDataStruct testPlanReportStruct, String status) {
        if (CollectionUtils.isNotEmpty(testPlanReportStruct.getApiAllCases())) {
            //使用LinkedHashMap是为了确保reportId的一致性，同时保证顺序
            Map<String, TestPlanApiDTO> errorApiDTOMap = new LinkedHashMap<>();
            List<String> checkStatusList;
            List<TestPlanApiDTO> statusApiList = null;

            if (StringUtils.equalsIgnoreCase(status, "fakeError")) {
                checkStatusList = new ArrayList<>() {{
                    this.add("errorReport".toLowerCase());
                    this.add("errorReportResult".toLowerCase());
                }};
                statusApiList = testPlanReportStruct.getErrorReportCases().stream().filter(item -> item.getReportId() != null).toList();
            } else if (StringUtils.equalsIgnoreCase(status, "unExecute")) {
                checkStatusList = new ArrayList<>() {{
                    this.add("stop".toLowerCase());
                    this.add("unExecute".toLowerCase());
                }};
                statusApiList = testPlanReportStruct.getUnExecuteCases().stream().filter(item -> item.getReportId() != null).toList();
            } else if (StringUtils.equalsIgnoreCase(status, "error")) {
                checkStatusList = new ArrayList<>() {{
                    this.add("Error".toLowerCase());
                }};
                statusApiList = testPlanReportStruct.getApiFailureCases().stream().filter(item -> item.getReportId() != null).toList();
            } else {
                checkStatusList = null;
                statusApiList = new ArrayList<>();
            }
            if (CollectionUtils.isNotEmpty(statusApiList)) {
                statusApiList.forEach(item -> {
                    if (StringUtils.isNotBlank(item.getReportId())) {
                        errorApiDTOMap.put(item.getReportId(), item);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(checkStatusList)) {
                testPlanReportStruct.getApiAllCases().forEach(item -> {
                    if (item.getExecResult() != null && checkStatusList.contains(item.getExecResult().toLowerCase())) {
                        errorApiDTOMap.put(item.getReportId(), item);
                    }
                });
            }
            return new ArrayList<>(errorApiDTOMap.values());
        } else {
            return new ArrayList<>(0);
        }
    }

    private List<TestPlanScenarioDTO> getScenarioListByStatus(TestPlanReportDataStruct testPlanReportStruct, String status) {
        if (CollectionUtils.isNotEmpty(testPlanReportStruct.getScenarioAllCases())) {
            //使用LinkedHashMap是为了确保reportId的一致性，同时保证顺序
            Map<String, TestPlanScenarioDTO> statusScenarioDTOMap = new LinkedHashMap<>();
            List<String> checkStatusList;
            List<TestPlanScenarioDTO> statusScenarioList = null;

            if (StringUtils.equalsIgnoreCase(status, "fakeError")) {
                checkStatusList = new ArrayList<>() {{
                    this.add("errorReport".toLowerCase());
                    this.add("errorReportResult".toLowerCase());
                }};
                statusScenarioList = testPlanReportStruct.getErrorReportScenarios().stream().filter(item -> item.getReportId() != null).toList();
            } else if (StringUtils.equalsIgnoreCase(status, "unExecute")) {
                checkStatusList = new ArrayList<>() {{
                    this.add("unExecute".toLowerCase());
                    this.add("stop".toLowerCase());
                }};
                statusScenarioList = testPlanReportStruct.getUnExecuteScenarios().stream().filter(item -> item.getReportId() != null).toList();
            } else if (StringUtils.equalsIgnoreCase(status, "error")) {
                checkStatusList = new ArrayList<>() {{
                    this.add("Fail".toLowerCase());
                    this.add("Error".toLowerCase());
                }};
                statusScenarioList = testPlanReportStruct.getScenarioFailureCases().stream().filter(item -> item.getReportId() != null).toList();
            } else {
                checkStatusList = null;
                statusScenarioList = new ArrayList<>();
            }
            if (CollectionUtils.isNotEmpty(statusScenarioList)) {
                statusScenarioList.forEach(item -> {
                    if (StringUtils.isNotBlank(item.getReportId())) {
                        statusScenarioDTOMap.put(item.getReportId(), item);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(checkStatusList)) {
                testPlanReportStruct.getScenarioAllCases().forEach(item -> {
                    if (item.getLastResult() != null && checkStatusList.contains(item.getLastResult().toLowerCase())) {
                        statusScenarioDTOMap.put(item.getReportId(), item);
                    }
                });
            }
            return new ArrayList<>(statusScenarioDTOMap.values());
        } else {
            return new ArrayList<>(0);
        }
    }

    private boolean isApiListContainsByReportId(List<TestPlanApiDTO> list, TestPlanApiDTO checkItem) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (TestPlanApiDTO item : list) {
                if (StringUtils.equals(item.getReportId(), checkItem.getReportId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isScenarioListContainsByReportId(List<TestPlanScenarioDTO> list, TestPlanScenarioDTO checkItem) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (TestPlanScenarioDTO item : list) {
                if (StringUtils.equals(item.getReportId(), checkItem.getReportId())) {
                    return true;
                }
            }
        }
        return false;
    }

    //获取已生成过的测试计划报告内容
    private TestPlanReportDataStruct getTestPlanReportStructByCreated(TestPlanReportContentWithBLOBs
                                                                              testPlanReportContentWithBLOBs) {
        TestPlanReportDataStruct reportStruct = null;
        try {
            if (StringUtils.isNotEmpty(testPlanReportContentWithBLOBs.getApiBaseCount())) {
                reportStruct = JSON.parseObject(testPlanReportContentWithBLOBs.getApiBaseCount(), TestPlanReportDataStruct.class);
            }
        } catch (Exception e) {
            LogUtil.info("解析接口统计数据出错！数据：" + testPlanReportContentWithBLOBs.getApiBaseCount(), e);
        }
        return reportStruct;
    }

    public TestPlanReportDataStruct buildPlanReport(String planId, boolean saveResponse) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(planId);

        String reportConfig = testPlan.getReportConfig();
        Map config = null;
        if (StringUtils.isNotBlank(reportConfig)) {
            config = JSON.parseMap(reportConfig);
        }
        TestPlanReportDataStruct report = testPlanReportService.getRealTimeReport(planId);
        buildFunctionalReport(report, config, planId);
        buildApiReport(report, config, planId, saveResponse);
        buildLoadReport(report, config, planId, saveResponse);
        buildUiReport(report, config, planId, null, saveResponse);
        return report;
    }

    public void exportPlanReport(String planId, String lang, HttpServletResponse response) throws
            UnsupportedEncodingException, JsonProcessingException {
        TestPlanReportDataStruct report = buildPlanReport(planId, true);
        report.setLang(lang);
        render(report, response);
    }

    public void exportPlanDbReport(String reportId, String lang, HttpServletResponse response) throws
            UnsupportedEncodingException, JsonProcessingException {
        TestPlanReportDataStruct report = testPlanReportService.getReport(reportId);
        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();
        if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
            report.setApiAllCases(planTestPlanApiCaseService.buildResponse(report.getApiAllCases()));
            report.setApiFailureCases(planTestPlanApiCaseService.buildResponse(report.getApiFailureCases()));
            report.setErrorReportCases(planTestPlanApiCaseService.buildResponse(report.getErrorReportCases()));
            report.setScenarioAllCases(planTestPlanScenarioCaseService.buildResponse(report.getScenarioAllCases()));
            report.setScenarioFailureCases(planTestPlanScenarioCaseService.buildResponse(report.getScenarioFailureCases()));
            report.setErrorReportScenarios(planTestPlanScenarioCaseService.buildResponse(report.getErrorReportScenarios()));
        }

        if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
            report.setUiAllCases(planTestPlanUiScenarioCaseService.buildResponse(report.getUiAllCases()));
            report.setUiFailureCases(planTestPlanUiScenarioCaseService.buildResponse(report.getUiFailureCases()));
        }

        if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
            report.setLoadAllCases(planTestPlanLoadCaseService.buildResponse(report.getLoadAllCases()));
        }

        report.setLang(lang);
        render(report, response);
    }

    public Boolean checkReportConfig(Map config, String key) {
        return ServiceUtils.checkConfigEnable(config, key);
    }

    public void render(TestPlanReportDataStruct report, HttpServletResponse response) throws
            UnsupportedEncodingException {
        response.reset();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("test", StandardCharsets.UTF_8.name()));

        try (InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("/public/plan-report.html"), StandardCharsets.UTF_8);
             ServletOutputStream outputStream = response.getOutputStream()) {
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line = null;
            while (null != (line = bufferedReader.readLine())) {
                if (line.contains("\"#report\"")) {
                    String reportInfo = new Gson().toJson(report);
                    line = line.replace("\"#report\"", reportInfo);
                }
                line += StringUtils.LF;
                byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
                int start = 0;
                while (start < lineBytes.length) {
                    if (start + 1024 < lineBytes.length) {
                        outputStream.write(lineBytes, start, 1024);
                    } else {
                        outputStream.write(lineBytes, start, lineBytes.length - start);
                    }
                    outputStream.flush();
                    start += 1024;
                }
            }
        } catch (Throwable e) {
            LogUtil.error(e);
            MSException.throwException(e);
        }
    }

    public TestPlanReportDataStruct getShareReport(ShareInfo shareInfo, String planId) {
        if (SessionUtils.getUser() == null) {
            HttpHeaderUtils.runAsUser(shareInfo.getCreateUserId());
        }
        try {
            return testPlanReportService.getRealTimeReport(planId);
        } finally {
            HttpHeaderUtils.clearUser();
        }
    }

    //根据用例运行结果生成测试计划报告
    public TestPlanReportDataStruct initTestPlanReportStructData(Map reportConfig, TestPlanReport
            testPlanReport, TestPlanWithBLOBs testPlan, TestPlanCaseReportResultDTO testPlanCaseReportResultDTO) {
        TestPlanReportDataStruct report = new TestPlanReportDataStruct();
        if (ObjectUtils.anyNotNull(testPlan, testPlanReport, testPlanCaseReportResultDTO)) {
            TestPlanFunctionResultReportDTO functionResult = new TestPlanFunctionResultReportDTO();
            TestPlanApiResultReportDTO apiResult = new TestPlanApiResultReportDTO();
            TestPlanUiResultReportDTO uiResult = new TestPlanUiResultReportDTO();
            report.setFunctionResult(functionResult);
            report.setApiResult(apiResult);
            report.setUiResult(uiResult);
            report.setStartTime(testPlanReport.getCreateTime());
            if (!StringUtils.equals(
                    DateUtils.getTimeString(testPlanReport.getCreateTime()),
                    DateUtils.getTimeString(testPlanReport.getEndTime()))
                    && testPlanReport.getEndTime() != 0) {
                //防止测试计划报告非正常状态停止时造成的测试时间显示不对
                report.setEndTime(testPlanReport.getEndTime());
            }
            report.setSummary(testPlan.getReportSummary());
            report.setConfig(testPlan.getReportConfig());

            if (testPlanCaseReportResultDTO.getApiPlanReportDTO() != null) {
                BeanUtils.copyBean(report, testPlanCaseReportResultDTO.getApiPlanReportDTO());
                planTestPlanApiCaseService.calculateReportByApiCase(testPlanCaseReportResultDTO.getApiPlanReportDTO().getApiAllCases(), report);
                planTestPlanScenarioCaseService.calculateReportByScenario(testPlanCaseReportResultDTO.getApiPlanReportDTO().getScenarioAllCases(), report);
                this.sortApiCaseResultCount(report.getApiResult());
            }
            if (testPlanCaseReportResultDTO.getLoadPlanReportDTO() != null) {
                BeanUtils.copyBean(report, testPlanCaseReportResultDTO.getLoadPlanReportDTO());
                planTestPlanLoadCaseService.calculateReportByLoadCaseList(testPlanCaseReportResultDTO.getLoadPlanReportDTO().getLoadAllCases(), report);
            }
            if (testPlanCaseReportResultDTO.getUiPlanReportDTO() != null) {
                BeanUtils.copyBean(report, testPlanCaseReportResultDTO.getUiPlanReportDTO());
                planTestPlanUiScenarioCaseService.calculateReportByUiScenarios(testPlanCaseReportResultDTO.getUiPlanReportDTO().getUiAllCases(), report);
            }

            //功能用例的状态更新以及统计
            testPlanTestCaseService.calculateReportByTestCaseList(
                    testPlanReport.getCreator(),
                    testPlan,
                    StringUtils.equalsAnyIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.COMPLETED.name(), TestPlanReportStatus.SUCCESS.name(), TestPlanReportStatus.FAILED.name()),
                    testPlanCaseReportResultDTO.getFunctionCaseList(), report);
            if (report.getFunctionAllCases() == null || report.getIssueList() == null) {
                //构建功能用例和issue
                this.buildFunctionalReport(report, reportConfig, testPlan.getId());
            }
            issuesService.calculateReportByIssueList(testPlanCaseReportResultDTO.getIssueList(), report);

            DecimalFormat rateFormat = new DecimalFormat("#0.0000");
            rateFormat.setMinimumFractionDigits(4);
            rateFormat.setMaximumFractionDigits(4);

            if (report.getExecuteCount() != 0 && report.getCaseCount() != null) {
                double executeRate = Double.parseDouble(rateFormat.format((double) report.getExecuteCount() / (double) report.getCaseCount()));
                if (executeRate == 1 && report.getExecuteCount() < report.getCaseCount()) {
                    report.setExecuteRate(0.9999);
                } else {
                    report.setExecuteRate(executeRate);
                }
            } else {
                report.setExecuteRate(0.0000);
            }
            if (report.getPassCount() != 0 && report.getCaseCount() != null) {
                double passRate = Double.parseDouble(rateFormat.format((double) report.getPassCount() / (double) report.getCaseCount()));
                if (passRate == 1 && report.getPassCount() < report.getCaseCount()) {
                    report.setPassRate(0.9999);
                } else {
                    report.setPassRate(passRate);
                }
            } else {
                report.setPassRate(0.0000);
            }

            report.setName(testPlan.getName());
            Project project = baseProjectService.getProjectById(testPlan.getProjectId());
            report.setIsThirdPartIssue(project.getPlatform() == null || !project.getPlatform().equals(IssuesManagePlatform.Local.name()));
        }
        return report;
    }

    private void sortApiCaseResultCount(TestPlanApiResultReportDTO apiResult) {
        if (apiResult != null && CollectionUtils.isNotEmpty(apiResult.getApiCaseData())) {
            /**
             *排序方式： 未通过->误报->成功->未执行 未执行的放最后，成功倒数第二，以此类推。
             * 这样做的目的是因为前台统计百分比时，四舍五入处理之后，有时会出现总百分比加起来不等于100%的情况。
             * 为了解决这个问题，按照上序排序方式， 排在最后的状态，采用 100-其余百分比总和 来计算。
             */
            List<TestCaseReportStatusResultDTO> oldData = apiResult.getApiCaseData();
            List<TestCaseReportStatusResultDTO> apiCaseData = new ArrayList<>();

            TestCaseReportStatusResultDTO errorStatusDTO = null;
            TestCaseReportStatusResultDTO fakeErrorStatusDTO = null;
            TestCaseReportStatusResultDTO successStatusDTO = null;
            //因为未执行的状态比较多， 比如停止、未运行、准备中等都属于未执行。所以这里直接用排除法，并用集合接收
            List<TestCaseReportStatusResultDTO> unExecuteStatusDTOList = new ArrayList<>();

            for (TestCaseReportStatusResultDTO dto : oldData) {
                if (StringUtils.equalsIgnoreCase(dto.getStatus(), ApiReportStatus.FAKE_ERROR.toString())) {
                    fakeErrorStatusDTO = dto;
                } else if (StringUtils.equalsIgnoreCase(dto.getStatus(), ApiReportStatus.ERROR.toString())) {
                    errorStatusDTO = dto;
                } else if (StringUtils.equalsIgnoreCase(dto.getStatus(), ApiReportStatus.SUCCESS.toString())) {
                    successStatusDTO = dto;
                } else {
                    unExecuteStatusDTOList.add(dto);
                }
            }
            if (errorStatusDTO != null) {
                apiCaseData.add(errorStatusDTO);
            }
            if (fakeErrorStatusDTO != null) {
                apiCaseData.add(fakeErrorStatusDTO);
            }
            if (successStatusDTO != null) {
                apiCaseData.add(successStatusDTO);
            }
            apiCaseData.addAll(unExecuteStatusDTOList);

            apiResult.setApiCaseData(apiCaseData);
        }
    }

    public void editReport(TestPlanWithBLOBs testPlanWithBLOBs) {
        testPlanMapper.updateByPrimaryKeySelective(testPlanWithBLOBs);
    }

    public TestCaseReportStatusResultDTO getFunctionalResultReport(String planId) {
        return null;
    }

    public Map<String, List<String>> getPlanCaseEnv(String planId) {
        Map<String, List<String>> envMap = new HashMap<>();
        if (StringUtils.isBlank(planId)) {
            return envMap;
        }

        envMap = planTestPlanApiCaseService.getApiCaseEnv(planId);
        Map<String, List<String>> scenarioEnv = planTestPlanScenarioCaseService.getApiScenarioEnv(planId);

        if (DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            scenarioEnv = mergeUiScenarioEnv(planId, scenarioEnv);
        }

        Set<String> projectIds = scenarioEnv.keySet();
        for (String projectId : projectIds) {
            if (envMap.containsKey(projectId)) {
                List<String> apiList = envMap.get(projectId);
                List<String> scenarioList = scenarioEnv.get(projectId);
                List<String> result = Stream.of(apiList, scenarioList)
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(result)) {
                    envMap.put(projectId, result);
                }
            } else {
                if (CollectionUtils.isNotEmpty(scenarioEnv.get(projectId))) {
                    envMap.put(projectId, scenarioEnv.get(projectId));
                }
            }
        }

        return envMap;
    }

    /**
     * 合并ui场景的环境信息
     *
     * @param planId
     * @param scenarioEnv
     * @return
     */
    private Map<String, List<String>> mergeUiScenarioEnv(String planId, Map<String, List<String>> scenarioEnv) {
        Map<String, List<String>> uiScenarioEnv = planTestPlanUiScenarioCaseService.getUiScenarioEnv(planId);
        if (MapUtils.isEmpty(scenarioEnv)) {
            return uiScenarioEnv;
        }
        uiScenarioEnv.forEach((key, value) -> {
            if (scenarioEnv.containsKey(key)) {
                List<String> environmentIds = scenarioEnv.get(key);
                value.forEach(eId -> {
                    if (!environmentIds.contains(eId)) {
                        environmentIds.add(eId);
                    }
                });
            } else {
                scenarioEnv.put(key, value);
            }
        });
        return scenarioEnv;
    }

    public String runPlan(TestPlanRunRequest testplanRunRequest) {
        //检查测试计划下有没有可以执行的用例；
        if (!haveExecCase(testplanRunRequest.getTestPlanId(), false)) {
            MSException.throwException(Translator.get("plan_warning"));
        }
        String envType = testplanRunRequest.getEnvironmentType();
        Map<String, String> envMap = testplanRunRequest.getEnvMap();
        String environmentGroupId = testplanRunRequest.getEnvironmentGroupId();
        String testPlanId = testplanRunRequest.getTestPlanId();
        RunModeConfigDTO runModeConfig = getRunModeConfigDTO(testplanRunRequest, envType, envMap, environmentGroupId, testPlanId);
        runModeConfig.setTestPlanDefaultEnvMap(testplanRunRequest.getTestPlanDefaultEnvMap());

        //执行测试计划行为，要更新TestPlan状态为进行中，并重置实际结束时间
        this.updateTestPlanExecuteInfo(testPlanId, TestPlanStatus.Underway.name());

        String apiRunConfig = JSON.toJSONString(runModeConfig);
        return testPlanExecuteService.runTestPlan(testPlanId, testplanRunRequest.getProjectId(),
                testplanRunRequest.getUserId(), testplanRunRequest.getTriggerMode(), testplanRunRequest.getReportId(), testplanRunRequest.getExecutionWay(), apiRunConfig);
    }

    private void updateTestPlanExecuteInfo(String testPlanId, String status) {
        extTestPlanMapper.updateStatusAndActStartTimeAndSetActEndTimeNullById(testPlanId, System.currentTimeMillis(), status);
    }

    public RunModeConfigDTO getRunModeConfigDTO(TestPlanRunRequest testplanRunRequest, String
            envType, Map<String, String> envMap, String environmentGroupId, String testPlanId) {
        RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
        if (!testplanRunRequest.isRunWithinResourcePool()) {
            runModeConfig.setResourcePoolId(null);
        }
        runModeConfig.setEnvironmentType(testplanRunRequest.getEnvironmentType());
        if (StringUtils.equals(envType, "JSON") && !envMap.isEmpty()) {
            runModeConfig.setEnvMap(testplanRunRequest.getEnvMap());
            if (!StringUtils.equals(testplanRunRequest.getExecutionWay(), ExecutionWay.RUN.name())) {
                this.setPlanCaseEnv(testPlanId, runModeConfig);
            }
        } else if (StringUtils.equals(envType, "GROUP") && StringUtils.isNotBlank(environmentGroupId)) {
            runModeConfig.setEnvironmentGroupId(testplanRunRequest.getEnvironmentGroupId());
            if (!StringUtils.equals(testplanRunRequest.getExecutionWay(), ExecutionWay.RUN.name())) {
                this.setPlanCaseEnv(testPlanId, runModeConfig);
            }
        }
        runModeConfig.setMode(testplanRunRequest.getMode());
        runModeConfig.setResourcePoolId(testplanRunRequest.getResourcePoolId());
        runModeConfig.setOnSampleError(testplanRunRequest.isOnSampleError());
        if (StringUtils.isBlank(testplanRunRequest.getReportType())) {
            runModeConfig.setReportType("iddReport");
        } else {
            runModeConfig.setReportType(testplanRunRequest.getReportType());
        }
        runModeConfig.setRetryEnable(testplanRunRequest.isRetryEnable());
        runModeConfig.setRetryNum(testplanRunRequest.getRetryNum());
        runModeConfig.setBrowser(testplanRunRequest.getBrowser());
        runModeConfig.setHeadlessEnabled(testplanRunRequest.isHeadlessEnabled());
        return runModeConfig;
    }

    private void updatePlan(TestPlanRunRequest testplanRunRequest, String testPlanId) {
        String request = JSON.toJSONString(testplanRunRequest);
        TestPlanWithBLOBs testPlanWithBLOBs = testPlanMapper.selectByPrimaryKey(testPlanId);
        if (testPlanWithBLOBs.getRunModeConfig() == null || !(StringUtils.equals(request, testPlanWithBLOBs.getRunModeConfig()))) {
            testPlanWithBLOBs.setRunModeConfig(request);
            testPlanMapper.updateByPrimaryKeyWithBLOBs(testPlanWithBLOBs);
        }
    }

    public void setPlanCaseEnv(String planId, RunModeConfigDTO runModeConfig) {
        if (DiscoveryUtil.hasService(MicroServiceName.API_TEST)) {
            planTestPlanApiCaseService.setApiCaseEnv(planId, runModeConfig);
            planTestPlanScenarioCaseService.setScenarioEnv(planId, runModeConfig);
        }

        if (DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            planTestPlanUiScenarioCaseService.setScenarioEnv(planId, runModeConfig);
        }
    }

    public void editReportConfig(TestPlanDTO testPlanDTO) {
        TestPlanWithBLOBs testPlan = new TestPlanWithBLOBs();
        testPlan.setId(testPlanDTO.getId());
        testPlan.setReportConfig(testPlanDTO.getReportConfig());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    public boolean haveExecCase(String planId, boolean ignoreUI) {
        if (StringUtils.isBlank(planId)) {
            return false;
        }

        if (planTestPlanApiCaseService.haveExecCase(planId)) {
            return true;
        }

        if (planTestPlanScenarioCaseService.haveExecCase(planId)) {
            return true;
        }

        if (!ignoreUI) {
            if (planTestPlanUiScenarioCaseService.haveUiCase(planId)) {
                return true;
            }
        }

        return planTestPlanLoadCaseService.haveExecCase(planId);
    }

    public List<User> getPlanPrincipal(String planId) {
        List<User> result = new ArrayList<>();
        if (StringUtils.isBlank(planId)) {
            return result;
        }
        TestPlanPrincipalExample example = new TestPlanPrincipalExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanPrincipal> testPlanPrincipals = testPlanPrincipalMapper.selectByExample(example);
        List<String> userIds = testPlanPrincipals.stream().map(TestPlanPrincipal::getPrincipalId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return result;
        }
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        return userMapper.selectByExample(userExample);
    }

    public List<User> getPlanFollow(String planId) {
        List<User> result = new ArrayList<>();
        if (StringUtils.isBlank(planId)) {
            return result;
        }
        TestPlanFollowExample example = new TestPlanFollowExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanFollow> testPlanFollow = testPlanFollowMapper.selectByExample(example);
        List<String> userIds = testPlanFollow.stream().map(TestPlanFollow::getFollowId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return result;
        }
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        return userMapper.selectByExample(userExample);
    }

    public boolean isAllowedRepeatCase(String planId) {
        return testPlanMapper.selectByPrimaryKey(planId).getRepeatCase();
    }

    public List getStageOption(String projectId) {
        CustomField stageField = baseCustomFieldService.getCustomFieldByName(projectId, "测试阶段");
        return JSON.parseArray(stageField.getOptions());
    }

    public void batchUpdateScheduleEnable(ScheduleInfoRequest request) {
        List<String> scheduleIds = request.getTaskIds();
        if (request.isSelectAll()) {
            QueryTestPlanRequest queryTestPlanRequest = request.getQueryTestPlanRequest();
            request.getQueryTestPlanRequest().setOrders(ServiceUtils.getDefaultOrder(queryTestPlanRequest.getOrders()));
            if (StringUtils.isNotBlank(queryTestPlanRequest.getProjectId())) {
                request.getQueryTestPlanRequest().setProjectId(queryTestPlanRequest.getProjectId());
            }
            List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.list(queryTestPlanRequest);
            scheduleIds = testPlans.stream().filter(testPlan -> testPlan.getScheduleId() != null).map(testPlan -> testPlan.getScheduleId()).collect(Collectors.toList());
        }
        for (String id : scheduleIds) {
            Schedule schedule = baseScheduleService.getSchedule(id);
            schedule.setEnable(request.isEnable());
            updateSchedule(schedule);
        }
    }

    public long countScheduleEnableTotal(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        if (StringUtils.isNotBlank(request.getProjectId())) {
            request.setProjectId(request.getProjectId());
        }
        List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.list(request);
        return testPlans.stream().filter(testPlan -> testPlan.getScheduleId() != null).count();

    }

    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN_SCHEDULE, type = OperLogConstants.UPDATE, title = "#request.name",
            beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = BaseScheduleService.class)
    public Schedule updateSchedule(Schedule request) {
        //测试计划的定时任务修改会修改任务的配置信息，并不只是单纯的修改定时任务时间。需要重新配置这个定时任务
        JobKey jobKey = TestPlanTestJob.getJobKey(request.getResourceId());
        TriggerKey triggerKey = TestPlanTestJob.getTriggerKey(request.getResourceId());
        Class clazz = TestPlanTestJob.class;
        request.setJob(clazz.getName());
        baseScheduleService.editSchedule(request);
        baseScheduleService.resetJob(request, jobKey, triggerKey, clazz);
        return request;
    }

    public ScheduleDTO getNextTriggerSchedule(Schedule schedule) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        BeanUtils.copyBean(scheduleDTO, schedule);
        if (schedule.getEnable() != null && schedule.getEnable()) {
            scheduleDTO.setScheduleExecuteTime(getNextTriggerTime(schedule.getValue()));
        }
        return scheduleDTO;
    }

    public List<TestPlanDTOWithMetric> listByWorkspaceId(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extTestPlanMapper.list(request);
    }

    public void runBatch(TestPlanRunRequest request) {
        List<String> ids = request.getTestPlanIds();
        if (CollectionUtils.isEmpty(ids) && !request.getIsAll()) {
            return;
        }
        LoggerUtil.info("开始查询测试计划");
        List<TestPlanWithBLOBs> planList = new ArrayList<>();
        if (request.getIsAll() != null && request.getIsAll()) {
            List<TestPlanDTOWithMetric> testPlanDTOWithMetrics = this.listTestPlan(request.getQueryTestPlanRequest());
            planList.addAll(testPlanDTOWithMetrics);
        } else {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(ids);
            example.createCriteria().andProjectIdEqualTo(request.getProjectId());
            planList = testPlanMapper.selectByExampleWithBLOBs(example);
        }

        Map<String, TestPlanWithBLOBs> testPlanMap = planList.stream().collect(Collectors.toMap(TestPlan::getId, a -> a, (k1, k2) -> k1));
        Map<String, String> executeQueue = new LinkedHashMap<>();

        StringBuilder stringBuilder = new StringBuilder();
        //检查测试计划下是否有可执行的用例
        StringBuilder haveExecCaseBuilder = new StringBuilder();
        for (int i = 0; i < planList.size(); i++) {
            if (StringUtils.isBlank(planList.get(i).getRunModeConfig())) {
                StringBuilder append = stringBuilder.append("请保存[").append(planList.get(i).getName()).append("]的运行配置");
                if (i <= planList.size() - 2) {
                    append.append("/");
                }
            }
            if (!haveExecCase(planList.get(i).getId(), false)) {
                haveExecCaseBuilder.append(planList.get(i).getName()).append("; ");
            }
        }

        if (StringUtils.isNotEmpty(stringBuilder)) {
            MSException.throwException(stringBuilder.toString());
        }

        if (StringUtils.isNotEmpty(haveExecCaseBuilder)) {
            MSException.throwException(Translator.get("track_test_plan") + ": " + haveExecCaseBuilder.toString() + ": " + Translator.get("plan_warning"));
        }

        for (String id : ids) {
            TestPlanWithBLOBs testPlan = testPlanMap.get(id);

            String planReportId = UUID.randomUUID().toString();

            //检查资源池运行情况
            RunModeConfigDTO runModeConfigDTO = JSON.parseObject(testPlan.getRunModeConfig(), RunModeConfigDTO.class);
            runModeConfigDTO = ObjectUtils.isEmpty(runModeConfigDTO) ? new RunModeConfigDTO() : runModeConfigDTO;
            if (haveExecCase(testPlan.getId(), true)) {
                this.verifyPool(testPlan.getProjectId(), runModeConfigDTO);
            }
            //测试计划准备执行，取消测试计划的实际结束时间
            extTestPlanMapper.updateStatusAndActStartTimeAndSetActEndTimeNullById(testPlan.getId(), System.currentTimeMillis(), TestPlanStatus.Underway.name());
            executeQueue.put(testPlan.getId(), planReportId);
        }

        LoggerUtil.info("开始生成测试计划队列");

        //生成测试计划队列
        List<TestPlanExecutionQueue> planExecutionQueues = getTestPlanExecutionQueues(request, executeQueue);
        //串行存储队列，并行在目前的业务需求下不需要存储
        if (CollectionUtils.isNotEmpty(planExecutionQueues) && StringUtils.equalsIgnoreCase(request.getMode(), "serial")) {
            testPlanExecutionQueueService.batchSave(planExecutionQueues);
        }
        // 开始选择执行模式
        runByMode(request, testPlanMap, planExecutionQueues);
    }


    private List<TestPlanExecutionQueue> getTestPlanExecutionQueues(TestPlanRunRequest
                                                                            request, Map<String, String> executeQueue) {
        List<TestPlanExecutionQueue> planExecutionQueues = new ArrayList<>();
        String resourceId = UUID.randomUUID().toString();
        final int[] nextNum = {testPlanExecutionQueueService.getNextNum(resourceId)};
        executeQueue.forEach((k, v) -> {
            TestPlanExecutionQueue executionQueue = new TestPlanExecutionQueue();
            executionQueue.setId(UUID.randomUUID().toString());
            executionQueue.setCreateTime(System.currentTimeMillis());
            executionQueue.setReportId(v);
            executionQueue.setTestPlanId(k);
            executionQueue.setRunMode(request.getMode());
            executionQueue.setResourceId(resourceId);
            executionQueue.setNum(nextNum[0]);
            executionQueue.setExecuteUser(request.getUserId());
            nextNum[0]++;
            planExecutionQueues.add(executionQueue);
        });
        return planExecutionQueues;
    }

    private void runByMode(TestPlanRunRequest
                                   request, Map<String, TestPlanWithBLOBs> testPlanMap, List<TestPlanExecutionQueue> planExecutionQueues) {
        if (CollectionUtils.isNotEmpty(planExecutionQueues)) {
            if (StringUtils.equalsIgnoreCase(request.getMode(), RunModeConstants.SERIAL.name())) {
                TestPlanExecutionQueue planExecutionQueue = planExecutionQueues.get(0);
                TestPlanWithBLOBs testPlan = testPlanMap.get(planExecutionQueue.getTestPlanId());
                Map jsonObject = JSON.parseMap(testPlan.getRunModeConfig());
                TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                TestPlanRunRequest runRequest = JSON.parseObject(JSON.toJSONString(jsonObject), TestPlanRunRequest.class);
                if (StringUtils.isNotBlank(planExecutionQueue.getExecuteUser())) {
                    runRequest.setUserId(planExecutionQueue.getExecuteUser());
                }
                runRequest.setTestPlanId(planExecutionQueue.getTestPlanId());
                runRequest.setReportId(planExecutionQueue.getReportId());
                runRequest.setTriggerMode(request.getTriggerMode());
                runPlan(runRequest);
            } else {
                for (TestPlanExecutionQueue planExecutionQueue : planExecutionQueues) {
                    TestPlanWithBLOBs testPlan = testPlanMap.get(planExecutionQueue.getTestPlanId());
                    Map jsonObject = JSON.parseMap(testPlan.getRunModeConfig());
                    TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                    TestPlanRunRequest runRequest = JSON.parseObject(JSON.toJSONString(jsonObject), TestPlanRunRequest.class);
                    runRequest.setReportId(planExecutionQueue.getReportId());
                    runRequest.setTriggerMode(request.getTriggerMode());
                    runPlan(runRequest);
                }
            }
        }
    }

    public void updateRunModeConfig(TestPlanRunRequest testplanRunRequest) {
        String testPlanId = testplanRunRequest.getTestPlanId();
        updatePlan(testplanRunRequest, testPlanId);
        RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
        if (!testplanRunRequest.isRunWithinResourcePool()) {
            runModeConfig.setResourcePoolId(null);
        }
        runModeConfig.setEnvironmentType(testplanRunRequest.getEnvironmentType());
        if (StringUtils.equals(testplanRunRequest.getEnvironmentType(), "JSON") && !testplanRunRequest.getEnvMap().isEmpty()) {
            runModeConfig.setEnvMap(testplanRunRequest.getEnvMap());
            this.setPlanCaseEnv(testPlanId, runModeConfig);
        } else if (StringUtils.equals(testplanRunRequest.getEnvironmentType(), "GROUP") && StringUtils.isNotBlank(testplanRunRequest.getEnvironmentGroupId())) {
            runModeConfig.setEnvironmentGroupId(testplanRunRequest.getEnvironmentGroupId());
            this.setPlanCaseEnv(testPlanId, runModeConfig);
        }
    }

    public boolean haveUiCase(String planId) {
        if (!DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            return false;
        }
        return planTestPlanUiScenarioCaseService.haveUiCase(planId);
    }

    public void createSchedule(ScheduleRequest request) {
        Schedule schedule = baseScheduleService.buildApiTestSchedule(request);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getResourceId());
        schedule.setName(testPlan.getName());
        schedule.setProjectId(testPlan.getProjectId());
        schedule.setGroup(ScheduleGroup.TEST_PLAN_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setConfig(request.getConfig());
        JobKey jobKey = TestPlanTestJob.getJobKey(request.getResourceId());
        TriggerKey triggerKey = TestPlanTestJob.getTriggerKey(request.getResourceId());
        Class clazz = TestPlanTestJob.class;
        schedule.setJob(TestPlanTestJob.class.getName());

        baseScheduleService.addSchedule(schedule);
        baseScheduleService.addOrUpdateCronJob(request, jobKey, triggerKey, clazz);
    }

    public void resetStatus(String planId) {
        TestPlan testPlan = get(planId);
        if (StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                || StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Completed.name())) {
            testPlan.setStatus(TestPlanStatus.Underway.name());
            testPlan.setActualStartTime(System.currentTimeMillis());  // 将状态更新为进行中时，开始时间也要更新
            testPlan.setActualEndTime(null);
            testPlanMapper.updateByPrimaryKey(testPlan);
        }
    }

    public void deleteTestPlanBatch(BatchOperateRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            QueryTestPlanRequest queryTestPlanRequest = request.getQueryTestPlanRequest();
            request.getQueryTestPlanRequest().setOrders(ServiceUtils.getDefaultOrder(queryTestPlanRequest.getOrders()));
            if (StringUtils.isNotBlank(queryTestPlanRequest.getProjectId())) {
                request.getQueryTestPlanRequest().setProjectId(queryTestPlanRequest.getProjectId());
            }
            List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.list(queryTestPlanRequest);
            ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
            if (request.getUnSelectIds() != null) {
                ids.removeAll(request.getUnSelectIds());
            }
        }
        if (testPlanReportService.hasRunningReport(ids)) {
            MSException.throwException(Translator.get("test_plan_delete_exec_error"));
        }
        this.deleteTestPlans(ids);
    }

    public List<String> getRelevanceProjectIds(String planId) {
        List<String> projectIds = new ArrayList<>();
        List<String> apiCaseProjectIds = planTestPlanApiCaseService.getApiCaseProjectIds(planId);
        List<String> apiScenarioProjectIds = planTestPlanScenarioCaseService.getApiScenarioEnvProjectIds(planId);
        if (DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            List<String> uiScenarioProjectIds = planTestPlanUiScenarioCaseService.getUiScenarioEnvProjectIds(planId);
            projectIds.addAll(uiScenarioProjectIds);
        }
        projectIds.addAll(apiCaseProjectIds);
        projectIds.addAll(apiScenarioProjectIds);
        return projectIds.stream().distinct().collect(Collectors.toList());
    }

    public List<String> getRelevanceProjectIdsByCaseType(String planId, String caseType) {
        if (StringUtils.equalsIgnoreCase(caseType, "apiCase")) {
            return planTestPlanApiCaseService.getApiCaseProjectIds(planId);
        } else if (StringUtils.equalsIgnoreCase(caseType, "apiScenario")) {
            return planTestPlanScenarioCaseService.getApiScenarioProjectIds(planId);
        } else if (StringUtils.equalsIgnoreCase(caseType, "uiScenario") && DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            return planTestPlanUiScenarioCaseService.getUiScenarioProjectIds(planId);
        } else {
            return new ArrayList<>();
        }
    }

    public TestPlanReportDataStruct buildOldVersionTestPlanReport(TestPlanReport
                                                                          testPlanReport, TestPlanReportContentWithBLOBs testPlanReportContent) {
        TestPlanWithBLOBs testPlanWithBLOBs = this.testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
        TestPlanReportDataStruct testPlanReportDataStruct = new TestPlanReportDataStruct();
        try {
            testPlanReportDataStruct = this.generateReportStruct(testPlanWithBLOBs, testPlanReport, testPlanReportContent, false);
            if (testPlanReportContent != null && StringUtils.isBlank(testPlanReportContent.getApiBaseCount())
                    && !testPlanReportDataStruct.hasRunningCase()
                    && StringUtils.equalsAnyIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.FAILED.name(), TestPlanReportStatus.COMPLETED.name(), TestPlanReportStatus.SUCCESS.name())) {
                //旧版本的测试计划报告，没有重新统计过测试计划报告时，且当不存在运行中的用例，会将结果保存下来
                testPlanReportService.updateReportStructInfo(testPlanReportContent, testPlanReportDataStruct);
            }
        } catch (Exception e) {
            LoggerUtil.error("统计测试计划数据出错！", e);
        }
        return testPlanReportDataStruct;
    }

    //这个方法是为定时任务调用的。
    public String runTestPlanBySchedule(String testPlanId, String projectId, String userId, String triggerMode, String planReportId, String executionWay, String apiRunConfig) {
        //定时任务执行重新设置实际开始时间
        if (StringUtils.equals(triggerMode, TriggerMode.SCHEDULE.name())) {
            extTestPlanMapper.updateStatusAndActStartTimeAndSetActEndTimeNullById(testPlanId, System.currentTimeMillis(), TestPlanStatus.Underway.name());
        }
        return testPlanExecuteService.runTestPlan(testPlanId, projectId, userId, triggerMode, planReportId, executionWay, apiRunConfig);
    }

    public TestPlanWithBLOBs selectAndChangeTestPlanExecuteInfo(String testPlanId) {
        //更新TestPlan状态为完成
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        if (testPlan != null
                && !StringUtils.equalsAny(testPlan.getStatus(), TestPlanStatus.Completed.name(), TestPlanStatus.Finished.name())) {
            testPlan.setStatus(calcTestPlanStatusWithPassRate(testPlan));
            this.editTestPlan(testPlan);
        }
        return testPlan;
    }

    private String calcTestPlanStatusWithPassRate(TestPlanWithBLOBs testPlan) {
        try {
            int fullMarks = 100;

            // 计算通过率
            TestPlanDTOWithMetric testPlanDTOWithMetric = BeanUtils.copyBean(new TestPlanDTOWithMetric(), testPlan);
            testPlanService.calcTestPlanRate(Collections.singletonList(testPlanDTOWithMetric));
            //测试进度
            Double testRate = Optional.ofNullable(testPlanDTOWithMetric.getTestRate()).orElse(0.0);
            //通过率
            Double passRate = Optional.ofNullable(testPlanDTOWithMetric.getPassRate()).orElse(0.0);

            // 已完成：测试进度=100% 且 通过率=100%
            if (testRate >= fullMarks && passRate >= fullMarks) {
                return TestPlanStatus.Completed.name();
            }

            // 已结束：超过了计划结束时间（如有） 或 测试进度=100% 且 通过率非100%
            Long plannedEndTime = testPlan.getPlannedEndTime();
            long currentTime = System.currentTimeMillis();
            if (Objects.nonNull(plannedEndTime) && currentTime >= plannedEndTime) {
                return TestPlanStatus.Finished.name();
            }

            if (testRate >= fullMarks && passRate < fullMarks) {
                return TestPlanStatus.Finished.name();
            }

        } catch (Exception e) {
            LogUtil.error("计算通过率失败！", e);
        }

        // 进行中：0 < 测试进度 < 100%
        return TestPlanStatus.Underway.name();
    }

    public boolean checkTestPlanIsRunning(String testPlanId) {
        String status = testPlanReportService.selectLastReportByTestPlanId(testPlanId);
        return StringUtils.equalsIgnoreCase(status, TestPlanReportStatus.RUNNING.name());
    }
}
