package io.metersphere.track.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.datacount.request.ScheduleInfoRequest;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.definition.ParamsDTO;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.*;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestPlanReference;
import io.metersphere.performance.base.*;
import io.metersphere.performance.dto.LoadTestExportJmx;
import io.metersphere.performance.dto.MetricData;
import io.metersphere.performance.dto.Monitor;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.MetricQueryService;
import io.metersphere.performance.service.PerformanceReportService;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.*;
import io.metersphere.track.domain.ReportComponent;
import io.metersphere.track.dto.*;
import io.metersphere.track.factory.ReportComponentFactory;
import io.metersphere.track.request.testcase.PlanCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.AddTestPlanRequest;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.TestPlanRunRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.service.utils.TestPlanRequestUtil;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {
    Logger testPlanLog = LoggerFactory.getLogger("testPlanExecuteLog");

    @Resource
    ExtScheduleMapper extScheduleMapper;
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    ExtTestPlanMapper extTestPlanMapper;
    @Resource
    ScheduleService scheduleService;
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    UserService userService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Lazy
    @Resource
    TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    TestCaseReportMapper testCaseReportMapper;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    ProjectMapper projectMapper;
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
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
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private PerformanceReportService performanceReportService;
    @Resource
    private MetricQueryService metricQueryService;
    @Resource
    private TestPlanPrincipalService testPlanPrincipalService;
    @Resource
    private TestPlanPrincipalMapper testPlanPrincipalMapper;
    @Resource
    private TestPlanFollowService testPlanFollowService;
    @Resource
    private TestPlanFollowMapper testPlanFollowMapper;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private TestPlanExecutionQueueService testPlanExecutionQueueService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private JMeterService jMeterService;

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
            extScheduleMapper.updateNameByResourceID(testPlan.getId(), testPlan.getName());//   同步更新该测试的定时任务的name
            i = testPlanMapper.updateByPrimaryKeyWithBLOBs(testPlan); //  更新
        }
        return testPlanMapper.selectByPrimaryKey(testPlan.getId());
    }

    //计划内容
    private Map<String, Object> getTestPlanParamMap(TestPlan testPlan) {
        Map context = new HashMap();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        context.putAll(new BeanMap(testPlan));
        return context;
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
        testPlanPrincipalService.deleteTestPlanPrincipalByPlanId(planId);
        testPlanFollowService.deleteTestPlanFollowByPlanId(planId);
        deleteTestCaseByPlanId(planId);
        testPlanApiCaseService.deleteByPlanId(planId);
        testPlanScenarioCaseService.deleteByPlanId(planId);
        testPlanLoadCaseService.deleteByPlanId(planId);
        testPlanReportService.deleteByPlanId(planId);

        //删除定时任务
        scheduleService.deleteByResourceId(planId, ScheduleGroup.TEST_PLAN_TEST.name());

        return testPlanMapper.deleteByPrimaryKey(planId);
    }

    public void deleteTestCaseByPlanId(String testPlanId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(testPlanId);
        testPlanTestCaseMapper.deleteByExample(testPlanTestCaseExample);
    }

    public void calcTestPlanRate(List<TestPlanDTOWithMetric> testPlans) {
        testPlans.forEach(testPlan -> {
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

            List<String> apiExecResults = testPlanApiCaseService.getExecResultByPlanId(testPlan.getId());
            apiExecResults.forEach(item -> {
                if (StringUtils.isNotBlank(item)) {
                    testPlan.setTested(testPlan.getTested() + 1);
                    if (StringUtils.equals(item, "success")) {
                        testPlan.setPassed(testPlan.getPassed() + 1);
                    }
                }
            });

            List<String> scenarioExecResults = testPlanScenarioCaseService.getExecResultByPlanId(testPlan.getId());
            scenarioExecResults.forEach(item -> {
                if (StringUtils.isNotBlank(item)) {
                    testPlan.setTested(testPlan.getTested() + 1);
                    if (StringUtils.equals(item, ScenarioStatus.Success.name())) {
                        testPlan.setPassed(testPlan.getPassed() + 1);
                    }
                }
            });

            List<String> loadResults = testPlanLoadCaseService.getStatus(testPlan.getId());
            loadResults.forEach(item -> {
                if (StringUtils.isNotBlank(item)) {
                    testPlan.setTested(testPlan.getTested() + 1);
                    if (StringUtils.equals(item, "success")) {
                        testPlan.setPassed(testPlan.getPassed() + 1);
                    }
                }
            });

            testPlan.setTotal(apiExecResults.size() + scenarioExecResults.size() + functionalExecTotal + loadResults.size());

            testPlan.setPassRate(MathUtils.getPercentWithDecimal(testPlan.getTested() == 0 ? 0 : testPlan.getPassed() * 1.0 / testPlan.getTotal()));
            testPlan.setTestRate(MathUtils.getPercentWithDecimal(testPlan.getTotal() == 0 ? 0 : testPlan.getTested() * 1.0 / testPlan.getTotal()));
        });
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
        Map<String, ParamsDTO> planLoadCaseMap = extTestPlanMapper.testPlanLoadCaseCount(ids);
        ArrayList<String> idList = new ArrayList<>(ids);
        List<Schedule> scheduleByResourceIds = scheduleService.getScheduleByResourceIds(idList, ScheduleGroup.TEST_PLAN_TEST.name());
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
            item.setTestPlanLoadCaseCount(planLoadCaseMap.get(item.getId()) == null ? 0 : Integer.parseInt(planLoadCaseMap.get(item.getId()).getValue() == null ? "0" : planLoadCaseMap.get(item.getId()).getValue()));
        });
        calcTestPlanRate(testPlans);
        return testPlans;
    }

    public void checkStatus(String testPlanId) { //  检查执行结果，自动更新计划状态
        List<String> statusList = new ArrayList<>();
        statusList.addAll(extTestPlanTestCaseMapper.getExecResultByPlanId(testPlanId));
        statusList.addAll(testPlanApiCaseService.getExecResultByPlanId(testPlanId));
        statusList.addAll(testPlanScenarioCaseService.getExecResultByPlanId(testPlanId));
        statusList.addAll(testPlanLoadCaseService.getStatus(testPlanId));
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
                    || StringUtils.equals(res, "success")
                    || StringUtils.equals(res, ScenarioStatus.Success.name())) {
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
            String maintainer = Optional.ofNullable(userMap.get(caseId)).orElse(SessionUtils.getUserId());
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

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
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
                    Long nextLoadOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanLoadCaseMapper::getLastOrder);
                    Long nextApiOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanApiCaseMapper::getLastOrder);
                    Long nextScenarioOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanScenarioCaseMapper::getLastOrder);

                    for (TestCaseTest l : list) {
                        if (StringUtils.equals(l.getTestType(), TestCaseStatus.performance.name())) {
                            String id = l.getTestId();
                            LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(id);
                            TestPlanLoadCaseWithBLOBs t = new TestPlanLoadCaseWithBLOBs();
                            t.setId(UUID.randomUUID().toString());
                            t.setTestPlanId(request.getPlanId());
                            t.setLoadCaseId(l.getTestId());
                            t.setCreateTime(System.currentTimeMillis());
                            t.setUpdateTime(System.currentTimeMillis());
                            t.setOrder(nextLoadOrder);
                            if (loadTest != null) {
                                t.setTestResourcePoolId(loadTest.getTestResourcePoolId());
                                t.setLoadConfiguration(loadTest.getLoadConfiguration());
                                t.setAdvancedConfiguration(loadTest.getAdvancedConfiguration());
                            }
                            nextLoadOrder += 5000;
                            TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
                            testPlanLoadCaseExample.createCriteria().andTestPlanIdEqualTo(request.getPlanId()).andLoadCaseIdEqualTo(t.getLoadCaseId());
                            if (testPlanLoadCaseMapper.countByExample(testPlanLoadCaseExample) <= 0) {
                                testPlanLoadCaseMapper.insert(t);
                            }

                        }
                        if (StringUtils.equals(l.getTestType(), TestCaseStatus.testcase.name())) {
                            TestPlanApiCase t = new TestPlanApiCase();
                            ApiTestCaseWithBLOBs apitest = apiTestCaseMapper.selectByPrimaryKey(l.getTestId());
                            if (null != apitest) {
                                t.setId(UUID.randomUUID().toString());
                                t.setTestPlanId(request.getPlanId());
                                t.setApiCaseId(l.getTestId());
                                ApiTestEnvironment apiCaseEnvironment = apiTestCaseService.getApiCaseEnvironment(l.getTestId());
                                if (apiCaseEnvironment != null && StringUtils.isNotBlank(apiCaseEnvironment.getId())) {
                                    t.setEnvironmentId(apiCaseEnvironment.getId());
                                }
                                t.setCreateTime(System.currentTimeMillis());
                                t.setUpdateTime(System.currentTimeMillis());
                                t.setOrder(nextApiOrder);
                                nextApiOrder += 5000;
                                TestPlanApiCaseExample example = new TestPlanApiCaseExample();
                                example.createCriteria().andTestPlanIdEqualTo(request.getPlanId()).andApiCaseIdEqualTo(t.getApiCaseId());
                                if (testPlanApiCaseMapper.countByExample(example) <= 0) {
                                    testPlanApiCaseMapper.insert(t);
                                }
                            }


                        }
                        if (StringUtils.equals(l.getTestType(), TestCaseStatus.automation.name())) {
                            TestPlanApiScenario t = new TestPlanApiScenario();
                            ApiScenarioWithBLOBs testPlanApiScenario = apiScenarioMapper.selectByPrimaryKey(l.getTestId());
                            if (testPlanApiScenario != null) {
                                t.setId(UUID.randomUUID().toString());
                                t.setTestPlanId(request.getPlanId());
                                t.setApiScenarioId(l.getTestId());
                                t.setPassRate(testPlanApiScenario.getPassRate());
                                t.setReportId(testPlanApiScenario.getReportId());
                                t.setStatus(testPlanApiScenario.getStatus());
                                t.setCreateTime(System.currentTimeMillis());
                                t.setUpdateTime(System.currentTimeMillis());
                                t.setOrder(nextScenarioOrder);
                                t.setEnvironmentType(testPlanApiScenario.getEnvironmentType());
                                t.setEnvironment(testPlanApiScenario.getEnvironmentJson());
                                t.setEnvironmentGroupId(testPlanApiScenario.getEnvironmentGroupId());
                                nextScenarioOrder += 5000;
                                TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
                                example.createCriteria().andTestPlanIdEqualTo(request.getPlanId()).andApiScenarioIdEqualTo(t.getApiScenarioId());
                                if (testPlanApiScenarioMapper.countByExample(example) <= 0) {
                                    testPlanApiScenarioMapper.insert(t);
                                }
                            }

                        }
                    }
                });
            }
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
        testPlanTestCaseService.wrapQueryTestPlanCaseRequest(request);
        return testPlanTestCaseService.list(request);
    }

    private List<TestPlanCaseDTO> listTestCaseByProjectIds(List<String> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return new ArrayList<>();
        }
        return extTestPlanTestCaseMapper.listTestCaseByProjectIds(projectIds);
    }

    public TestCaseReportMetricDTO getMetric(String planId) {
        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(planId);

        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        testPlan.setProjectName(project.getName());

        TestCaseReport testCaseReport = testCaseReportMapper.selectByPrimaryKey(testPlan.getReportId());
        JSONObject content = JSONObject.parseObject(testCaseReport.getContent());
        JSONArray componentIds = content.getJSONArray("components");

        List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);
        List<IssuesDao> issues = buildFunctionalCaseReport(planId, components);
        buildApiCaseReport(planId, components);
        buildScenarioCaseReport(planId, components);
        buildLoadCaseReport(planId, components);

        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        components.forEach(component -> {
            component.afterBuild(testCaseReportMetricDTO);
        });
        testCaseReportMetricDTO.setIssues(issues);
        return testCaseReportMetricDTO;
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
        List<String> apiStatusList = extTestPlanApiCaseMapper.getStatusByTestPlanId(planId);
        for (String apiStatus : apiStatusList) {
            if (apiStatus == null) {
                return TestPlanStatus.Underway.name();
            }
        }

        // test-plan-scenario-case status
        List<String> scenarioStatusList = extTestPlanScenarioCaseMapper.getExecResultByPlanId(planId);
        for (String scenarioStatus : scenarioStatusList) {
            if (scenarioStatus == null) {
                return TestPlanStatus.Underway.name();
            }
        }

        // test-plan-load-case status
        List<String> loadStatusList = extTestPlanLoadCaseMapper.getStatusByTestPlanId(planId);
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
        String projectName = "";

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

    public TestCaseReportMetricDTO getStatisticsMetric(String planId) {
        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(planId);

        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        testPlan.setProjectName(project.getName());

        TestCaseReport testCaseReport = testCaseReportMapper.selectByPrimaryKey(testPlan.getReportId());
        JSONObject content = JSONObject.parseObject(testCaseReport.getContent());
        JSONArray componentIds = content.getJSONArray("components");

        List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);
        List<IssuesDao> issues = buildFunctionalCaseReport(planId, components);
        buildApiCaseReport(planId, components);
        buildScenarioCaseReport(planId, components);
        buildLoadCaseReport(planId, components);

        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        components.forEach(component -> {
            component.afterBuild(testCaseReportMetricDTO);
        });
        testCaseReportMetricDTO.setIssues(issues);
        return testCaseReportMetricDTO;
    }

    public void buildApiCaseReport(String planId, List<ReportComponent> components) {
        ApiTestCaseRequest request = new ApiTestCaseRequest();
        request.setPlanId(planId);
        List<TestPlanApiCaseDTO> apiCaseDTOS = testPlanApiCaseService.list(request);
        for (TestPlanApiCaseDTO item : apiCaseDTOS) {
            for (ReportComponent component : components) {
                component.readRecord(item);
            }
        }
    }

    public void buildScenarioCaseReport(String planId, List<ReportComponent> components) {
        TestPlanScenarioRequest request = new TestPlanScenarioRequest();
        request.setPlanId(planId);
        List<ApiScenarioDTO> scenarioDTOS = testPlanScenarioCaseService.list(request);
        for (ApiScenarioDTO item : scenarioDTOS) {
            for (ReportComponent component : components) {
                component.readRecord(item);
            }
        }
    }

    public void buildLoadCaseReport(String planId, List<ReportComponent> components) {
        LoadCaseRequest request = new LoadCaseRequest();
        request.setTestPlanId(planId);
        List<TestPlanLoadCaseDTO> loadDTOs = testPlanLoadCaseService.list(request);
        for (TestPlanLoadCaseDTO item : loadDTOs) {
            for (ReportComponent component : components) {
                component.readRecord(item);
            }
        }
    }

    public List<IssuesDao> buildFunctionalCaseReport(String planId, List<ReportComponent> components) {
        List<TestPlanCaseDTO> testPlanTestCases = listTestCaseByPlanId(planId);
        List<IssuesDao> issues = new ArrayList<>();
        for (TestPlanCaseDTO testCase : testPlanTestCases) {
            List<IssuesDao> issue = issuesService.getIssues(testCase.getId(), IssueRefType.PLAN_FUNCTIONAL.name());
            if (issue.size() > 0) {
                for (IssuesDao i : issue) {
                    i.setModel(testCase.getNodePath());
                    i.setProjectName(testCase.getProjectName());
                    if (StringUtils.isNotBlank(i.getDescription())) {
                        String des = i.getDescription().replaceAll("<p>", "").replaceAll("</p>", "");
                        i.setDescription(des);
                    }
                    if (i.getLastmodify() == null || i.getLastmodify() == "") {
                        if (i.getReporter() != null || i.getReporter() != "") {
                            i.setLastmodify(i.getReporter());
                        }
                    }
                }
                issues.addAll(issue);
                Collections.sort(issues,
                        Comparator.comparing(Issues::getCreateTime, (t1, t2) -> {
                            if (t1 == null) {
                                return 1;
                            } else if (t2 == null) {
                                return -1;
                            }
                            return t2.compareTo(t1);
                        })
                );
            }
            components.forEach(component -> {
                component.readRecord(testCase);
            });
        }
        return issues;
    }

    public List<TestPlanDTO> selectTestPlanByRelevancy(QueryTestPlanRequest params) {
        return extTestPlanMapper.selectTestPlanByRelevancy(params);
    }

    public String findTestProjectNameByTestPlanID(String testPlanId) {
        return extTestPlanMapper.findTestProjectNameByTestPlanID(testPlanId);
    }

    public String findScheduleCreateUserById(String testPlanId) {
        return extTestPlanMapper.findScheduleCreateUserById(testPlanId);
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
            list.addAll(apiAutomationService.run(request));
        }
        return list;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    TestPlanScheduleReportInfoDTO genTestPlanReport(String planReportId, String planId, String userId, String triggerMode) {
        TestPlanScheduleReportInfoDTO reportInfoDTO = testPlanReportService.genTestPlanReportBySchedule(planReportId, planId, userId, triggerMode);
        return reportInfoDTO;
    }

    public String run(String testPlanID, String projectID, String userId, String triggerMode, String planReportId, String apiRunConfig) {
        RunModeConfigDTO runModeConfig = null;
        try {
            runModeConfig = JSONObject.parseObject(apiRunConfig, RunModeConfigDTO.class);
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
                    JSONObject json = JSONObject.parseObject(testPlanWithBLOBs.getRunModeConfig());
                    TestPlanRequestUtil.changeStringToBoolean(json);
                    TestPlanRunRequest testPlanRunRequest = JSON.toJavaObject(json, TestPlanRunRequest.class);
                    if (testPlanRunRequest != null) {
                        String envType = testPlanRunRequest.getEnvironmentType();
                        Map<String, String> envMap = testPlanRunRequest.getEnvMap();
                        String environmentGroupId = testPlanRunRequest.getEnvironmentGroupId();
                        runModeConfig = getRunModeConfigDTO(testPlanRunRequest, envType, envMap, environmentGroupId, testPlanID);
                    }
                } catch (Exception e) {
                    LogUtil.error("获取测试计划保存的环境信息出错!", e);
                }
            }
        }
        if (planReportId == null) {
            planReportId = UUID.randomUUID().toString();
        }
        jMeterService.verifyPool(projectID, runModeConfig);
        //创建测试报告，然后返回的ID重新赋值为resourceID，作为后续的参数
        TestPlanScheduleReportInfoDTO reportInfoDTO = this.genTestPlanReport(planReportId, testPlanID, userId, triggerMode);
        //测试计划准备执行，取消测试计划的实际结束时间
        extTestPlanMapper.updateActualEndTimeIsNullById(testPlanID);

        LoggerUtil.info("预生成测试计划报告【" + reportInfoDTO.getTestPlanReport() != null ? reportInfoDTO.getTestPlanReport().getName() : "" + "】计划报告ID[" + planReportId + "]");

        Map<String, String> apiCaseReportMap = null;
        Map<String, String> scenarioReportMap = null;
        Map<String, String> loadCaseReportMap = null;
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
            loadCaseReportMap = this.executeLoadCaseTask(planReportId, runModeConfig, triggerMode, reportInfoDTO.getPerformanceIdMap());
        }
        if (apiCaseReportMap != null && scenarioReportMap != null && loadCaseReportMap != null) {
            LoggerUtil.info("开始生成测试计划报告内容 " + planReportId);
            testPlanReportService.createTestPlanReportContentReportIds(planReportId, apiCaseReportMap, scenarioReportMap, loadCaseReportMap);
        }

        return planReportId;
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
        List<MsExecResponseDTO> dtoList = testPlanApiCaseService.run(request);
        return this.parseMsExecREsponseDTOToTestIdReportMap(dtoList);
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
            scenarioRequest.setExecuteType(ExecuteType.Saved.name());
            Map<String, Map<String, String>> testPlanScenarioIdMap = new HashMap<>();
            testPlanScenarioIdMap.put(testPlanID, planScenarioIdMap);
            scenarioRequest.setTestPlanScenarioIDMap(testPlanScenarioIdMap);
            scenarioRequest.setReportUserID(userId);
            scenarioRequest.setTestPlanID(testPlanID);
            scenarioRequest.setTestPlanReportId(planReportId);
            scenarioRequest.setConfig(runModeConfig);
            List<MsExecResponseDTO> dtoList = this.scenarioRunModeConfig(scenarioRequest);
            return this.parseMsExecREsponseDTOToTestIdReportMap(dtoList);
        } else {
            return new HashMap<>();
        }
    }

    private Map<String, String> parseMsExecREsponseDTOToTestIdReportMap(List<MsExecResponseDTO> dtoList) {
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

    private Map<String, String> executeLoadCaseTask(String planReportId, RunModeConfigDTO runModeConfig, String triggerMode, Map<String, String> performanceIdMap) {
        Map<String, String> loadCaseReportMap = new HashMap<>();
        for (Map.Entry<String, String> entry : performanceIdMap.entrySet()) {
            String id = entry.getKey();
            String caseID = entry.getValue();
            RunTestPlanRequest performanceRequest = new RunTestPlanRequest();
            performanceRequest.setId(caseID);
            performanceRequest.setTestPlanLoadId(id);
            if (StringUtils.isNotBlank(runModeConfig.getResourcePoolId())) {
                performanceRequest.setTestResourcePoolId(runModeConfig.getResourcePoolId());
            }
            if (StringUtils.equals(ReportTriggerMode.API.name(), triggerMode)) {
                performanceRequest.setTriggerMode(ReportTriggerMode.TEST_PLAN_API.name());
            } else {
                performanceRequest.setTriggerMode(ReportTriggerMode.TEST_PLAN_SCHEDULE.name());
            }
            String reportId = null;
            try {
                reportId = performanceTestService.run(performanceRequest);
                if (reportId != null) {
                    loadCaseReportMap.put(id, reportId);
                    //更新关联处的报告
                    TestPlanLoadCaseWithBLOBs loadCase = new TestPlanLoadCaseDTO();
                    loadCase.setId(id);
                    loadCase.setLoadReportId(reportId);
                    loadCase.setStatus(TestPlanLoadCaseStatus.run.name());
                    testPlanLoadCaseService.update(loadCase);
                }
            } catch (Exception e) {
                TestPlanLoadCaseWithBLOBs testPlanLoadCase = new TestPlanLoadCaseWithBLOBs();
                testPlanLoadCase.setId(id);
                testPlanLoadCase.setLoadReportId(reportId);
                testPlanLoadCase.setStatus(TestPlanLoadCaseStatus.error.name());
                testPlanLoadCaseService.update(testPlanLoadCase);
                LogUtil.error(e);
            }

        }
        if (MapUtils.isNotEmpty(loadCaseReportMap)) {
            //将性能测试加入到队列中
            apiExecutionQueueService.add(loadCaseReportMap, null, ApiRunMode.TEST_PLAN_PERFORMANCE_TEST.name(),
                    planReportId, null, null, new RunModeConfigDTO());
        }


        return loadCaseReportMap;
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

            TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
            testPlanApiCaseExample.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);
            TestPlanApiCaseMapper apiCaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
            if (!CollectionUtils.isEmpty(testPlanApiCases)) {
                Long nextApiOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanApiCaseMapper::getLastOrder);
                for (TestPlanApiCase apiCase : testPlanApiCases) {
                    TestPlanApiCase api = new TestPlanApiCase();
                    api.setId(UUID.randomUUID().toString());
                    api.setTestPlanId(targetPlanId);
                    api.setApiCaseId(apiCase.getApiCaseId());
                    api.setEnvironmentId(apiCase.getEnvironmentId());
                    api.setCreateTime(System.currentTimeMillis());
                    api.setUpdateTime(System.currentTimeMillis());
                    api.setCreateUser(SessionUtils.getUserId());
                    api.setOrder(nextApiOrder);
                    nextApiOrder += 5000;
                    apiCaseMapper.insert(api);
                }
            }
            sqlSession.flushStatements();

            TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
            testPlanApiScenarioExample.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanApiScenario> apiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(testPlanApiScenarioExample);
            TestPlanApiScenarioMapper apiScenarioMapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);
            if (!CollectionUtils.isEmpty(apiScenarios)) {
                Long nextScenarioOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanScenarioCaseMapper::getLastOrder);
                for (TestPlanApiScenario apiScenario : apiScenarios) {
                    TestPlanApiScenario planScenario = new TestPlanApiScenario();
                    planScenario.setId(UUID.randomUUID().toString());
                    planScenario.setTestPlanId(targetPlanId);
                    planScenario.setApiScenarioId(apiScenario.getApiScenarioId());
                    planScenario.setEnvironment(apiScenario.getEnvironment());
                    if (apiScenario.getEnvironmentType() != null) {
                        planScenario.setEnvironmentType(apiScenario.getEnvironmentType());
                    }
                    planScenario.setCreateTime(System.currentTimeMillis());
                    planScenario.setUpdateTime(System.currentTimeMillis());
                    planScenario.setCreateUser(SessionUtils.getUserId());
                    planScenario.setOrder(nextScenarioOrder);
                    nextScenarioOrder += 5000;
                    apiScenarioMapper.insert(planScenario);
                }
            }
            sqlSession.flushStatements();

            TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanLoadCase> loadCases = testPlanLoadCaseMapper.selectByExample(example);
            TestPlanLoadCaseMapper mapper = sqlSession.getMapper(TestPlanLoadCaseMapper.class);
            if (!CollectionUtils.isEmpty(loadCases)) {
                Long nextLoadOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanLoadCaseMapper::getLastOrder);
                for (TestPlanLoadCase loadCase : loadCases) {
                    TestPlanLoadCaseWithBLOBs load = new TestPlanLoadCaseWithBLOBs();
                    load.setId(UUID.randomUUID().toString());
                    load.setTestPlanId(targetPlanId);
                    load.setLoadCaseId(loadCase.getLoadCaseId());
                    load.setCreateTime(System.currentTimeMillis());
                    load.setUpdateTime(System.currentTimeMillis());
                    load.setCreateUser(SessionUtils.getUserId());
                    load.setOrder(nextLoadOrder);
                    mapper.insert(load);
                    nextLoadOrder += 5000;
                }
            }
            sqlSession.flushStatements();

            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public Map<String, List<String>> getApiCaseEnv(List<String> planApiCaseIds) {
        Map<String, List<String>> envMap = new HashMap<>();
        if (CollectionUtils.isEmpty(planApiCaseIds)) {
            return envMap;
        }

        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andIdIn(planApiCaseIds);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> apiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return envMap;
        }

        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(apiCaseIds);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        Map<String, String> projectCaseIdMap = new HashMap<>(16);
        apiTestCases.forEach(c -> projectCaseIdMap.put(c.getId(), c.getProjectId()));

        testPlanApiCases.forEach(testPlanApiCase -> {
            String caseId = testPlanApiCase.getApiCaseId();
            String envId = testPlanApiCase.getEnvironmentId();
            String projectId = projectCaseIdMap.get(caseId);
            if (StringUtils.isNotBlank(projectId)) {
                if (envMap.containsKey(projectId)) {
                    List<String> list = envMap.get(projectId);
                    if (!list.contains(envId)) {
                        list.add(envId);
                    }
                } else {
                    List<String> envs = new ArrayList<>();
                    envs.add(envId);
                    envMap.put(projectId, envs);
                }
            }
        });
        return envMap;
    }

    public Map<String, List<String>> getApiScenarioEnv(List<String> planApiScenarioIds) {
        Map<String, List<String>> envMap = new HashMap<>();
        if (CollectionUtils.isEmpty(planApiScenarioIds)) {
            return envMap;
        }

        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andIdIn(planApiScenarioIds);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(scenarioExample);

        for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
            String env = testPlanApiScenario.getEnvironment();
            if (StringUtils.isBlank(env)) {
                continue;
            }
            Map<String, String> map = JSON.parseObject(env, Map.class);
            if (!map.isEmpty()) {
                Set<String> set = map.keySet();
                for (String s : set) {
                    String e = map.get(s);
                    if (envMap.containsKey(s)) {
                        List<String> list = envMap.get(s);
                        if (!list.contains(e)) {
                            list.add(e);
                        }
                    } else {
                        List<String> envs = new ArrayList<>();
                        envs.add(e);
                        envMap.put(s, envs);
                    }
                }
            }
        }
        return envMap;
    }

    public void buildFunctionalReport(TestPlanSimpleReportDTO report, JSONObject config, String planId) {
        if (checkReportConfig(config, "functional")) {
            List<TestPlanCaseDTO> allCases = null;
            if (checkReportConfig(config, "functional", "all")) {
                allCases = testPlanTestCaseService.getAllCases(planId);
                report.setFunctionAllCases(allCases);
            }
            if (checkReportConfig(config, "functional", "failure")) {
                List<TestPlanCaseDTO> failureCases = null;
                if (!CollectionUtils.isEmpty(allCases)) {
                    failureCases = allCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getStatus())
                                    && i.getStatus().equals("Failure"))
                            .collect(Collectors.toList());
                } else {
                    failureCases = testPlanTestCaseService.getFailureCases(planId);
                }
                report.setFunctionFailureCases(failureCases);
            }
            if (checkReportConfig(config, "functional", "issue")) {
                List<IssuesDao> issueList = issuesService.getIssuesByPlanId(planId);
                report.setIssueList(issueList);
            }
        }
    }

    public void buildApiReport(TestPlanSimpleReportDTO report, JSONObject config, String planId, boolean saveResponse) {
        if (checkReportConfig(config, "api")) {
            List<TestPlanFailureApiDTO> apiAllCases = null;
            List<TestPlanFailureScenarioDTO> scenarioAllCases = null;
            if (checkReportConfig(config, "api", "all")) {
                // 接口
                apiAllCases = testPlanApiCaseService.getAllCases(planId);
                report.setApiAllCases(apiAllCases);
                if (saveResponse) {
                    buildApiResponse(apiAllCases);
                }
                //场景
                scenarioAllCases = testPlanScenarioCaseService.getAllCases(planId);
                if (saveResponse) {
                    buildScenarioResponse(scenarioAllCases);
                }
                report.setScenarioAllCases(scenarioAllCases);
            }
            //筛选符合配置需要的执行结果的用例和场景
            this.screenApiCaseByStatusAndReportConfig(report, apiAllCases, config);
            this.screenScenariosByStatusAndReportConfig(report, scenarioAllCases, config);
        }
    }

    public void buildApiResponse(List<TestPlanFailureApiDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            List<String> reportIds = new ArrayList<>();
            for (TestPlanFailureApiDTO apiCase : cases) {
                if (StringUtils.isEmpty(apiCase.getReportId())) {
                    ApiDefinitionExecResult result = extApiDefinitionExecResultMapper.selectPlanApiMaxResultByTestIdAndType(apiCase.getId(), "API_PLAN");
                    if (result != null && StringUtils.isNotBlank(result.getContent())) {
                        apiCase.setReportId(result.getId());
                        apiCase.setResponse(result.getContent());
                    }
                } else {
                    reportIds.add(apiCase.getReportId());
                }
            }
            if (CollectionUtils.isNotEmpty(reportIds)) {
                ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
                example.createCriteria().andIdIn(reportIds);
                List<ApiDefinitionExecResult> results = apiDefinitionExecResultMapper.selectByExampleWithBLOBs(example);
                // 格式化数据结果
                Map<String, ApiDefinitionExecResult> resultMap = results.stream().collect(Collectors.toMap(ApiDefinitionExecResult::getId, item -> item, (k, v) -> k));
                cases.forEach(item -> {
                    if (resultMap.get(item.getReportId()) != null &&
                            StringUtils.isNotBlank(resultMap.get(item.getReportId()).getContent())) {
                        item.setResponse(resultMap.get(item.getReportId()).getContent());
                    }
                });
            }
        }
    }

    public void buildScenarioResponse(List<TestPlanFailureScenarioDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            cases.forEach((item) -> {
                item.setResponse(apiScenarioReportService.get(item.getReportId(), true));
            });
        }
    }

    public void buildLoadResponse(List<TestPlanLoadCaseDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            cases.forEach(item -> {
                LoadCaseReportRequest request = new LoadCaseReportRequest();
                String reportId = item.getLoadReportId();
                if (StringUtils.isNotBlank(reportId)) {
                    request.setTestPlanLoadCaseId(item.getId());
                    request.setReportId(reportId);
                    Boolean existReport = testPlanLoadCaseService.isExistReport(request);
                    if (existReport) {
                        try {
                            LoadTestReportWithBLOBs loadTestReport = performanceReportService.getLoadTestReport(reportId);
                            ReportTimeInfo reportTimeInfo = performanceReportService.getReportTimeInfo(reportId);
                            TestPlanLoadCaseDTO.ResponseDTO response = new TestPlanLoadCaseDTO.ResponseDTO();
                            if (loadTestReport != null) {
                                BeanUtils.copyBean(response, loadTestReport);
                            }
                            if (reportTimeInfo != null) {
                                BeanUtils.copyBean(response, reportTimeInfo);
                            }

                            // 压力配置
                            if (StringUtils.isBlank(loadTestReport.getLoadConfiguration())) {
                                String loadConfiguration = performanceTestService.getLoadConfiguration(item.getId());
                                response.setFixLoadConfiguration(loadConfiguration);
                            }
                            List<LoadTestExportJmx> jmxContent = performanceReportService.getJmxContent(reportId);
                            if (!CollectionUtils.isEmpty(jmxContent)) {
                                response.setJmxContent(JSONObject.toJSONString(jmxContent.get(0)));
                            }
                            List<LoadTestExportJmx> fixJmxContent = performanceTestService.getJmxContent(item.getId());
                            response.setFixJmxContent(fixJmxContent);

                            // 概览
                            TestOverview testOverview = performanceReportService.getTestOverview(reportId);
                            response.setTestOverview(testOverview);
                            List<ChartsData> loadChartData = performanceReportService.getLoadChartData(reportId);
                            response.setLoadChartData(loadChartData);
                            List<ChartsData> responseTimeChartData = performanceReportService.getResponseTimeChartData(reportId);
                            response.setResponseTimeChartData(responseTimeChartData);
                            List<ChartsData> errorChartData = performanceReportService.getErrorChartData(reportId);
                            response.setErrorChartData(errorChartData);
                            List<ChartsData> responseCodeChartData = performanceReportService.getResponseCodeChartData(reportId);
                            response.setResponseCodeChartData(responseCodeChartData);

                            // 报告详情
                            List<String> reportKeys = Arrays.asList(
                                    "ALL",
                                    "ActiveThreadsChart",
                                    "TransactionsChart",
                                    "ResponseTimeChart",
                                    "ResponseTimePercentilesChart",
                                    "ResponseCodeChart",
                                    "ErrorsChart",
                                    "LatencyChart",
                                    "BytesThroughputChart");

                            Map<String, List<ChartsData>> checkOptions = new HashMap<>();
                            reportKeys.forEach(reportKey -> {
                                List<ChartsData> reportChart = performanceReportService.getReportChart(reportKey, reportId);
                                checkOptions.put(reportKey, reportChart);
                            });
                            response.setCheckOptions(checkOptions);

                            // 统计分析
                            List<Statistics> reportStatistics = performanceReportService.getReportStatistics(reportId);
                            response.setReportStatistics(reportStatistics);

                            // 错误分析
                            List<Errors> reportErrors = performanceReportService.getReportErrors(reportId);
                            response.setReportErrors(reportErrors);
                            List<ErrorsTop5> reportErrorsTop5 = performanceReportService.getReportErrorsTOP5(reportId);
                            response.setReportErrorsTop5(reportErrorsTop5);

                            // 日志详情
                            List<LogDetailDTO> reportLogResource = performanceReportService.getReportLogResource(reportId);
                            response.setReportLogResource(reportLogResource);
//                        performanceReportService.getReportLogs(reportId, resourceId);

                            List<Monitor> reportResource = metricQueryService.queryReportResource(reportId);
                            response.setReportResource(reportResource);
                            List<MetricData> metricData = metricQueryService.queryMetric(reportId);
                            response.setMetricData(metricData);
                            item.setResponse(response);
                        } catch (Exception e) {
                            LogUtil.error(e);
                        }
                    }
                }
            });
        }
    }

    public void buildLoadReport(TestPlanSimpleReportDTO report, JSONObject config, String planId, boolean saveResponse) {
        if (checkReportConfig(config, "load")) {
            List<TestPlanLoadCaseDTO> allCases = null;
            if (checkReportConfig(config, "load", "all")) {
                allCases = testPlanLoadCaseService.getAllCases(planId);
                if (saveResponse) {
                    buildLoadResponse(allCases);
                }
                report.setLoadAllCases(allCases);
            }
            if (checkReportConfig(config, "load", "failure")) {
                List<TestPlanLoadCaseDTO> failureCases = null;
                if (!CollectionUtils.isEmpty(allCases)) {
                    failureCases = allCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getStatus())
                                    && i.getStatus().equals("error"))
                            .collect(Collectors.toList());
                } else {
                    failureCases = testPlanLoadCaseService.getFailureCases(planId);
                }
                report.setLoadFailureCases(failureCases);
            }
        }
    }

    public void buildApiReport(TestPlanSimpleReportDTO report, JSONObject config, TestPlanExecuteReportDTO testPlanExecuteReportDTO) {
        if (MapUtils.isEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap())
                && MapUtils.isEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
            return;
        }
        if (checkReportConfig(config, "api")) {
            List<TestPlanFailureApiDTO> apiAllCases = null;
            List<TestPlanFailureScenarioDTO> scenarioAllCases = null;
            if (checkReportConfig(config, "api", "all")) {
                // 接口
                apiAllCases = testPlanApiCaseService.getByApiExecReportIds(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap(), testPlanExecuteReportDTO.getApiCaseInfoDTOMap());
                //场景
                scenarioAllCases = testPlanScenarioCaseService.getAllCases(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap(), testPlanExecuteReportDTO.getScenarioInfoDTOMap());
                this.checkApiCaseCreatorName(apiAllCases, scenarioAllCases);
                report.setApiAllCases(apiAllCases);
                report.setScenarioAllCases(scenarioAllCases);
            }

            //筛选符合配置需要的执行结果的用例和场景
            this.screenApiCaseByStatusAndReportConfig(report, apiAllCases, config);
            this.screenScenariosByStatusAndReportConfig(report, scenarioAllCases, config);
        }
    }

    private void checkApiCaseCreatorName(List<TestPlanFailureApiDTO> apiCases, List<TestPlanFailureScenarioDTO> scenarioCases) {
        List<String> userIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(apiCases)) {
            apiCases.forEach(item -> {
                if (StringUtils.isEmpty(item.getCreatorName()) && StringUtils.isNotEmpty(item.getCreateUserId())) {
                    userIdList.add(item.getCreateUserId());
                }
            });
        }
        if (CollectionUtils.isNotEmpty(scenarioCases)) {
            scenarioCases.forEach(item -> {
                if (StringUtils.isEmpty(item.getCreatorName()) && StringUtils.isNotEmpty(item.getCreateUser())) {
                    userIdList.add(item.getCreateUser());
                }
            });
        }
        Map<String, User> usersMap = userService.queryNameByIds(userIdList);
        if (CollectionUtils.isNotEmpty(apiCases)) {
            for (TestPlanFailureApiDTO dto : apiCases) {
                if (StringUtils.isEmpty(dto.getCreatorName())) {
                    User user = usersMap.get(dto.getCreateUserId());
                    if (user != null) {
                        dto.setCreatorName(user.getName());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(scenarioCases)) {
            for (TestPlanFailureScenarioDTO dto : scenarioCases) {
                if (StringUtils.isEmpty(dto.getCreatorName())) {
                    User user = usersMap.get(dto.getCreateUser());
                    if (user != null) {
                        dto.setCreatorName(user.getName());
                    }
                }
            }
        }

    }

    private void screenScenariosByStatusAndReportConfig(TestPlanSimpleReportDTO report, List<TestPlanFailureScenarioDTO> scenarios, JSONObject reportConfig) {

        if (!CollectionUtils.isEmpty(scenarios)) {
            List<TestPlanFailureScenarioDTO> failureScenarios = new ArrayList<>();
            List<TestPlanFailureScenarioDTO> errorReportScenarios = new ArrayList<>();
            List<TestPlanFailureScenarioDTO> unExecuteScenarios = new ArrayList<>();
            for (TestPlanFailureScenarioDTO scenario : scenarios) {
                if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), "Fail", "Error")) {
                    failureScenarios.add(scenario);
                } else if (StringUtils.equalsIgnoreCase(scenario.getLastResult(), ExecuteResult.ERROR_REPORT_RESULT.toString())) {
                    errorReportScenarios.add(scenario);
                } else if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), "stop", "unexecute")) {
                    unExecuteScenarios.add(scenario);
                }
            }
            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setScenarioFailureCases(failureScenarios);
            }
            if (checkReportConfig(reportConfig, "api", "errorReport")) {
                report.setErrorReportScenarios(errorReportScenarios);
            }
            if (checkReportConfig(reportConfig, "api", "unExecute")) {
                report.setUnExecuteScenarios(unExecuteScenarios);
            }
        }
    }

    private void screenApiCaseByStatusAndReportConfig(TestPlanSimpleReportDTO report, List<TestPlanFailureApiDTO> apiAllCases, JSONObject reportConfig) {
        if (!CollectionUtils.isEmpty(apiAllCases)) {
            List<TestPlanFailureApiDTO> apiFailureCases = new ArrayList<>();
            List<TestPlanFailureApiDTO> apiErrorReportCases = new ArrayList<>();
            List<TestPlanFailureApiDTO> apiUnExecuteCases = new ArrayList<>();
            for (TestPlanFailureApiDTO apiDTO : apiAllCases) {
                if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), "error")) {
                    apiFailureCases.add(apiDTO);
                } else if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ExecuteResult.ERROR_REPORT_RESULT.toString())) {
                    apiErrorReportCases.add(apiDTO);
                } else if (StringUtils.equalsAnyIgnoreCase(apiDTO.getExecResult(), "stop", "unexecute")) {
                    apiUnExecuteCases.add(apiDTO);
                }
            }

            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setApiFailureCases(apiFailureCases);
            }
            if (checkReportConfig(reportConfig, "api", "errorReport")) {
                report.setErrorReportCases(apiErrorReportCases);
            }
            if (checkReportConfig(reportConfig, "api", "unExecute")) {
                report.setUnExecuteCases(apiUnExecuteCases);
            }

        }
    }

    public void buildLoadReport(TestPlanSimpleReportDTO report, JSONObject config, Map<String, String> loadCaseReportMap, boolean saveResponse) {
        if (MapUtils.isEmpty(loadCaseReportMap)) {
            report.setLoadAllCases(new ArrayList<>());
            return;
        }
        if (checkReportConfig(config, "load")) {
            List<TestPlanLoadCaseDTO> allCases = null;
            if (checkReportConfig(config, "load", "all")) {
                allCases = testPlanLoadCaseService.getAllCases(loadCaseReportMap.keySet(), loadCaseReportMap.values());
                if (saveResponse) {
                    buildLoadResponse(allCases);
                }
                report.setLoadAllCases(allCases);
            }
            if (checkReportConfig(config, "load", "failure")) {
                List<TestPlanLoadCaseDTO> failureCases = null;
                if (!CollectionUtils.isEmpty(allCases)) {
                    failureCases = allCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getStatus())
                                    && StringUtils.equalsAnyIgnoreCase(i.getStatus(), "error"))
                            .collect(Collectors.toList());
                }
                report.setLoadFailureCases(failureCases);
            }
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
            JSONObject config = null;
            if (StringUtils.isNotBlank(reportConfig)) {
                config = JSONObject.parseObject(reportConfig);
            }
            TestPlanExecuteReportDTO testPlanExecuteReportDTO = testPlanReportService.genTestPlanExecuteReportDTOByTestPlanReportContent(testPlanReportContentWithBLOBs);
            TestPlanSimpleReportDTO report = null;
            boolean apiBaseInfoChanged = false;
            if (StringUtils.isEmpty(testPlanReportContentWithBLOBs.getApiBaseCount())) {
                report = getReport(testPlanReport.getTestPlanId(), testPlanExecuteReportDTO);
                apiBaseInfoChanged = true;
            } else {
                try {
                    report = JSONObject.parseObject(testPlanReportContentWithBLOBs.getApiBaseCount(), TestPlanSimpleReportDTO.class);
                } catch (Exception e) {
                    LogUtil.info("解析接口统计数据出错！数据：" + testPlanReportContentWithBLOBs.getApiBaseCount(), e);
                }
                if (report == null) {
                    report = getReport(testPlanReport.getTestPlanId(), testPlanExecuteReportDTO);
                    apiBaseInfoChanged = true;
                }
            }
            if (report.getFunctionAllCases() == null || report.getIssueList() == null) {
                buildFunctionalReport(report, config, testPlanReport.getTestPlanId());
                apiBaseInfoChanged = true;
            }
            if (report.getApiAllCases() == null && report.getScenarioAllCases() == null) {
                buildApiReport(report, config, testPlanExecuteReportDTO);
                apiBaseInfoChanged = true;
            }
            if (report.getLoadAllCases() == null) {
                buildLoadReport(report, config, testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap(), false);
                apiBaseInfoChanged = true;
            }
            returnDTO.setTestPlanSimpleReportDTO(report);

            if (apiBaseInfoChanged) {
                testPlanReportContentWithBLOBs.setApiBaseCount(JSONObject.toJSONString(report));
                returnDTO.setApiBaseInfoChanged(true);
            }
            return returnDTO;
        } else {
            returnDTO.setTestPlanSimpleReportDTO(new TestPlanSimpleReportDTO());
            return returnDTO;
        }
    }

    public boolean checkAllReportFinished(TestPlanSimpleReportDTO testPlanSimpleReportDTO) {
        if(CollectionUtils.isNotEmpty(testPlanSimpleReportDTO.getScenarioAllCases())){
            for (TestPlanFailureScenarioDTO dto: testPlanSimpleReportDTO.getScenarioAllCases()) {
                if(StringUtils.equalsAnyIgnoreCase(dto.getLastResult(),"Waiting","Running")){
                    return false;
                }
            }
        }
        return true;
    }
    public TestPlanSimpleReportDTO buildPlanReport(String planId, boolean saveResponse) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(planId);

        String reportConfig = testPlan.getReportConfig();
        JSONObject config = null;
        if (StringUtils.isNotBlank(reportConfig)) {
            config = JSONObject.parseObject(reportConfig);
        }
        TestPlanSimpleReportDTO report = getReport(planId, null);
        buildFunctionalReport(report, config, planId);
        buildApiReport(report, config, planId, saveResponse);
        buildLoadReport(report, config, planId, saveResponse);
        return report;
    }

    public void exportPlanReport(String planId, String lang, HttpServletResponse response) throws UnsupportedEncodingException {
        TestPlanSimpleReportDTO report = buildPlanReport(planId, true);
        report.setLang(lang);
        render(report, response);
    }

    public void exportPlanDbReport(String reportId, String lang, HttpServletResponse response) throws UnsupportedEncodingException {
        TestPlanSimpleReportDTO report = testPlanReportService.getReport(reportId);
        buildApiResponse(report.getApiAllCases());
        buildApiResponse(report.getApiFailureCases());
        buildScenarioResponse(report.getScenarioAllCases());
        buildScenarioResponse(report.getScenarioFailureCases());
        buildLoadResponse(report.getLoadAllCases());
        report.setLang(lang);
        render(report, response);
    }

    public Boolean checkReportConfig(JSONObject config, String key) {
        if (config == null) {
            return true;
        } else {
            JSONObject configItem = config.getJSONObject(key);
            return configItem.getBoolean("enable");
        }
    }

    public Boolean checkReportConfig(JSONObject config, String key, String subKey) {
        if (config == null) {
            return true;
        } else {
            JSONObject configItem = config.getJSONObject(key);
            Boolean enable = configItem.getBoolean("enable");
            if (!enable) {
                return false;
            } else {
                JSONObject subConfig = configItem.getJSONObject("children").getJSONObject(subKey);
                return subConfig == null ? true : subConfig.getBoolean("enable");
            }
        }
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
                line += "\n";
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
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
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
        report.setFunctionResult(functionResult);
        report.setApiResult(apiResult);
        report.setStartTime(testPlan.getActualStartTime());
        report.setEndTime(testPlan.getActualEndTime());
        report.setSummary(testPlan.getReportSummary());
        report.setConfig(testPlan.getReportConfig());
        testPlanTestCaseService.calculatePlanReport(planId, report);
        issuesService.calculatePlanReport(planId, report);
        if (testPlanExecuteReportDTO == null) {
            testPlanApiCaseService.calculatePlanReport(planId, report);
            testPlanScenarioCaseService.calculatePlanReport(planId, report);
            testPlanLoadCaseService.calculatePlanReport(planId, report);
        } else {
            if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap())) {
                testPlanApiCaseService.calculatePlanReport(new ArrayList<>(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap().values()), report);
            }

            if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
                testPlanScenarioCaseService.calculatePlanReport(new ArrayList<>(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap().values()), report);
            }

            if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap())) {
                testPlanLoadCaseService.calculatePlanReport(new ArrayList<>(testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap().values()), report);
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
        Project project = projectService.getProjectById(testPlan.getProjectId());
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

        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> apiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getId).collect(Collectors.toList());
        envMap = this.getApiCaseEnv(apiCaseIds);

        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(scenarioExample);
        List<String> scenarioIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getId).collect(Collectors.toList());
        Map<String, List<String>> scenarioEnv = this.getApiScenarioEnv(scenarioIds);

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
        //检查是否有可以执行的用例
        if (!haveExecCase(testplanRunRequest.getTestPlanId())) {
            MSException.throwException(Translator.get("plan_warning"));
        }
        String envType = testplanRunRequest.getEnvironmentType();
        Map<String, String> envMap = testplanRunRequest.getEnvMap();
        String environmentGroupId = testplanRunRequest.getEnvironmentGroupId();
        String testPlanId = testplanRunRequest.getTestPlanId();
        RunModeConfigDTO runModeConfig = getRunModeConfigDTO(testplanRunRequest, envType, envMap, environmentGroupId, testPlanId);
        String apiRunConfig = JSONObject.toJSONString(runModeConfig);
        return this.run(testPlanId, testplanRunRequest.getProjectId(),
                testplanRunRequest.getUserId(), testplanRunRequest.getTriggerMode(), testplanRunRequest.getReportId(), apiRunConfig);

    }

    private RunModeConfigDTO getRunModeConfigDTO(TestPlanRunRequest testplanRunRequest, String envType, Map<String, String> envMap, String environmentGroupId, String testPlanId) {
        RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
        runModeConfig.setEnvironmentType(testplanRunRequest.getEnvironmentType());
        if (StringUtils.equals(envType, EnvironmentType.JSON.name()) && !envMap.isEmpty()) {
            runModeConfig.setEnvMap(testplanRunRequest.getEnvMap());
            this.setPlanCaseEnv(testPlanId, runModeConfig);
        } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
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
        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> planApiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getId).collect(Collectors.toList());
        Map<String, String> envMap = runModeConfig.getEnvMap();
        String envType = runModeConfig.getEnvironmentType();
        String environmentGroupId = runModeConfig.getEnvironmentGroupId();
        if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
            envMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
        }

        testPlanApiCaseService.setApiCaseEnv(testPlanApiCases, planApiCaseIds, envMap);

        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);

        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(scenarioExample);
        List<String> planScenarioIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getId).collect(Collectors.toList());
        testPlanScenarioCaseService.setScenarioEnv(testPlanApiScenarios, planScenarioIds, runModeConfig);
    }

    public void editReportConfig(TestPlanDTO testPlanDTO) {
        TestPlanWithBLOBs testPlan = new TestPlanWithBLOBs();
        testPlan.setId(testPlanDTO.getId());
        testPlan.setReportConfig(testPlanDTO.getReportConfig());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    public boolean haveExecCase(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        List<String> ids = new ArrayList<>();
        ids.add(id);
        List<TestPlanApiCase> testPlanApiCases = extTestPlanApiCaseMapper.selectByIdsAndStatusIsNotTrash(ids);
        if (!CollectionUtils.isEmpty(testPlanApiCases)) {
            return true;
        }

        List<TestPlanApiScenario> testPlanApiScenarios = extTestPlanApiScenarioMapper.selectByIdsAndStatusIsNotTrash(ids);
        if (!CollectionUtils.isEmpty(testPlanApiScenarios)) {
            return true;
        }

        TestPlanLoadCaseExample loadCaseExample = new TestPlanLoadCaseExample();
        loadCaseExample.createCriteria().andTestPlanIdEqualTo(id);
        List<TestPlanLoadCase> testPlanLoadCases = testPlanLoadCaseMapper.selectByExample(loadCaseExample);
        return !CollectionUtils.isEmpty(testPlanLoadCases);
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

    public JSONArray getStageOption(String projectId) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andSceneEqualTo("PLAN")
                .andNameEqualTo("测试阶段");

        List<CustomField> customFields = customFieldMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(customFields)) {
            example.clear();
            example.createCriteria()
                    .andGlobalEqualTo(true)
                    .andSceneEqualTo("PLAN")
                    .andNameEqualTo("测试阶段");
            customFields = customFieldMapper.selectByExampleWithBLOBs(example);
        }
        return JSONArray.parseArray(customFields.get(0).getOptions());
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
            Schedule schedule = scheduleService.getSchedule(id);
            schedule.setEnable(request.isEnable());
            apiAutomationService.updateSchedule(schedule);
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

    public ScheduleDTO updateTestPlanBySchedule(ScheduleInfoRequest request) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        Schedule schedule = scheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(request.isEnable());
        apiAutomationService.updateSchedule(schedule);
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

        if (StringUtils.isNotEmpty(haveExecCaseBuilder)) {
            MSException.throwException(Translator.get("track_test_plan") + ": " + haveExecCaseBuilder.toString() + " :" + Translator.get("plan_warning"));
        }
        if (StringUtils.isNotEmpty(stringBuilder)) {
            MSException.throwException(stringBuilder.toString());
        }

        for (String id : ids) {
            TestPlanWithBLOBs testPlan = testPlanMap.get(id);
            RunModeConfigDTO runModeConfigDTO = JSON.parseObject(testPlan.getRunModeConfig(), RunModeConfigDTO.class);
            runModeConfigDTO = ObjectUtils.isEmpty(runModeConfigDTO) ? new RunModeConfigDTO() : runModeConfigDTO;
            jMeterService.verifyPool(testPlan.getProjectId(), runModeConfigDTO);

            String planReportId = UUID.randomUUID().toString();
            //创建测试报告
            this.genTestPlanReport(planReportId, testPlan.getId(), request.getUserId(), request.getTriggerMode());
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
                        JSONObject jsonObject = JSONObject.parseObject(testPlan.getRunModeConfig());
                        TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                        TestPlanRunRequest runRequest = JSON.toJavaObject(jsonObject, TestPlanRunRequest.class);
                        runRequest.setReportId(planExecutionQueue.getReportId());
                        runPlan(runRequest);
                    } else {
                        for (TestPlanExecutionQueue planExecutionQueue : planExecutionQueues) {
                            TestPlanWithBLOBs testPlan = testPlanMap.get(planExecutionQueue.getTestPlanId());
                            JSONObject jsonObject = JSONObject.parseObject(testPlan.getRunModeConfig());
                            TestPlanRequestUtil.changeStringToBoolean(jsonObject);
                            TestPlanRunRequest runRequest = JSON.toJavaObject(jsonObject, TestPlanRunRequest.class);
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
}
