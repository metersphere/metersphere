package io.metersphere.plan.service;


import com.google.gson.Gson;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.excel.constants.TestPlanTestCaseStatus;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestPlanReference;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.job.TestPlanTestJob;
import io.metersphere.plan.reuest.AddTestPlanRequest;
import io.metersphere.plan.reuest.QueryTestPlanRequest;
import io.metersphere.plan.reuest.ScheduleInfoRequest;
import io.metersphere.plan.reuest.api.ApiPlanReportRequest;
import io.metersphere.plan.reuest.api.RunScenarioRequest;
import io.metersphere.plan.reuest.api.SchedulePlanScenarioExecuteRequest;
import io.metersphere.plan.reuest.api.TestPlanRunRequest;
import io.metersphere.plan.reuest.function.PlanCaseRelevanceRequest;
import io.metersphere.plan.reuest.function.QueryTestPlanCaseRequest;
import io.metersphere.plan.reuest.performance.LoadPlanReportDTO;
import io.metersphere.plan.reuest.ui.RunUiScenarioRequest;
import io.metersphere.plan.service.remote.api.PlanApiAutomationService;
import io.metersphere.plan.service.remote.api.PlanTestPlanApiCaseService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.plan.service.remote.performance.PerfExecService;
import io.metersphere.plan.service.remote.performance.PlanTestPlanLoadCaseService;
import io.metersphere.plan.service.remote.ui.PlanTestPlanUiScenarioCaseService;
import io.metersphere.plan.service.remote.ui.PlanUiAutomationService;
import io.metersphere.plan.utils.TestPlanRequestUtil;
import io.metersphere.request.ScheduleRequest;
import io.metersphere.service.*;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xpack.track.dto.IssuesDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
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
    private ExtTestPlanReportMapper extTestPlanReportMapper;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Lazy
    @Resource
    private IssuesService issuesService;
    @Resource
    private PerfExecService perfExecService;
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

    public synchronized TestPlan addTestPlan(AddTestPlanRequest testPlan) {
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

        int i;
        if (testPlan.getName() == null) {//  若是点击该测试计划，则仅更新了updateTime，其它字段全为null，使用updateByPrimaryKeySelective
            i = testPlanMapper.updateByPrimaryKeySelective(testPlan);
        } else {  //  有修改字段的调用，为保证将某些时间置null的情况，使用updateByPrimaryKey
            baseScheduleService.updateNameByResourceID(testPlan.getId(), testPlan.getName());//   同步更新该测试的定时任务的name
            i = testPlanMapper.updateByPrimaryKeyWithBLOBs(testPlan); //  更新
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

        // 发送删除通知
        kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC, planId);

        testPlanPrincipalService.deleteTestPlanPrincipalByPlanId(planId);
        testPlanFollowService.deleteTestPlanFollowByPlanId(planId);
        deleteTestCaseByPlanId(planId);

        testPlanReportService.deleteByPlanId(planId);

        //删除定时任务
        baseScheduleService.deleteByResourceId(planId, ScheduleGroup.TEST_PLAN_TEST.name());

        return testPlanMapper.deleteByPrimaryKey(planId);
    }

    public void deleteTestCaseByPlanId(String testPlanId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(testPlanId);
        testPlanTestCaseMapper.deleteByExample(testPlanTestCaseExample);
    }

    public void calcTestPlanRate(List<TestPlanDTOWithMetric> testPlans) {
        // 速度太慢 todo
        testPlans.forEach(testPlan -> calcTestPlanRate(testPlan));
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

        calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanApiCaseService::getExecResultByPlanId);

        calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanScenarioCaseService::getExecResultByPlanId);

        calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanLoadCaseService::getExecResultByPlanId);

        List<String> uiScenarioExecResults = calcExecResultStatus(testPlan.getId(), testPlan, planTestPlanUiScenarioCaseService::getExecResultByPlanId);
        uiScenarioExecResults.forEach(item -> {
            if (StringUtils.isNotBlank(item)) {
                testPlan.setTested(testPlan.getTested() + 1);
                if (StringUtils.equals(item, "Success")) {
                    testPlan.setPassed(testPlan.getPassed() + 1);
                }
            }
        });

        testPlan.setPassRate(MathUtils.getPercentWithDecimal(testPlan.getTested() == 0 ? 0 : testPlan.getPassed() * 1.0 / testPlan.getTotal()));
        testPlan.setTestRate(MathUtils.getPercentWithDecimal(testPlan.getTotal() == 0 ? 0 : testPlan.getTested() * 1.0 / testPlan.getTotal()));
    }

    /**
     * 出现异常不回滚，继续计算其他
     *
     * @param planId
     * @return
     */
    List<String> calcExecResultStatus(String planId, TestPlanDTOWithMetric testPlan, Function<String, List<String>> getResultFunc) {
        try {
            List<String> execResults = getResultFunc.apply(planId);
            execResults.forEach(item -> {
                if (StringUtils.isNotBlank(item)) {
                    testPlan.setTested(testPlan.getTested() + 1);
                    if (StringUtils.equalsIgnoreCase(item, "Success")) {
                        testPlan.setPassed(testPlan.getPassed() + 1);
                    }
                }
            });
            testPlan.setTotal(testPlan.getTotal() + execResults.size());
        } catch (MSException e) {
            LogUtil.error(e);
        }
        return new ArrayList<>();
    }

    public List<TestPlanDTOWithMetric> listTestPlan(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        if (StringUtils.isNotBlank(request.getProjectId())) {
            request.setProjectId(request.getProjectId());
        }
        List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.list(request);
        if (testPlans.size() == 0) {
            return new ArrayList<>();
        }
        Set<String> ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toSet());
        Map<String, ParamsDTO> planTestCaseCountMap = extTestPlanMapper.testPlanTestCaseCount(ids);
        Map<String, ParamsDTO> planApiCaseMap = extTestPlanMapper.testPlanApiCaseCount(ids);
        Map<String, ParamsDTO> planApiScenarioMap = extTestPlanMapper.testPlanApiScenarioCount(ids);
        Map<String, ParamsDTO> planUiScenarioMap = extTestPlanMapper.testPlanUiScenarioCount(ids);
        Map<String, ParamsDTO> planLoadCaseMap = extTestPlanMapper.testPlanLoadCaseCount(ids);
        ArrayList<String> idList = new ArrayList<>(ids);
        List<Schedule> scheduleByResourceIds = baseScheduleService.getScheduleByResourceIds(idList, ScheduleGroup.TEST_PLAN_TEST.name());
        Map<String, Schedule> scheduleMap = scheduleByResourceIds.stream().collect(Collectors.toMap(Schedule::getResourceId, Schedule -> Schedule));
        Map<String, ParamsDTO> stringParamsDTOMap = extTestPlanReportMapper.reportCount(ids);

        testPlans.forEach(item -> {
            item.setExecutionTimes(stringParamsDTOMap.get(item.getId()) == null ? 0 : Integer.parseInt(stringParamsDTOMap.get(item.getId()).getValue() == null ? "0" : stringParamsDTOMap.get(item.getId()).getValue()));
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
            item.setTestPlanTestCaseCount(planTestCaseCountMap.get(item.getId()) == null ? 0 : Integer.parseInt(planTestCaseCountMap.get(item.getId()).getValue() == null ? "0" : planTestCaseCountMap.get(item.getId()).getValue()));
            item.setTestPlanApiCaseCount(planApiCaseMap.get(item.getId()) == null ? 0 : Integer.parseInt(planApiCaseMap.get(item.getId()).getValue() == null ? "0" : planApiCaseMap.get(item.getId()).getValue()));
            item.setTestPlanApiScenarioCount(planApiScenarioMap.get(item.getId()) == null ? 0 : Integer.parseInt(planApiScenarioMap.get(item.getId()).getValue() == null ? "0" : planApiScenarioMap.get(item.getId()).getValue()));
            item.setTestPlanUiScenarioCount(planUiScenarioMap.get(item.getId()) == null ? 0 : Integer.parseInt(planUiScenarioMap.get(item.getId()).getValue() == null ? "0" : planUiScenarioMap.get(item.getId()).getValue()));
            item.setTestPlanLoadCaseCount(planLoadCaseMap.get(item.getId()) == null ? 0 : Integer.parseInt(planLoadCaseMap.get(item.getId()).getValue() == null ? "0" : planLoadCaseMap.get(item.getId()).getValue()));
        });

        calcTestPlanRate(testPlans);
        return testPlans;
    }

    public void checkStatus(String testPlanId) { //  检查执行结果，自动更新计划状态
        List<String> statusList = new ArrayList<>();
        statusList.addAll(extTestPlanTestCaseMapper.getExecResultByPlanId(testPlanId));
        try {
            statusList.addAll(planTestPlanApiCaseService.getExecResultByPlanId(testPlanId));
            statusList.addAll(planTestPlanScenarioCaseService.getExecResultByPlanId(testPlanId));
            statusList.addAll(planTestPlanLoadCaseService.getExecResultByPlanId(testPlanId));
            statusList.addAll(planTestPlanUiScenarioCaseService.getExecResultByPlanId(testPlanId));
        } catch (MSException e) {
            LogUtil.error(e);
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
        if (passNum == statusList.size()) {   //  全部通过
            testPlanWithBLOBs.setStatus(TestPlanStatus.Completed.name());
            this.editTestPlan(testPlanWithBLOBs);
        } else if (prepareNum == 0 && passNum + failNum == statusList.size()) {  //  已结束
            if (testPlanWithBLOBs.getPlannedEndTime() != null && testPlanWithBLOBs.getPlannedEndTime() > System.currentTimeMillis()) {
                testPlanWithBLOBs.setStatus(TestPlanStatus.Completed.name());
            } else {
                testPlanWithBLOBs.setStatus(TestPlanStatus.Finished.name());
            }
            editTestPlan(testPlanWithBLOBs);
        } else if (prepareNum != 0) {    //  进行中
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

        boolean isSelectAll = request.getRequest() != null && request.getRequest().isSelectAll();
        if (isSelectAll) {
            List<TestCase> maintainerMap = extTestCaseMapper.getMaintainerMap(request.getRequest());
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

        Long nextOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanTestCaseMapper::getLastOrder);
        for (String caseId : testCaseIds) {
            TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
            testPlanTestCase.setId(UUID.randomUUID().toString());
            testPlanTestCase.setCreateUser(SessionUtils.getUserId());
            String maintainer = userMap.get(caseId);
            if (StringUtils.isBlank(maintainer) || !projectMemberSet.contains(maintainer)) {
                maintainer = SessionUtils.getUserId();
            }
            testPlanTestCase.setExecutor(maintainer);
            testPlanTestCase.setCaseId(caseId);
            testPlanTestCase.setCreateTime(System.currentTimeMillis());
            testPlanTestCase.setUpdateTime(System.currentTimeMillis());
            testPlanTestCase.setPlanId(request.getPlanId());
            testPlanTestCase.setStatus(TestPlanStatus.Prepare.name());
            testPlanTestCase.setIsDel(false);
            testPlanTestCase.setOrder(nextOrder);
            nextOrder += ServiceUtils.ORDER_STEP;
            batchMapper.insert(testPlanTestCase);
        }

        sqlSession.flushStatements();

        caseTestRelevance(request, testCaseIds);

        if (StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                || StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Completed.name())) {
            testPlan.setStatus(TestPlanStatus.Underway.name());
            testPlan.setActualStartTime(System.currentTimeMillis());  // 将状态更新为进行中时，开始时间也要更新
            testPlan.setActualEndTime(null);
            testPlanMapper.updateByPrimaryKey(testPlan);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void caseTestRelevance(PlanCaseRelevanceRequest request, List<String> testCaseIds) {

        //同步添加关联的接口和测试用例
        if (request.getChecked()) {
            if (!testCaseIds.isEmpty()) {
                testCaseIds.forEach(caseId -> {
                    List<TestCaseTest> list = new ArrayList<>();
                    TestCaseTestExample examp = new TestCaseTestExample();
                    examp.createCriteria().andTestCaseIdEqualTo(caseId);
                    if (testCaseTestMapper.countByExample(examp) > 0) {
                        list = testCaseTestMapper.selectByExample(examp);
                    }

                    List<String> apiCaseIds = new ArrayList<>();
                    List<String> scenarioIds = new ArrayList<>();
                    List<String> performanceIds = new ArrayList<>();

                    for (TestCaseTest l : list) {
                        if (StringUtils.equals(l.getTestType(), TestCaseTestStatus.performance.name())) {
                            performanceIds.add(l.getTestId());
                        }
                        if (StringUtils.equals(l.getTestType(), TestCaseTestStatus.api.name())) {
                            apiCaseIds.add(l.getTestId());
                        }
                        if (StringUtils.equals(l.getTestType(), TestCaseTestStatus.automation.name())) {
                            scenarioIds.add(l.getTestId());
                        }
                    }
                    try {
                        relevanceTestCaseTest(apiCaseIds, request.getPlanId(), planTestPlanApiCaseService::relevanceByTestIds);
                        relevanceTestCaseTest(scenarioIds, request.getPlanId(), planTestPlanScenarioCaseService::relevanceByTestIds);
                        relevanceTestCaseTest(performanceIds, request.getPlanId(), planTestPlanLoadCaseService::relevanceByTestIds);
                    } catch (MSException e) {
                        LogUtil.error(e);
                    }
                });
            }
        }
    }

    public void relevanceTestCaseTest(List<String> ids, String planId, BiConsumer<List<String>, String> relevanceByTestIds) {
        try {
            relevanceByTestIds.accept(ids, planId);
        } catch (MSException e) {
            LogUtil.error(e);
        }
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

    public List<MsExecResponseDTO> scenarioRunModeConfig(SchedulePlanScenarioExecuteRequest planScenarioExecuteRequest) {
        Map<String, Map<String, String>> testPlanScenarioIdMap = planScenarioExecuteRequest.getTestPlanScenarioIDMap();
        List<MsExecResponseDTO> list = new LinkedList<>();
        for (Map.Entry<String, Map<String, String>> entry : testPlanScenarioIdMap.entrySet()) {
            Map<String, String> scenarioMap = entry.getValue();

            RunScenarioRequest request = new RunScenarioRequest();
            request.setReportId(planScenarioExecuteRequest.getReportId());
            request.setEnvironmentId(planScenarioExecuteRequest.getEnvironmentId());
            request.setTriggerMode(planScenarioExecuteRequest.getTriggerMode());
            request.setExecuteType(planScenarioExecuteRequest.getExecuteType());
            request.setRunMode(planScenarioExecuteRequest.getRunMode());
            request.setIds(new ArrayList<>(scenarioMap.values()));//场景IDS
            request.setReportUserID(planScenarioExecuteRequest.getReportUserID());
            request.setScenarioTestPlanIdMap(scenarioMap);//未知
            request.setConfig(planScenarioExecuteRequest.getConfig());
            request.setTestPlanScheduleJob(true);
            request.setTestPlanReportId(planScenarioExecuteRequest.getTestPlanReportId());
            request.setId(UUID.randomUUID().toString());
            request.setProjectId(planScenarioExecuteRequest.getProjectId());
            request.setRequestOriginator("TEST_PLAN");
            request.setPlanCaseIds(new ArrayList<>(testPlanScenarioIdMap.keySet()));
            if ("api".equalsIgnoreCase(planScenarioExecuteRequest.getType())) {
                list.addAll(planApiAutomationService.run(request));
            } else {
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
    TestPlanScheduleReportInfoDTO genTestPlanReport(String planReportId, String planId, String userId, String triggerMode, RunModeConfigDTO runModeConfigDTO) {
        TestPlanScheduleReportInfoDTO reportInfoDTO = testPlanReportService.genTestPlanReportBySchedule(planReportId, planId, userId, triggerMode, runModeConfigDTO);
        return reportInfoDTO;
    }

    public String run(String testPlanID, String projectID, String userId, String triggerMode, String planReportId, String apiRunConfig) {
        RunModeConfigDTO runModeConfig = null;
        try {
            runModeConfig = JSON.parseObject(apiRunConfig, RunModeConfigDTO.class);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (runModeConfig == null) {
            runModeConfig = buildRunModeConfigDTO();
        }

        //环境参数为空时，依据测试计划保存的环境执行
        if ((StringUtils.equals("GROUP", runModeConfig.getEnvironmentType()) && StringUtils.isBlank(runModeConfig.getEnvironmentGroupId()))
                || (!StringUtils.equals("GROUP", runModeConfig.getEnvironmentType()) && MapUtils.isEmpty(runModeConfig.getEnvMap()))) {
            TestPlanWithBLOBs testPlanWithBLOBs = testPlanMapper.selectByPrimaryKey(testPlanID);
            if (StringUtils.isNotEmpty(testPlanWithBLOBs.getRunModeConfig())) {
                try {
                    Map json = JSON.parseMap(testPlanWithBLOBs.getRunModeConfig());
                    TestPlanRequestUtil.changeStringToBoolean(json);
                    TestPlanRunRequest testPlanRunRequest = JSON.parseObject(JSON.toJSONString(json), TestPlanRunRequest.class);
                    if (testPlanRunRequest != null) {
                        String envType = testPlanRunRequest.getEnvironmentType();
                        Map<String, String> envMap = testPlanRunRequest.getEnvMap();
                        String environmentGroupId = testPlanRunRequest.getEnvironmentGroupId();
                        runModeConfig = getRunModeConfigDTO(testPlanRunRequest, envType, envMap, environmentGroupId, testPlanID);
                        if (!testPlanRunRequest.isRunWithinResourcePool()) {
                            runModeConfig.setResourcePoolId(null);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("获取测试计划保存的环境信息出错!", e);
                }
            }
        }
        if (planReportId == null) {
            planReportId = UUID.randomUUID().toString();
        }

        //创建测试报告，然后返回的ID重新赋值为resourceID，作为后续的参数
        TestPlanScheduleReportInfoDTO reportInfoDTO = this.genTestPlanReport(planReportId, testPlanID, userId, triggerMode, runModeConfig);
        //测试计划准备执行，取消测试计划的实际结束时间
        extTestPlanMapper.updateActualEndTimeIsNullById(testPlanID);

        LoggerUtil.info("预生成测试计划报告【" + reportInfoDTO.getTestPlanReport() != null ? reportInfoDTO.getTestPlanReport().getName() : StringUtils.EMPTY + "】计划报告ID[" + planReportId + "]");

        Map<String, String> apiCaseReportMap = null;
        Map<String, String> scenarioReportMap = null;
        Map<String, String> loadCaseReportMap = null;
        Map<String, String> uiScenarioReportMap = null;
        if (reportInfoDTO.getApiTestCaseDataMap() != null) {
            //执行接口案例任务
            LoggerUtil.info("开始执行测试计划接口用例 " + planReportId);
            apiCaseReportMap = this.executeApiTestCase(triggerMode, planReportId, userId, new ArrayList<>(reportInfoDTO.getApiTestCaseDataMap().keySet()), runModeConfig);
        }
        if (reportInfoDTO.getPlanScenarioIdMap() != null) {
            //执行场景执行任务
            LoggerUtil.info("开始执行测试计划场景用例 " + planReportId);
            scenarioReportMap = this.executeScenarioCase(planReportId, testPlanID, projectID, runModeConfig, triggerMode, userId, reportInfoDTO.getPlanScenarioIdMap());
        }

        if (reportInfoDTO.getPerformanceIdMap() != null) {
            //执行性能测试任务
            LoggerUtil.info("开始执行测试计划性能用例 " + planReportId);
            loadCaseReportMap = perfExecService.executeLoadCase(planReportId, runModeConfig, transformationPerfTriggerMode(triggerMode), reportInfoDTO.getPerformanceIdMap());
        }

        if (reportInfoDTO.getUiScenarioIdMap() != null) {
            //执行UI场景执行任务
            LoggerUtil.info("开始执行测试计划 UI 场景用例 " + planReportId);
            uiScenarioReportMap = this.executeUiScenarioCase(planReportId, testPlanID, projectID, runModeConfig, triggerMode, userId, reportInfoDTO.getUiScenarioIdMap());
        }

//        if (apiCaseReportMap != null && scenarioReportMap != null && loadCaseReportMap != null && uiScenarioReportMap != null) todo 需要判断？
        LoggerUtil.info("开始生成测试计划报告内容 " + planReportId);
        testPlanReportService.createTestPlanReportContentReportIds(planReportId, apiCaseReportMap, scenarioReportMap, loadCaseReportMap, uiScenarioReportMap);

        return planReportId;
    }

    /**
     * 将测试计划运行时的triggerMode转化为性能测试中辨别更明确的值
     *
     * @param triggerMode
     * @return
     */
    private String transformationPerfTriggerMode(String triggerMode) {
        if (StringUtils.equalsIgnoreCase(triggerMode, ReportTriggerMode.SCHEDULE.name())) {
            return ReportTriggerMode.TEST_PLAN_SCHEDULE.name();
        } else if (StringUtils.equalsIgnoreCase(triggerMode, ReportTriggerMode.API.name())) {
            return ReportTriggerMode.TEST_PLAN_API.name();
        } else {
            return triggerMode;
        }
    }

    private RunModeConfigDTO buildRunModeConfigDTO() {
        RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
        runModeConfig.setMode(RunModeConstants.SERIAL.name());
        runModeConfig.setReportType("iddReport");
        runModeConfig.setEnvMap(new HashMap<>());
        runModeConfig.setOnSampleError(false);
        return runModeConfig;
    }

    private Map<String, String> executeApiTestCase(String triggerMode, String planReportId, String userId, List<String> planCaseIds, RunModeConfigDTO runModeConfig) {
        BatchRunDefinitionRequest request = new BatchRunDefinitionRequest();
        request.setTriggerMode(triggerMode);
        request.setPlanIds(planCaseIds);
        request.setPlanReportId(planReportId);
        request.setConfig(runModeConfig);
        request.setUserId(userId);
        List<MsExecResponseDTO> dtoList = planTestPlanApiCaseService.run(request);
        return this.parseMsExecResponseDTOToTestIdReportMap(dtoList);
    }

    private Map<String, String> executeScenarioCase(String planReportId, String testPlanID, String projectID, RunModeConfigDTO runModeConfig, String triggerMode, String userId, Map<String, String> planScenarioIdMap) {
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

    private Map<String, String> executeUiScenarioCase(String planReportId, String testPlanID, String projectID, RunModeConfigDTO runModeConfig, String triggerMode, String userId, Map<String, String> planScenarioIdMap) {
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
                List<OperatingLogDetails> detailsList = new ArrayList<>();
                for (TestPlan plan : planList) {
                    List<DetailColumn> columns = ReflexObjectUtil.getColumns(planList.get(0), TestPlanReference.testPlanColumns);
                    OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(plan.getId()), plan.getProjectId(), plan.getName(), plan.getCreator(), columns);
                    detailsList.add(details);
                }
                return JSON.toJSONString(detailsList);
            }
        }
        return null;
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

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
    public void copyPlanCase(String sourcePlanId, String targetPlanId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(sourcePlanId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestPlanTestCaseMapper testCaseMapper = sqlSession.getMapper(TestPlanTestCaseMapper.class);
            if (!CollectionUtils.isEmpty(testPlanTestCases)) {
                Long nextTestCaseOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanTestCaseMapper::getLastOrder);
                for (TestPlanTestCase testCase : testPlanTestCases) {
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
            sqlSession.flushStatements();

            planTestPlanApiCaseService.copyPlan(sourcePlanId, targetPlanId);
            planTestPlanScenarioCaseService.copyPlan(sourcePlanId, targetPlanId);
            planTestPlanLoadCaseService.copyPlan(sourcePlanId, targetPlanId);
            planTestPlanUiScenarioCaseService.copyPlan(sourcePlanId, targetPlanId);

            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public Map<String, List<String>> getApiCaseEnv(List<String> planApiCaseIds) {
        return planTestPlanApiCaseService.getApiCaseEnv(planApiCaseIds);
    }

    public Map<String, List<String>> getApiScenarioEnv(List<String> planApiScenarioIds) {
        return planTestPlanScenarioCaseService.getApiCaseEnv(planApiScenarioIds);
    }

    public void buildFunctionalReport(TestPlanSimpleReportDTO report, Map config, String planId) {
        if (checkReportConfig(config, "functional")) {
            List<TestPlanCaseDTO> allCases = null;
            List<String> statusList = getFunctionalReportStatusList(config);
            if (statusList != null) {
                // 不等于null，说明配置了用例，根据配置的状态查询用例
                allCases = testPlanTestCaseService.getAllCasesByStatusList(planId, statusList);
                report.setFunctionAllCases(allCases);
            }

            if (checkReportConfig(config, "functional", "issue")) {
                List<IssuesDao> issueList = issuesService.getIssuesByPlanId(planId);
                report.setIssueList(issueList);
            }
        }
    }

    public void buildUiReport(TestPlanSimpleReportDTO report, Map config, String planId, TestPlanExecuteReportDTO testPlanExecuteReportDTO, boolean saveResponse) {
        ApiPlanReportRequest request = new ApiPlanReportRequest();
        request.setConfig(config);
        request.setPlanId(planId);
        request.setSaveResponse(saveResponse);
        request.setTestPlanExecuteReportDTO(testPlanExecuteReportDTO);
        try {
            // todo 调用失败处理
            UiPlanReportDTO uiReport = planTestPlanUiScenarioCaseService.getUiReport(request);
            BeanUtils.copyBean(report, uiReport);
        } catch (MSException e) {
            LogUtil.error(e);
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
        if (checkReportConfig(config, "functional", "all")) {
            return statusList;
        }
        if (checkReportConfig(config, "functional", "failure")) {
            statusList.add(TestPlanTestCaseStatus.Failure.name());
        }
        if (checkReportConfig(config, "functional", "blocking")) {
            statusList.add(TestPlanTestCaseStatus.Blocking.name());
        }
        if (checkReportConfig(config, "functional", "skip")) {
            statusList.add(TestPlanTestCaseStatus.Skip.name());
        }
        return statusList.size() > 0 ? statusList : null;
    }

    public void buildApiReport(TestPlanSimpleReportDTO report, Map config, String planId, boolean saveResponse) {
        ApiPlanReportRequest request = new ApiPlanReportRequest();
        request.setConfig(config);
        request.setPlanId(planId);
        request.setSaveResponse(saveResponse);
        try {
            ApiPlanReportDTO apiReport = planTestPlanScenarioCaseService.getApiReport(request);
            BeanUtils.copyBean(report, apiReport);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void buildLoadReport(TestPlanSimpleReportDTO report, Map config, String planId, boolean saveResponse) {
        ApiPlanReportRequest request = new ApiPlanReportRequest();
        request.setConfig(config);
        request.setPlanId(planId);
        request.setSaveResponse(saveResponse);
        try {
            LoadPlanReportDTO loadPlanReport = planTestPlanLoadCaseService.getLoadReport(request);
            BeanUtils.copyBean(report, loadPlanReport);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void buildApiReport(TestPlanSimpleReportDTO report, Map config, TestPlanExecuteReportDTO testPlanExecuteReportDTO) {
        ApiPlanReportRequest request = new ApiPlanReportRequest();
        request.setConfig(config);
        request.setTestPlanExecuteReportDTO(testPlanExecuteReportDTO);
        try {
            // todo 调用失败处理
            ApiPlanReportDTO apiPlanReport = planTestPlanScenarioCaseService.getApiExecuteReport(request);
            BeanUtils.copyBean(report, apiPlanReport);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private boolean checkReportConfig(Map config, String key, String subKey) {
        return ServiceUtils.checkConfigEnable(config, key, subKey);
    }

    public void buildLoadReport(TestPlanSimpleReportDTO report, Map config, Map<String, String> loadCaseReportMap, boolean saveResponse) {
        ApiPlanReportRequest request = new ApiPlanReportRequest();
        request.setConfig(config);
        request.setSaveResponse(saveResponse);
        request.setReportIdMap(loadCaseReportMap);
        try {
            // todo 调用失败处理
            LoadPlanReportDTO loadPlanReport = planTestPlanLoadCaseService.getLoadExecuteReport(request);
            BeanUtils.copyBean(report, loadPlanReport);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * @param testPlanReport                 测试计划报告
     * @param testPlanReportContentWithBLOBs 测试计划报告内容
     * @return
     */
    public TestPlanReportBuildResultDTO buildPlanReport(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        TestPlanReportBuildResultDTO returnDTO = new TestPlanReportBuildResultDTO();
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
        if (testPlan != null) {
            String reportConfig = testPlan.getReportConfig();
            Map config = null;
            if (StringUtils.isNotBlank(reportConfig)) {
                config = JSON.parseMap(reportConfig);
            }
            TestPlanExecuteReportDTO testPlanExecuteReportDTO = testPlanReportService.genTestPlanExecuteReportDTOByTestPlanReportContent(testPlanReportContentWithBLOBs);
            TestPlanSimpleReportDTO report = null;
            if (StringUtils.isEmpty(testPlanReportContentWithBLOBs.getApiBaseCount())) {
                report = getReport(testPlanReport.getTestPlanId(), testPlanExecuteReportDTO);
                testPlanReportContentWithBLOBs.setApiBaseCount(JSON.toJSONString(report));
                returnDTO.setApiBaseInfoChanged(true);
            } else {
                try {
                    report = JSON.parseObject(testPlanReportContentWithBLOBs.getApiBaseCount(), TestPlanSimpleReportDTO.class);
                } catch (Exception e) {
                    LogUtil.info("解析接口统计数据出错！数据：" + testPlanReportContentWithBLOBs.getApiBaseCount(), e);
                }
                if (report == null) {
                    report = getReport(testPlanReport.getTestPlanId(), testPlanExecuteReportDTO);
                    testPlanReportContentWithBLOBs.setApiBaseCount(JSON.toJSONString(report));
                    returnDTO.setApiBaseInfoChanged(true);
                }
            }
            buildFunctionalReport(report, config, testPlanReport.getTestPlanId());
            buildApiReport(report, config, testPlanExecuteReportDTO);
            buildLoadReport(report, config, testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap(), false);
            buildUiReport(report, config, testPlanReport.getTestPlanId(), testPlanExecuteReportDTO, false);
            returnDTO.setTestPlanSimpleReportDTO(report);
            return returnDTO;
        } else {
            returnDTO.setTestPlanSimpleReportDTO(new TestPlanSimpleReportDTO());
            return returnDTO;
        }
    }

    public TestPlanSimpleReportDTO buildPlanReport(String planId, boolean saveResponse) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(planId);

        String reportConfig = testPlan.getReportConfig();
        Map config = null;
        if (StringUtils.isNotBlank(reportConfig)) {
            config = JSON.parseMap(reportConfig);
        }
        TestPlanSimpleReportDTO report = getReport(planId, null);
        buildFunctionalReport(report, config, planId);
        buildApiReport(report, config, planId, saveResponse);
        buildLoadReport(report, config, planId, saveResponse);
        buildUiReport(report, config, planId, null, saveResponse);
        return report;
    }

    public void exportPlanReport(String planId, String lang, HttpServletResponse response) throws UnsupportedEncodingException {
        TestPlanSimpleReportDTO report = buildPlanReport(planId, true);
        report.setLang(lang);
        render(report, response);
    }

    public void exportPlanDbReport(String reportId, String lang, HttpServletResponse response) throws UnsupportedEncodingException {
        TestPlanSimpleReportDTO report = testPlanReportService.getReport(reportId);
        runReportWithExceptionHandle(report, r -> planTestPlanApiCaseService.buildResponse(r.getApiAllCases()),
                (r, res) -> r.setApiAllCases((List<TestPlanFailureApiDTO>) res));
        runReportWithExceptionHandle(report, r -> planTestPlanApiCaseService.buildResponse(r.getApiFailureCases()),
                (r, res) -> r.setApiFailureCases((List<TestPlanFailureApiDTO>) res));

        runReportWithExceptionHandle(report, r -> planTestPlanScenarioCaseService.buildResponse(r.getScenarioAllCases()),
                (r, res) -> r.setScenarioAllCases((List<TestPlanFailureScenarioDTO>) res));
        runReportWithExceptionHandle(report, r -> planTestPlanScenarioCaseService.buildResponse(r.getScenarioFailureCases()),
                (r, res) -> r.setScenarioFailureCases((List<TestPlanFailureScenarioDTO>) res));

        runReportWithExceptionHandle(report, r -> planTestPlanUiScenarioCaseService.buildResponse(r.getUiAllCases()),
                (r, res) -> r.setUiAllCases((List<TestPlanUiScenarioDTO>) res));
        runReportWithExceptionHandle(report, r -> planTestPlanUiScenarioCaseService.buildResponse(r.getUiFailureCases()),
                (r, res) -> r.setUiFailureCases((List<TestPlanUiScenarioDTO>) res));

        runReportWithExceptionHandle(report, r -> planTestPlanLoadCaseService.buildResponse(r.getLoadAllCases()),
                (r, res) -> r.setLoadAllCases((List<TestPlanLoadCaseDTO>) res));
        report.setLang(lang);
        render(report, response);
    }

    public void runReportWithExceptionHandle(TestPlanSimpleReportDTO report, Function<TestPlanSimpleReportDTO, Object> getCaseFunc,
                                               BiConsumer<TestPlanSimpleReportDTO, Object> setReportCaseFunc) {
        try {
            // todo 服务调用失败
            setReportCaseFunc.accept(report, getCaseFunc.apply(report));
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public Boolean checkReportConfig(Map config, String key) {
        return ServiceUtils.checkConfigEnable(config, key);
    }


    public void render(TestPlanSimpleReportDTO report, HttpServletResponse response) throws UnsupportedEncodingException {
        response.reset();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("test", StandardCharsets.UTF_8.name()));

        try (InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("/public/plan-report.html"), StandardCharsets.UTF_8);
             ServletOutputStream outputStream = response.getOutputStream()) {
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line = null;
            while (null != (line = bufferedReader.readLine())) {
                if (line.contains("\"#report\"")) {
                    line = line.replace("\"#report\"", new Gson().toJson(report));
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

    /**
     * 生成测试计划报告并进行统计
     *
     * @param planId
     * @param testPlanExecuteReportDTO 测试计划各个资源的报告。 （如果为空，则取当前测试计划资源的最新报告）
     * @return
     */
    public TestPlanSimpleReportDTO getReport(String planId, TestPlanExecuteReportDTO testPlanExecuteReportDTO) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(planId);
        TestPlanSimpleReportDTO report = new TestPlanSimpleReportDTO();
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
        if (testPlanExecuteReportDTO == null) {
            planTestPlanApiCaseService.calculatePlanReport(planId, report);
            planTestPlanScenarioCaseService.calculatePlanReport(planId, report);
            planTestPlanLoadCaseService.calculatePlanReport(planId, report);
            planTestPlanUiScenarioCaseService.calculatePlanReport(planId, report);
        } else {
            if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap())) {
                planTestPlanApiCaseService.calculatePlanReport(new ArrayList<>(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap().values()), report);
            }

            if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
                planTestPlanScenarioCaseService.calculatePlanReport(new ArrayList<>(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap().values()), report);
            }

            if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap())) {
                planTestPlanLoadCaseService.calculatePlanReport(new ArrayList<>(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap().values()), report);
            }

            if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanUiScenarioIdAndReportIdMap())) {
                planTestPlanUiScenarioCaseService.calculatePlanReport(new ArrayList<>(testPlanExecuteReportDTO.getTestPlanUiScenarioIdAndReportIdMap().values()), report);
            }
        }

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
        return report;
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

        Set<String> projectIds = scenarioEnv.keySet();
        for (String projectId : projectIds) {
            if (envMap.containsKey(projectId)) {
                List<String> apiList = envMap.get(projectId);
                List<String> scenarioList = scenarioEnv.get(projectId);
                List<String> result = Stream.of(apiList, scenarioList)
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());
                envMap.put(projectId, result);
            } else {
                envMap.put(projectId, scenarioEnv.get(projectId));
            }
        }

        return envMap;
    }

    public String runPlan(TestPlanRunRequest testplanRunRequest) {
        //检查测试计划下有没有可以执行的用例；
        if (!haveExecCase(testplanRunRequest.getTestPlanId())) {
            MSException.throwException(Translator.get("plan_warning"));
        }
        String envType = testplanRunRequest.getEnvironmentType();
        Map<String, String> envMap = testplanRunRequest.getEnvMap();
        String environmentGroupId = testplanRunRequest.getEnvironmentGroupId();
        String testPlanId = testplanRunRequest.getTestPlanId();
        RunModeConfigDTO runModeConfig = getRunModeConfigDTO(testplanRunRequest, envType, envMap, environmentGroupId, testPlanId);
        if (!testplanRunRequest.isRunWithinResourcePool()) {
            runModeConfig.setResourcePoolId(null);
        }
        String apiRunConfig = JSON.toJSONString(runModeConfig);
        return this.run(testPlanId, testplanRunRequest.getProjectId(),
                testplanRunRequest.getUserId(), testplanRunRequest.getTriggerMode(), testplanRunRequest.getReportId(), apiRunConfig);

    }

    private RunModeConfigDTO getRunModeConfigDTO(TestPlanRunRequest testplanRunRequest, String envType, Map<String, String> envMap, String environmentGroupId, String testPlanId) {
        RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
        runModeConfig.setEnvironmentType(testplanRunRequest.getEnvironmentType());
        if (StringUtils.equals(envType, "JSON") && !envMap.isEmpty()) {
            runModeConfig.setEnvMap(testplanRunRequest.getEnvMap());
            this.setPlanCaseEnv(testPlanId, runModeConfig);
        } else if (StringUtils.equals(envType, "JSON") && StringUtils.isNotBlank(environmentGroupId)) {
            runModeConfig.setEnvironmentGroupId(testplanRunRequest.getEnvironmentGroupId());
            this.setPlanCaseEnv(testPlanId, runModeConfig);
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
        try {
            // todo  调用失败
            planTestPlanApiCaseService.setApiCaseEnv(planId, runModeConfig);
            planTestPlanScenarioCaseService.setScenarioEnv(planId, runModeConfig);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void editReportConfig(TestPlanDTO testPlanDTO) {
        TestPlanWithBLOBs testPlan = new TestPlanWithBLOBs();
        testPlan.setId(testPlanDTO.getId());
        testPlan.setReportConfig(testPlanDTO.getReportConfig());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    public boolean haveExecCase(String planId) {
        if (StringUtils.isBlank(planId)) {
            return false;
        }

        if (planTestPlanApiCaseService.haveExecCase(planId)) {
            return true;
        }

        if (planTestPlanScenarioCaseService.haveExecCase(planId)) {
            return true;
        }

        if (planTestPlanUiScenarioCaseService.haveUiCase(planId)) {
            return true;
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

    //获取下次执行时间（getFireTimeAfter，也可以下下次...）
    private static long getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return 0;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Caclulate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? 0 : time1.getTime();
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
            if (!haveExecCase(planList.get(i).getId())) {
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
            //创建测试报告
            RunModeConfigDTO runModeConfigDTO = JSON.parseObject(testPlan.getRunModeConfig(), RunModeConfigDTO.class);
            this.genTestPlanReport(planReportId, testPlan.getId(), request.getUserId(), request.getTriggerMode(), runModeConfigDTO);
            //测试计划准备执行，取消测试计划的实际结束时间
            extTestPlanMapper.updateActualEndTimeIsNullById(testPlan.getId());
            executeQueue.put(testPlan.getId(), planReportId);
        }

        LoggerUtil.info("开始生成测试计划队列");

        List<TestPlanExecutionQueue> planExecutionQueues = getTestPlanExecutionQueues(request, executeQueue);

        if (CollectionUtils.isNotEmpty(planExecutionQueues)) {
            testPlanExecutionQueueService.batchSave(planExecutionQueues);
        }
        // 开始选择执行模式
        runByMode(request, testPlanMap, planExecutionQueues);

    }


    private List<TestPlanExecutionQueue> getTestPlanExecutionQueues(TestPlanRunRequest request, Map<String, String> executeQueue) {
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
            nextNum[0]++;
            planExecutionQueues.add(executionQueue);
        });
        return planExecutionQueues;
    }

    private void runByMode(TestPlanRunRequest request, Map<String, TestPlanWithBLOBs> testPlanMap, List<TestPlanExecutionQueue> planExecutionQueues) {
        if (CollectionUtils.isNotEmpty(planExecutionQueues)) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("TEST_PLAN_BATCH：" + System.currentTimeMillis());
                    if (StringUtils.equalsIgnoreCase(request.getMode(), RunModeConstants.SERIAL.name())) {
                        TestPlanExecutionQueue planExecutionQueue = planExecutionQueues.get(0);
                        TestPlanWithBLOBs testPlan = testPlanMap.get(planExecutionQueue.getTestPlanId());
                        Map jsonObject = JSON.parseMap(testPlan.getRunModeConfig());
                        TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                        TestPlanRunRequest runRequest = JSON.parseObject(JSON.toJSONString(jsonObject), TestPlanRunRequest.class);
                        runRequest.setReportId(planExecutionQueue.getReportId());
                        runPlan(runRequest);
                    } else {
                        for (TestPlanExecutionQueue planExecutionQueue : planExecutionQueues) {
                            TestPlanWithBLOBs testPlan = testPlanMap.get(planExecutionQueue.getTestPlanId());
                            Map jsonObject = JSON.parseMap(testPlan.getRunModeConfig());
                            TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                            TestPlanRunRequest runRequest = JSON.parseObject(JSON.toJSONString(jsonObject), TestPlanRunRequest.class);
                            runRequest.setReportId(planExecutionQueue.getReportId());
                            runPlan(runRequest);
                        }
                    }
                }
            });
            thread.start();
        }
    }

    public void updateRunModeConfig(TestPlanRunRequest testplanRunRequest) {
        String testPlanId = testplanRunRequest.getTestPlanId();
        updatePlan(testplanRunRequest, testPlanId);
    }

    public boolean haveUiCase(String planId) {
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
}
