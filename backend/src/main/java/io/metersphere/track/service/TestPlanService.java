package io.metersphere.track.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.datacount.request.ScheduleInfoRequest;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
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
import io.metersphere.track.Factory.ReportComponentFactory;
import io.metersphere.track.domain.ReportComponent;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.testcase.PlanCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.AddTestPlanRequest;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.TestplanRunRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import java.text.SimpleDateFormat;
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
    ExtApiScenarioMapper extApiScenarioMapper;
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
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
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
        List<String> follows = request.getFollows();
        editTestFollows(request.getId(), follows);
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

            List<String> functionalExecResults = extTestPlanTestCaseMapper.getExecResultByPlanId(testPlan.getId());
            functionalExecResults.forEach(item -> {
                if (!StringUtils.equals(item, TestPlanTestCaseStatus.Prepare.name())
                        && !StringUtils.equals(item, TestPlanTestCaseStatus.Underway.name())) {
                    testPlan.setTested(testPlan.getTested() + 1);
                    if (StringUtils.equals(item, TestPlanTestCaseStatus.Pass.name())) {
                        testPlan.setPassed(testPlan.getPassed() + 1);
                    }
                }
            });

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

            testPlan.setTotal(apiExecResults.size() + scenarioExecResults.size() + functionalExecResults.size() + loadResults.size());

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
        testPlans.forEach(item -> {
            TestPlanReportExample example = new TestPlanReportExample();
            example.createCriteria().andTestPlanIdEqualTo(item.getId());
            item.setExecutionTimes((int) testPlanReportMapper.countByExample(example));
            if (StringUtils.isNotBlank(item.getScheduleId())) {
                if (item.isScheduleOpen()) {
                    item.setScheduleStatus(ScheduleStatus.OPEN.name());
                    Schedule schedule = scheduleService.getScheduleByResource(item.getId(), ScheduleGroup.TEST_PLAN_TEST.name());
                    item.setScheduleCorn(schedule.getValue());
                    item.setScheduleExecuteTime(getNextTriggerTime(schedule.getValue()));
                } else {
                    item.setScheduleStatus(ScheduleStatus.SHUT.name());
                }
            } else {
                item.setScheduleStatus(ScheduleStatus.NOTSET.name());
            }
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
            testPlanWithBLOBs.setStatus(TestPlanStatus.Finished.name());
            editTestPlan(testPlanWithBLOBs);
        } else if (prepareNum != 0) {    //  进行中
            testPlanWithBLOBs.setStatus(TestPlanStatus.Underway.name());
            editTestPlan(testPlanWithBLOBs);
        }
    }

    public void checkStatus(TestPlanWithBLOBs testPlanWithBLOBs) { //  检查执行结果，自动更新计划状态
        List<String> statusList = new ArrayList<>();
        statusList.addAll(extTestPlanTestCaseMapper.getExecResultByPlanId(testPlanWithBLOBs.getId()));
        statusList.addAll(testPlanApiCaseService.getExecResultByPlanId(testPlanWithBLOBs.getId()));
        statusList.addAll(testPlanScenarioCaseService.getExecResultByPlanId(testPlanWithBLOBs.getId()));
        statusList.addAll(testPlanLoadCaseService.getStatus(testPlanWithBLOBs.getId()));
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
            testPlanWithBLOBs.setStatus(TestPlanStatus.Finished.name());
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

    private String getTestPlanContext(TestPlan testPlan, String type) {
        User user = userMapper.selectByPrimaryKey(testPlan.getCreator());
        Long startTime = testPlan.getPlannedStartTime();
        Long endTime = testPlan.getPlannedEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        String eTime = String.valueOf(endTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        } else {
            start = "未设置";
        }
        String end = null;
        if (!eTime.equals("null")) {
            end = sdf.format(new Date(Long.parseLong(eTime)));
        } else {
            end = "未设置";
        }
        String context = "";
        if (StringUtils.equals(NoticeConstants.Event.CREATE, type)) {
            context = "测试计划任务通知：" + user.getName() + "创建的" + "'" + testPlan.getName() + "'" + "待开始，计划开始时间是:" + "'" + start + "'" + ";" + "计划结束时间是:" + "'" + end + "'" + " " + "请跟进";
        } else if (StringUtils.equals(NoticeConstants.Event.UPDATE, type)) {
            String status = "";
            if (StringUtils.equals(TestPlanStatus.Underway.name(), testPlan.getStatus())) {
                status = "进行中";
            } else if (StringUtils.equals(TestPlanStatus.Prepare.name(), testPlan.getStatus())) {
                status = "未开始";
            } else if (StringUtils.equals(TestPlanStatus.Completed.name(), testPlan.getStatus())) {
                status = "已完成";
            }
            context = "测试计划任务通知：" + user.getName() + "创建的" + "'" + testPlan.getName() + "'" + "计划开始时间是:" + "'" + start + "'" + ";" + "计划结束时间是:" + "'" + end + "'" + " " + status;
        } else if (StringUtils.equals(NoticeConstants.Event.DELETE, type)) {
            context = "测试计划任务通知：" + user.getName() + "创建的" + "'" + testPlan.getName() + "'" + "计划开始时间是:" + "'" + start + "'" + ";" + "计划结束时间是:" + "'" + end + "'" + " " + "已删除";
        }
        return context;
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
    TestPlanScheduleReportInfoDTO genTestPlanReport(String projectID, String planId, String userId, String triggerMode) {
        TestPlanScheduleReportInfoDTO reportInfoDTO = testPlanReportService.genTestPlanReportBySchedule(projectID, planId, userId, triggerMode);
        return reportInfoDTO;
    }

    public String run(String testPlanID, String projectID, String userId, String triggerMode, String apiRunConfig) {
        RunModeConfigDTO runModeConfig = null;
        try {
            runModeConfig = JSONObject.parseObject(apiRunConfig, RunModeConfigDTO.class);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (runModeConfig == null) {
            runModeConfig = new RunModeConfigDTO();
            runModeConfig.setMode("serial");
            runModeConfig.setReportType("iddReport");
            runModeConfig.setEnvMap(new HashMap<>());
            runModeConfig.setOnSampleError(false);
        } else {
            if (runModeConfig.getEnvMap() == null) {
                runModeConfig.setEnvMap(new HashMap<>());
            }
        }

        //创建测试报告，然后返回的ID重新赋值为resourceID，作为后续的参数
        TestPlanScheduleReportInfoDTO reportInfoDTO = this.genTestPlanReport(projectID, testPlanID, userId, triggerMode);

        //测试计划准备执行，取消测试计划的实际结束时间
        extTestPlanMapper.updateActualEndTimeIsNullById(testPlanID);

        String planReportId = reportInfoDTO.getTestPlanReport().getId();
        testPlanLog.info("ReportId[" + planReportId + "] created. TestPlanID:[" + testPlanID + "]. " + "API Run Config:【" + apiRunConfig + "】");


        //执行接口案例任务
        LoggerUtil.info("开始执行测试计划接口用例 " + planReportId);
        Map<String, String> apiCaseReportMap = this.executeApiTestCase(triggerMode, planReportId, userId, new ArrayList<>(reportInfoDTO.getApiTestCaseDataMap().keySet()), runModeConfig);
        //执行场景执行任务
        LoggerUtil.info("开始执行测试计划场景用例 " + planReportId);
        Map<String, String> scenarioReportMap = this.executeScenarioCase(planReportId, testPlanID, projectID, runModeConfig, triggerMode, userId, reportInfoDTO.getPlanScenarioIdMap());
        //执行性能测试任务
        LoggerUtil.info("开始执行测试计划性能用例 " + planReportId);
        Map<String, String> loadCaseReportMap = this.executeLoadCaseTask(runModeConfig, triggerMode, reportInfoDTO.getPerformanceIdMap());
        LoggerUtil.info("开始生成测试计划报告 " + planReportId);
        testPlanReportService.createTestPlanReportContentReportIds(planReportId, apiCaseReportMap, scenarioReportMap, loadCaseReportMap);
        return planReportId;
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
                scenarioRequest.setTriggerMode(ReportTriggerMode.API.name());
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
        Map<String, String> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(dtoList)) {
            dtoList.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getTestId()) && StringUtils.isNotEmpty(item.getReportId())) {
                    returnMap.put(item.getTestId(), item.getReportId());
                }
            });
        }
        return returnMap;
    }

    private Map<String, String> executeLoadCaseTask(RunModeConfigDTO runModeConfig, String triggerMode, Map<String, String> performanceIdMap) {
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
            } else if (StringUtils.equals(ReportTriggerMode.MANUAL.name(), triggerMode)) {
                performanceRequest.setTriggerMode(ReportTriggerMode.MANUAL.name());
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
        return loadCaseReportMap;
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
                    nextTestCaseOrder += 5000;
                    testCaseMapper.insert(testPlanTestCase);
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
            cases.forEach(item -> {
                APIReportResult dbResult = apiDefinitionService.getDbResult(item.getId());
                if (dbResult != null && StringUtils.isNotBlank(dbResult.getContent())) {
                    item.setResponse(dbResult.getContent());
                }
            });
        }
    }

    public void buildScenarioResponse(List<TestPlanFailureScenarioDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            cases.forEach((item) -> {
                item.setResponse(apiScenarioReportService.get(item.getReportId()));
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
        if (MapUtils.isEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap()) && MapUtils.isEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
            return;
        }
        if (checkReportConfig(config, "api")) {
            List<TestPlanFailureApiDTO> apiAllCases = null;
            List<TestPlanFailureScenarioDTO> scenarioAllCases = null;
            if (checkReportConfig(config, "api", "all")) {
                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap())) {
                    // 接口
                    apiAllCases = testPlanApiCaseService.getByApiExecReportIds(testPlanExecuteReportDTO.getTestPlanApiCaseIdAndReportIdMap());
                    report.setApiAllCases(apiAllCases);
                }
                if (MapUtils.isNotEmpty(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap())) {
                    //场景
                    scenarioAllCases = testPlanScenarioCaseService.getAllCases(testPlanExecuteReportDTO.getTestPlanScenarioIdAndReportIdMap());
                    report.setScenarioAllCases(scenarioAllCases);
                }
            }

            //筛选符合配置需要的执行结果的用例和场景
            this.screenApiCaseByStatusAndReportConfig(report, apiAllCases, config);
            this.screenScenariosByStatusAndReportConfig(report, scenarioAllCases, config);
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
                } else if (StringUtils.equalsIgnoreCase(scenario.getLastResult(), ExecuteResult.errorReportResult.name())) {
                    errorReportScenarios.add(scenario);
                } else if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), "stop","unexecute")) {
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
                } else if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ExecuteResult.errorReportResult.name())) {
                    apiErrorReportCases.add(apiDTO);
                } else if (StringUtils.equalsAnyIgnoreCase(apiDTO.getExecResult(), "stop","unexecute")) {
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

    public TestPlanSimpleReportDTO buildPlanReport(TestPlanReport testPlanReport, TestPlanReportContentWithBLOBs testPlanReportContentWithBLOBs) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
        if (testPlan != null) {
            String reportConfig = testPlan.getReportConfig();
            JSONObject config = null;
            if (StringUtils.isNotBlank(reportConfig)) {
                config = JSONObject.parseObject(reportConfig);
            }
            TestPlanExecuteReportDTO testPlanExecuteReportDTO = testPlanReportService.genTestPlanExecuteReportDTOByTestPlanReportContent(testPlanReportContentWithBLOBs);
            TestPlanSimpleReportDTO report = getReport(testPlanReport.getTestPlanId(), testPlanExecuteReportDTO);
            buildFunctionalReport(report, config, testPlanReport.getTestPlanId());
            buildApiReport(report, config, testPlanExecuteReportDTO);
            buildLoadReport(report, config, testPlanExecuteReportDTO.getTestPlanLoadCaseIdAndReportIdMap(), false);
            return report;
        } else {
            return null;
        }

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

    public void exportPlanReport(String planId, HttpServletResponse response) throws UnsupportedEncodingException {
        render(buildPlanReport(planId, true), response);
    }

    public void exportPlanDbReport(String reportId, HttpServletResponse response) throws UnsupportedEncodingException {
        TestPlanSimpleReportDTO report = testPlanReportService.getReport(reportId);
        buildApiResponse(report.getApiAllCases());
        buildApiResponse(report.getApiFailureCases());
        buildScenarioResponse(report.getScenarioAllCases());
        buildScenarioResponse(report.getScenarioFailureCases());
        buildLoadResponse(report.getLoadAllCases());
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
            report.setPassRate(report.getPassCount() * 0.1 * 10 / report.getExecuteCount());
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

    public String runPlan(TestplanRunRequest testplanRunRequest) {
        String envType = testplanRunRequest.getEnvironmentType();
        Map<String, String> envMap = testplanRunRequest.getEnvMap();
        String environmentGroupId = testplanRunRequest.getEnvironmentGroupId();
        RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
        runModeConfig.setEnvironmentType(testplanRunRequest.getEnvironmentType());
        if (StringUtils.equals(envType, EnvironmentType.JSON.name()) && !envMap.isEmpty()) {
            runModeConfig.setEnvMap(testplanRunRequest.getEnvMap());
            this.setPlanCaseEnv(testplanRunRequest.getTestPlanId(), runModeConfig);
        } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
            runModeConfig.setEnvironmentGroupId(testplanRunRequest.getEnvironmentGroupId());
            this.setPlanCaseEnv(testplanRunRequest.getTestPlanId(), runModeConfig);
        }

        runModeConfig.setMode(testplanRunRequest.getMode());
        runModeConfig.setResourcePoolId(testplanRunRequest.getResourcePoolId());
        runModeConfig.setOnSampleError(Boolean.parseBoolean(testplanRunRequest.getOnSampleError()));
        if (StringUtils.isBlank(testplanRunRequest.getReportType())) {
            runModeConfig.setReportType("iddReport");
        } else {
            runModeConfig.setReportType(testplanRunRequest.getReportType());
        }
        String apiRunConfig = JSONObject.toJSONString(runModeConfig);
        return this.run(testplanRunRequest.getTestPlanId(), testplanRunRequest.getProjectId(),
                testplanRunRequest.getUserId(), testplanRunRequest.getTriggerMode(), apiRunConfig);
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

        testPlanApiCaseService.setApiCaseEnv(planApiCaseIds, envMap);

        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(scenarioExample);
        List<String> planScenarioIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getId).collect(Collectors.toList());
        testPlanScenarioCaseService.setScenarioEnv(planScenarioIds, runModeConfig);
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
        TestPlanApiCaseExample apiCaseExample = new TestPlanApiCaseExample();
        apiCaseExample.createCriteria().andTestPlanIdEqualTo(id);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(apiCaseExample);
        if (!CollectionUtils.isEmpty(testPlanApiCases)) {
            return true;
        }

        TestPlanApiScenarioExample apiScenarioExample = new TestPlanApiScenarioExample();
        apiScenarioExample.createCriteria().andTestPlanIdEqualTo(id);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(apiScenarioExample);
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
        return time1.getTime();
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
}
