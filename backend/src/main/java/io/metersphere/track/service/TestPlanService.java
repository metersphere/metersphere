package io.metersphere.track.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.metersphere.api.cache.TestPlanExecuteInfo;
import io.metersphere.api.cache.TestPlanReportExecuteCatch;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.IssueTemplateDao;
import io.metersphere.dto.LogDetailDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestPlanReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.performance.base.*;
import io.metersphere.performance.dto.LoadTestExportJmx;
import io.metersphere.performance.dto.MetricData;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.MetricQueryService;
import io.metersphere.performance.service.PerformanceReportService;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.IssueTemplateService;
import io.metersphere.service.ScheduleService;
import io.metersphere.service.SystemParameterService;
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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private NoticeSendService noticeSendService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
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
    private IssueTemplateService issueTemplateService;
    @Resource
    private PerformanceReportService performanceReportService;
    @Resource
    private MetricQueryService metricQueryService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    public synchronized TestPlan addTestPlan(AddTestPlanRequest testPlan) {
        if (getTestPlanByName(testPlan.getName()).size() > 0) {
            MSException.throwException(Translator.get("plan_name_already_exists"));
        }
        testPlan.setStatus(TestPlanStatus.Prepare.name());
        testPlan.setCreateTime(System.currentTimeMillis());
        testPlan.setUpdateTime(System.currentTimeMillis());
        testPlan.setCreator(SessionUtils.getUser().getId());
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

        if (StringUtils.isNotBlank(testPlan.getStatus()) && testPlan.getStatus().equals(TestPlanStatus.Prepare.name())) {
            testPlan.setActualStartTime(null);
            testPlan.setActualEndTime(null);
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
        Long startTime = testPlan.getPlannedStartTime();
        Long endTime = testPlan.getPlannedEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        String eTime = String.valueOf(endTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        }
        String end = null;
        if (!eTime.equals("null")) {
            end = sdf.format(new Date(Long.parseLong(eTime)));
        }

        Map<String, Object> context = new HashMap<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        context.put("url", baseSystemConfigDTO.getUrl());
        context.put("name", testPlan.getName());
        context.put("start", start);
        context.put("end", end);
        context.put("id", testPlan.getId());
        String status = "";
        if (StringUtils.equals(TestPlanStatus.Underway.name(), testPlan.getStatus())) {
            status = "进行中";
        } else if (StringUtils.equals(TestPlanStatus.Prepare.name(), testPlan.getStatus())) {
            status = "未开始";
        } else if (StringUtils.equals(TestPlanStatus.Completed.name(), testPlan.getStatus())) {
            status = "已完成";
        }
        context.put("status", status);
        context.put("creator", testPlan.getCreator());
        return context;
    }


    private void checkTestPlanExist(TestPlan testPlan) {
        if (testPlan.getName() != null) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria()
                    .andNameEqualTo(testPlan.getName())
                    .andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId())
                    .andIdNotEqualTo(testPlan.getId());
            if (testPlanMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("plan_name_already_exists"));
            }
        }
    }

    public int deleteTestPlan(String planId) {
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

    private void calcTestPlanRate(List<TestPlanDTOWithMetric> testPlans) {
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
            // 发送成功通知
//            sendCompletedNotice(testPlanWithBLOBs);
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

        ServiceUtils.getSelectAllIds(request, request.getRequest(),
                (query) -> extTestCaseMapper.selectRelateIdsByQuery(query));

        List<String> testCaseIds = request.getIds();

        if (testCaseIds.isEmpty()) {
            return;
        }

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(testCaseIds);
        List<TestCase> testCaseList = testCaseMapper.selectByExample(testCaseExample);
        Map<String, String> userMap = testCaseList.stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getId(), v.getMaintainer()), HashMap::putAll);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanTestCaseMapper batchMapper = sqlSession.getMapper(TestPlanTestCaseMapper.class);

        testCaseIds.forEach(caseId -> {
            TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
            testPlanTestCase.setId(UUID.randomUUID().toString());
            testPlanTestCase.setCreateUser(SessionUtils.getUserId());
            testPlanTestCase.setExecutor(userMap.get(caseId) == null ? SessionUtils.getUserId() : userMap.get(caseId));
            testPlanTestCase.setCaseId(caseId);
            testPlanTestCase.setCreateTime(System.currentTimeMillis());
            testPlanTestCase.setUpdateTime(System.currentTimeMillis());
            testPlanTestCase.setPlanId(request.getPlanId());
            testPlanTestCase.setStatus(TestPlanStatus.Prepare.name());
            batchMapper.insert(testPlanTestCase);
        });

        sqlSession.flushStatements();
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
                    list.forEach(l -> {
                        if (StringUtils.equals(l.getTestType(), TestCaseStatus.performance.name())) {
                            TestPlanLoadCase t = new TestPlanLoadCase();
                            t.setId(UUID.randomUUID().toString());
                            t.setTestPlanId(request.getPlanId());
                            t.setLoadCaseId(l.getTestId());
                            t.setCreateTime(System.currentTimeMillis());
                            t.setUpdateTime(System.currentTimeMillis());
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
                                ApiDefinitionWithBLOBs apidefinition = apiDefinitionMapper.selectByPrimaryKey(apitest.getApiDefinitionId());
                                t.setId(UUID.randomUUID().toString());
                                t.setTestPlanId(request.getPlanId());
                                t.setApiCaseId(l.getTestId());
                                t.setEnvironmentId(apidefinition.getEnvironmentId());
                                t.setCreateTime(System.currentTimeMillis());
                                t.setUpdateTime(System.currentTimeMillis());
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
                                t.setLastResult(testPlanApiScenario.getLastResult());
                                t.setPassRate(testPlanApiScenario.getPassRate());
                                t.setReportId(testPlanApiScenario.getReportId());
                                t.setStatus(testPlanApiScenario.getStatus());
                                t.setCreateTime(System.currentTimeMillis());
                                t.setUpdateTime(System.currentTimeMillis());
                                TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
                                example.createCriteria().andTestPlanIdEqualTo(request.getPlanId()).andApiScenarioIdEqualTo(t.getApiScenarioId());
                                if (testPlanApiScenarioMapper.countByExample(example) <= 0) {
                                    testPlanApiScenarioMapper.insert(t);
                                }
                            }

                        }
                    });
                });
            }
        }
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
        if (StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                || StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Completed.name())) {
            testPlan.setStatus(TestPlanStatus.Underway.name());
            testPlan.setActualStartTime(System.currentTimeMillis());  // 将状态更新为进行中时，开始时间也要更新
            testPlan.setActualEndTime(null);
            testPlanMapper.updateByPrimaryKey(testPlan);
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

    public List<TestPlanDTOWithMetric> listRelateAllPlan() {
        SessionUser user = SessionUtils.getUser();
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setPrincipal(user.getId());
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setProjectId(SessionUtils.getCurrentProjectId());
        request.setPlanIds(extTestPlanTestCaseMapper.findRelateTestPlanId(user.getId(), SessionUtils.getCurrentWorkspaceId(), SessionUtils.getCurrentProjectId()));
        List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.listRelate(request);
        calcTestPlanRate(testPlans);
        return testPlans;
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
        TestPlan testPlans = getTestPlan(planId);

        sendCompletedNotice(testPlans);
    }

    private void sendCompletedNotice(TestPlan testPlan) {
        if (StringUtils.equals(TestPlanStatus.Completed.name(), testPlan.getStatus())) {
            try {
                String context = getTestPlanContext(testPlan, NoticeConstants.Event.UPDATE);
                Map<String, Object> paramMap = getTestPlanParamMap(testPlan);
                NoticeModel noticeModel = NoticeModel.builder()
                        .operator(SessionUtils.getUserId())
                        .context(context)
                        .subject(Translator.get("test_plan_notification"))
                        .mailTemplate("track/TestPlanEnd")
                        .paramMap(paramMap)
                        .event(NoticeConstants.Event.COMPLETE)
                        .build();
                noticeSendService.send(NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
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
            List<IssuesDao> issue = issuesService.getIssues(testCase.getCaseId());
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

    public String scenarioRunModeConfig(SchedulePlanScenarioExecuteRequest planScenarioExecuteRequest) {
        Map<String, Map<String, String>> testPlanScenarioIdMap = planScenarioExecuteRequest.getTestPlanScenarioIDMap();

        String returnStr = null;
        for (Map.Entry<String, Map<String, String>> entry : testPlanScenarioIdMap.entrySet()) {
            Map<String, String> scenarioMap = entry.getValue();

            RunScenarioRequest request = new RunScenarioRequest();
            request.setReportId(planScenarioExecuteRequest.getReportId());
            request.setEnvironmentId(planScenarioExecuteRequest.getEnvironmentId());
            request.setTriggerMode(planScenarioExecuteRequest.getTriggerMode());
            request.setExecuteType(planScenarioExecuteRequest.getExecuteType());
            request.setRunMode(planScenarioExecuteRequest.getRunMode());
            request.setIds(new ArrayList<>(scenarioMap.keySet()));//场景IDS
            request.setReportUserID(planScenarioExecuteRequest.getReportUserID());
            request.setScenarioTestPlanIdMap(scenarioMap);//未知
            request.setConfig(planScenarioExecuteRequest.getConfig());
            request.setTestPlanScheduleJob(true);
            request.setTestPlanReportId(planScenarioExecuteRequest.getTestPlanReportId());
//            request.getConfig().getReportType()
            request.setId(UUID.randomUUID().toString());
            if (request.getConfig() != null) {
                if (request.getConfig().getMode().equals(RunModeConstants.PARALLEL.toString())) {
                    // 校验并发数量
                    int count = 50;
                    BaseSystemConfigDTO dto = systemParameterService.getBaseInfo();
                    if (StringUtils.isNotEmpty(dto.getConcurrency())) {
                        count = Integer.parseInt(dto.getConcurrency());
                    }
                    if (request.getIds().size() > count) {
                        MSException.throwException("并发数量过大，请重新选择！");
                    }
                    returnStr = apiAutomationService.modeRun(request);
                } else {
                    returnStr = apiAutomationService.modeRun(request);
                }
            } else {
                returnStr = apiAutomationService.excute(request);
            }
        }
        return returnStr;
    }

    /**
     * 测试计划的定时任务--执行场景案例
     *
     * @param request
     * @return
     */
    public String runScenarioCase(SchedulePlanScenarioExecuteRequest request) {
        String returnId = "";
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        HashTree jmeterHashTree = new ListedHashTree();
        Map<String, Map<String, String>> testPlanScenarioIdMap = request.getTestPlanScenarioIDMap();

        for (Map.Entry<String, Map<String, String>> entry : testPlanScenarioIdMap.entrySet()) {
            Map<String, String> planScenarioIdMap = entry.getValue();

            try {
                returnId = this.generateHashTreeByScenarioList(testPlan, planScenarioIdMap, request);
            } catch (Exception ex) {
                MSException.throwException(ex.getMessage());
            }

            testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
            String runMode = ApiRunMode.SCHEDULE_SCENARIO_PLAN.name();
            // 调用执行方法
            jMeterService.runLocal(request.getId(), jmeterHashTree, request.getReportId(), runMode);
        }

        return returnId;
    }

    private String generateHashTreeByScenarioList(MsTestPlan testPlan, Map<String, String> planScenarioIdMap, SchedulePlanScenarioExecuteRequest request) throws Exception {
        String returnId = "";
        boolean isFirst = true;
        List<ApiScenarioWithBLOBs> apiScenarios = extApiScenarioMapper.selectIds(new ArrayList<>(planScenarioIdMap.keySet()));
        for (ApiScenarioWithBLOBs item : apiScenarios) {
            String apiScenarioID = item.getId();
            String planScenarioID = planScenarioIdMap.get(apiScenarioID);
            if (StringUtils.isEmpty(planScenarioID)) {
                continue;
            }
            if (item.getStepTotal() == 0) {
                // 只有一个场景且没有测试步骤，则提示
                if (apiScenarios.size() == 1) {
                    MSException.throwException((item.getName() + "，" + Translator.get("automation_exec_info")));
                }
                LogUtil.warn(item.getName() + "，" + Translator.get("automation_exec_info"));
                continue;
            }
            MsThreadGroup group = new MsThreadGroup();
            group.setLabel(item.getName());
            group.setName(UUID.randomUUID().toString());
            // 批量执行的结果直接存储为报告
            if (isFirst) {
                group.setName(request.getId());
                isFirst = false;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JSONObject element = JSON.parseObject(item.getScenarioDefinition());
            MsScenario scenario = JSONObject.parseObject(item.getScenarioDefinition(), MsScenario.class);
            group.setOnSampleError(scenario.getOnSampleError());
            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                        new TypeReference<LinkedList<MsTestElement>>() {
                        });
                scenario.setHashTree(elements);
            }
            if (StringUtils.isNotEmpty(element.getString("variables"))) {
                LinkedList<ScenarioVariable> variables = mapper.readValue(element.getString("variables"),
                        new TypeReference<LinkedList<ScenarioVariable>>() {
                        });
                scenario.setVariables(variables);
            }
            group.setEnableCookieShare(scenario.isEnableCookieShare());
            LinkedList<MsTestElement> scenarios = new LinkedList<>();
            scenarios.add(scenario);
            // 创建场景报告
            //不同的运行模式，第二个参数入参不同
            APIScenarioReportResult report = apiAutomationService.createScenarioReport(group.getName(),
                    planScenarioID + ":" + request.getTestPlanReportId(),
                    item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                    request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig(), null);
            apiScenarioReportMapper.insert(report);
            group.setHashTree(scenarios);
            testPlan.getHashTree().add(group);
            returnId = request.getId();
        }
        return returnId;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String run(String testPlanID, String projectID, String userId, String triggerMode, String apiRunConfig) {
        //创建测试报告，然后返回的ID重新赋值为resourceID，作为后续的参数
        TestPlanScheduleReportInfoDTO reportInfoDTO = testPlanReportService.genTestPlanReportBySchedule(projectID, testPlanID, userId, triggerMode);

        TestPlanReport testPlanReport = reportInfoDTO.getTestPlanReport();
        Map<String, String> planScenarioIdMap = reportInfoDTO.getPlanScenarioIdMap();
        Map<ApiTestCaseWithBLOBs, String> apiTestCaseDataMap = reportInfoDTO.getApiTestCaseDataMap();
        Map<String, String> performanceIdMap = reportInfoDTO.getPerformanceIdMap();

        String planReportId = testPlanReport.getId();

        testPlanLog.info("ReportId[" + planReportId + "] created. TestPlanID:[" + testPlanID + "]. " + "API Run Config:【" + apiRunConfig + "】");

        //不同任务的执行ID
        Map<String, String> executePerformanceIdMap = new HashMap<>();
        Map<String, String> executeApiCaseIdMap = new HashMap<>();
        Map<String, String> executeScenarioCaseIdMap = new HashMap<>();

        //执行性能测试任务
        Map<String, String> performaneReportIDMap = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : performanceIdMap.entrySet()) {
            String id = entry.getKey();
            String caseID = entry.getValue();
            RunTestPlanRequest performanceRequest = new RunTestPlanRequest();
            performanceRequest.setId(caseID);
            performanceRequest.setTestPlanLoadId(caseID);
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
                    performaneReportIDMap.put(reportId, caseID);
                    TestPlanLoadCase testPlanLoadCase = new TestPlanLoadCase();
                    testPlanLoadCase.setId(performanceRequest.getTestPlanLoadId());
                    testPlanLoadCase.setLoadReportId(reportId);
                    testPlanLoadCaseService.update(testPlanLoadCase);

                    //更新关联处的报告
                    TestPlanLoadCase loadCase = new TestPlanLoadCaseDTO();
                    loadCase.setId(id);
                    loadCase.setLoadReportId(reportId);
                    loadCase.setStatus("run");
                    testPlanLoadCaseService.update(loadCase);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (StringUtils.isNotEmpty(reportId)) {
                executePerformanceIdMap.put(caseID, TestPlanApiExecuteStatus.RUNNING.name());
            }else {
                executePerformanceIdMap.put(caseID, TestPlanApiExecuteStatus.PREPARE.name());
            }
        }
        if (!performaneReportIDMap.isEmpty()) {
            //性能测试时保存性能测试报告ID，在结果返回时用于捕捉并进行
            testPlanReportService.updatePerformanceInfo(testPlanReport, performaneReportIDMap, triggerMode);

        }

        for (Map.Entry<ApiTestCaseWithBLOBs, String> entry : apiTestCaseDataMap.entrySet()) {
            ApiTestCaseWithBLOBs model = entry.getKey();
            executeApiCaseIdMap.put(model.getId(), TestPlanApiExecuteStatus.RUNNING.name());
        }
        for (String id : planScenarioIdMap.keySet()) {
            executeScenarioCaseIdMap.put(id, TestPlanApiExecuteStatus.RUNNING.name());
        }
        testPlanLog.info("ReportId[" + planReportId + "] start run. TestPlanID:[" + testPlanID + "].  Execute api :" + JSONObject.toJSONString(executeApiCaseIdMap) + "; Execute scenario:" + JSONObject.toJSONString(executeScenarioCaseIdMap) + "; Execute performance:" + JSONObject.toJSONString(executePerformanceIdMap));
        TestPlanReportExecuteCatch.updateApiTestPlanExecuteInfo(planReportId, executeApiCaseIdMap, executeScenarioCaseIdMap, executePerformanceIdMap);

        //执行接口案例任务
        this.executeApiTestCase(triggerMode, planReportId, testPlanID, apiTestCaseDataMap);
        //执行场景执行任务
        this.executeScenarioCase(planReportId, testPlanID, projectID, apiRunConfig, triggerMode, userId, planScenarioIdMap);
        this.listenTaskExecuteStatus(planReportId);
        return testPlanReport.getId();
    }

    private void listenTaskExecuteStatus(String planReportId) {
        executorService.submit(() -> {
            try {
                Thread.sleep(30000);
                while (TestPlanReportExecuteCatch.getTestPlanExecuteInfo(planReportId) != null) {
                    testPlanReportService.countReport(planReportId);
                    Thread.sleep(30000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }

    private void executeApiTestCase(String triggerMode, String planReportId, String testPlanId, Map<ApiTestCaseWithBLOBs, String> apiTestCaseDataMap) {
        executorService.submit(() -> {
            Map<String, String> executeErrorMap = new HashMap<>();
            for (Map.Entry<ApiTestCaseWithBLOBs, String> entry : apiTestCaseDataMap.entrySet()) {
                ApiTestCaseWithBLOBs blobs = entry.getKey();
                try {
                    if (StringUtils.equals(triggerMode, ReportTriggerMode.API.name())) {
                        apiTestCaseService.run(blobs, UUID.randomUUID().toString(), planReportId, testPlanId, ApiRunMode.JENKINS_API_PLAN.name());
                    } else if (StringUtils.equals(triggerMode, ReportTriggerMode.MANUAL.name())) {
                        apiTestCaseService.run(blobs, UUID.randomUUID().toString(), planReportId, testPlanId, ApiRunMode.MANUAL_PLAN.name());
                    } else {
                        apiTestCaseService.run(blobs, UUID.randomUUID().toString(), planReportId, testPlanId, ApiRunMode.SCHEDULE_API_PLAN.name());
                    }
                } catch (Exception e) {
                    executeErrorMap.put(blobs.getId(), TestPlanApiExecuteStatus.FAILD.name());
                }
            }

            if (!executeErrorMap.isEmpty()) {
                TestPlanReportExecuteCatch.updateApiTestPlanExecuteInfo(planReportId, executeErrorMap, null, null);
            }
        });
    }

    private void executeScenarioCase(String planReportId, String testPlanID, String projectID, String apiRunConfig, String triggerMode, String userId, Map<String, String> planScenarioIdMap) {
        executorService.submit(() -> {
            if (!planScenarioIdMap.isEmpty()) {
                SchedulePlanScenarioExecuteRequest scenarioRequest = new SchedulePlanScenarioExecuteRequest();
                String senarionReportID = UUID.randomUUID().toString();
                scenarioRequest.setId(senarionReportID);
                scenarioRequest.setReportId(senarionReportID);
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
                RunModeConfig runModeConfig = null;
                try {
                    runModeConfig = JSONObject.parseObject(apiRunConfig, RunModeConfig.class);
                    runModeConfig.setOnSampleError(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (runModeConfig == null) {
                    runModeConfig = new RunModeConfig();
                    runModeConfig.setMode("serial");
                    runModeConfig.setReportType("iddReport");
                    runModeConfig.setOnSampleError(false);
                }

                scenarioRequest.setConfig(runModeConfig);
                this.scenarioRunModeConfig(scenarioRequest);
            }
        });
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
        targetPlan.setId(targetPlanId);
        targetPlan.setName(testPlan.getName() + "_COPY");
        targetPlan.setWorkspaceId(testPlan.getWorkspaceId());
        targetPlan.setDescription(testPlan.getDescription());
        targetPlan.setStage(testPlan.getStage());
        targetPlan.setPrincipal(testPlan.getPrincipal());
        targetPlan.setTags(testPlan.getTags());
        targetPlan.setProjectId(testPlan.getProjectId());
        testPlan.setAutomaticStatusUpdate(testPlan.getAutomaticStatusUpdate());
        targetPlan.setStatus(TestPlanStatus.Prepare.name());
        targetPlan.setCreator(SessionUtils.getUserId());
        targetPlan.setCreateTime(System.currentTimeMillis());
        targetPlan.setUpdateTime(System.currentTimeMillis());
        testPlanMapper.insert(targetPlan);

        copyPlanCase(sourcePlanId, targetPlanId);

        return targetPlan;
    }

    @Transactional(rollbackFor = Exception.class)
    public void copyPlanCase(String sourcePlanId, String targetPlanId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(sourcePlanId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestPlanTestCaseMapper testCaseMapper = sqlSession.getMapper(TestPlanTestCaseMapper.class);
            testPlanTestCases.forEach(testCase -> {
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
                testCaseMapper.insert(testPlanTestCase);
            });
            sqlSession.flushStatements();

            TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
            testPlanApiCaseExample.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);
            TestPlanApiCaseMapper apiCaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
            testPlanApiCases.forEach(apiCase -> {
                TestPlanApiCase api = new TestPlanApiCase();
                api.setId(UUID.randomUUID().toString());
                api.setTestPlanId(targetPlanId);
                api.setApiCaseId(apiCase.getApiCaseId());
                api.setEnvironmentId(apiCase.getEnvironmentId());
                api.setCreateTime(System.currentTimeMillis());
                api.setUpdateTime(System.currentTimeMillis());
                api.setCreateUser(SessionUtils.getUserId());
                apiCaseMapper.insert(api);
            });
            sqlSession.flushStatements();

            TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
            testPlanApiScenarioExample.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanApiScenario> apiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(testPlanApiScenarioExample);
            TestPlanApiScenarioMapper apiScenarioMapper = sqlSession.getMapper(TestPlanApiScenarioMapper.class);
            apiScenarios.forEach(apiScenario -> {
                TestPlanApiScenario planScenario = new TestPlanApiScenario();
                planScenario.setId(UUID.randomUUID().toString());
                planScenario.setTestPlanId(targetPlanId);
                planScenario.setApiScenarioId(apiScenario.getApiScenarioId());
                planScenario.setEnvironment(apiScenario.getEnvironment());
                planScenario.setCreateTime(System.currentTimeMillis());
                planScenario.setUpdateTime(System.currentTimeMillis());
                planScenario.setCreateUser(SessionUtils.getUserId());
                apiScenarioMapper.insert(planScenario);
            });
            sqlSession.flushStatements();

            TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanLoadCase> loadCases = testPlanLoadCaseMapper.selectByExample(example);
            TestPlanLoadCaseMapper mapper = sqlSession.getMapper(TestPlanLoadCaseMapper.class);
            loadCases.forEach(loadCase -> {
                TestPlanLoadCase load = new TestPlanLoadCase();
                load.setId(UUID.randomUUID().toString());
                load.setTestPlanId(targetPlanId);
                load.setLoadCaseId(loadCase.getLoadCaseId());
                load.setCreateTime(System.currentTimeMillis());
                load.setUpdateTime(System.currentTimeMillis());
                load.setCreateUser(SessionUtils.getUserId());
                mapper.insert(load);
            });
            sqlSession.flushStatements();
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
                List<IssuesDao> issueList = issuesService.getIssuesByPlanoId(planId);
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
            if (checkReportConfig(config, "api", "failure")) {
                // 接口
                List<TestPlanFailureApiDTO> apiFailureCases = null;
                if (!CollectionUtils.isEmpty(apiAllCases)) {
                    apiFailureCases = apiAllCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getExecResult())
                                    && i.getExecResult().equals("error"))
                            .collect(Collectors.toList());
                } else {
                    apiFailureCases = testPlanApiCaseService.getFailureCases(planId);
                }
                if (saveResponse) {
                    buildApiResponse(apiFailureCases);
                }
                report.setApiFailureCases(apiFailureCases);

                // 场景
                List<TestPlanFailureScenarioDTO> scenarioFailureCases = null;
                if (!CollectionUtils.isEmpty(scenarioAllCases)) {
                    scenarioFailureCases = scenarioAllCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getLastResult())
                                    && i.getLastResult().equals("Fail"))
                            .collect(Collectors.toList());
                } else {
                    scenarioFailureCases = testPlanScenarioCaseService.getFailureCases(planId);
                }
                if (saveResponse) {
                    buildScenarioResponse(scenarioFailureCases);
                }
                report.setScenarioFailureCases(scenarioFailureCases);
            }
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
                    if  (existReport) {
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
                            LoadTestExportJmx jmxContent = performanceReportService.getJmxContent(reportId);
                            if (jmxContent != null) {
                                response.setJmxContent(JSONObject.toJSONString(jmxContent));
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

                            List<String> reportResource = metricQueryService.queryReportResource(reportId);
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

    public void buildApiReport(TestPlanSimpleReportDTO report, JSONObject config, TestPlanExecuteInfo executeInfo, String planId,boolean saveResponse) {
        if(MapUtils.isEmpty(executeInfo.getApiCaseExecInfo()) && MapUtils.isEmpty(executeInfo.getApiScenarioCaseExecInfo())){
            return;
        }
        if (checkReportConfig(config, "api")) {
            List<TestPlanFailureApiDTO> apiAllCases = null;
            List<TestPlanFailureScenarioDTO> scenarioAllCases = null;
            if (checkReportConfig(config, "api", "all")) {
                if(MapUtils.isNotEmpty(executeInfo.getApiCaseExecInfo())){
                    // 接口
                    apiAllCases = testPlanApiCaseService.getAllCases(executeInfo.getApiCaseExecInfo().keySet(),planId,null);
                    if (saveResponse) {
                        apiAllCases.forEach(item -> {
                            ApiDefinitionExecResult result = executeInfo.getApiCaseExecuteReportMap().get(item.getId());
                            if(result != null){
                                APIReportResult dbResult = apiDefinitionService.buildAPIReportResult(result);
                                if (dbResult != null && StringUtils.isNotBlank(dbResult.getContent())) {
                                    item.setResponse(dbResult.getContent());
                                }
                            }
                        });
                    }
                    report.setApiAllCases(apiAllCases);
                }
                if(MapUtils.isNotEmpty(executeInfo.getApiScenarioCaseExecInfo())){
                    //场景
                    scenarioAllCases = testPlanScenarioCaseService.getAllCases(executeInfo.getApiScenarioCaseExecInfo().keySet(),planId,null);
                    if (saveResponse) {
                        scenarioAllCases.forEach((item) -> {
                            APIScenarioReportResult result = executeInfo.getApiScenarioReportReportMap().get(item.getId());
                            if(result != null){
                                item.setResponse(result);
                            }
                        });
                    }
                    report.setScenarioAllCases(scenarioAllCases);
                }
            }
            if (checkReportConfig(config, "api", "failure")) {
                // 接口
                List<TestPlanFailureApiDTO> apiFailureCases = null;
                if (!CollectionUtils.isEmpty(apiAllCases)) {
                    apiFailureCases = apiAllCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getExecResult())
                                    && i.getExecResult().equals("error"))
                            .collect(Collectors.toList());
                }
                if (saveResponse) {
                    apiFailureCases.forEach(item -> {
                        APIReportResult dbResult = apiDefinitionService.getDbResult(item.getId());
                        if (dbResult != null && StringUtils.isNotBlank(dbResult.getContent())) {
                            item.setResponse(dbResult.getContent());
                        }
                    });
                }
                report.setApiFailureCases(apiFailureCases);

                // 场景
                List<TestPlanFailureScenarioDTO> scenarioFailureCases = null;
                if (!CollectionUtils.isEmpty(scenarioAllCases)) {
                    scenarioFailureCases = scenarioAllCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getLastResult())
                                    && i.getLastResult().equals("Fail"))
                            .collect(Collectors.toList());
                }
                if (saveResponse) {
                    scenarioFailureCases.forEach((item) -> {
                        item.setResponse(apiScenarioReportService.get(item.getReportId()));
                    });
                }
                report.setScenarioFailureCases(scenarioFailureCases);
            }
        }
    }

    public void buildLoadReport(TestPlanSimpleReportDTO report, JSONObject config, TestPlanExecuteInfo executeInfo, String planId, boolean saveResponse) {
        if(MapUtils.isEmpty(executeInfo.getLoadCaseExecInfo())){
            return;
        }
        if (checkReportConfig(config, "load")) {
            List<TestPlanLoadCaseDTO> allCases = null;
            if (checkReportConfig(config, "load", "all")) {
                allCases = testPlanLoadCaseService.getAllCases(executeInfo.getLoadCaseExecInfo().keySet(),planId,null);
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
                }
                report.setLoadFailureCases(failureCases);
            }
        }
    }

    public TestPlanSimpleReportDTO buildPlanReport(TestPlanExecuteInfo executeInfo,String planId, boolean saveResponse) {
        TestPlanWithBLOBs testPlan = testPlanMapper.selectByPrimaryKey(planId);
        if(testPlan != null){
            String reportConfig = testPlan.getReportConfig();
            JSONObject config = null;
            if (StringUtils.isNotBlank(reportConfig)) {
                config = JSONObject.parseObject(reportConfig);
            }
            TestPlanSimpleReportDTO report = getReport(planId);

            buildApiReport(report, config, executeInfo, planId, saveResponse);
            buildLoadReport(report, config, executeInfo, planId, saveResponse);
            return report;
        }else {
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
        TestPlanSimpleReportDTO report = getReport(planId);
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

    public TestPlanSimpleReportDTO getReport(String planId) {
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
        IssueTemplateDao template = issueTemplateService.getTemplate(testPlan.getProjectId());
        testPlanTestCaseService.calculatePlanReport(planId, report);
        issuesService.calculatePlanReport(planId, report);
        testPlanApiCaseService.calculatePlanReport(planId, report);
        testPlanScenarioCaseService.calculatePlanReport(planId, report);
        testPlanLoadCaseService.calculatePlanReport(planId, report);
        if(report.getExecuteCount() != 0 && report.getCaseCount() != null){
            report.setExecuteRate(report.getExecuteCount() * 0.1 * 10 / report.getCaseCount());
        }else {
            report.setExecuteRate(0.0);
        }
        if(report.getPassCount() != 0 && report.getCaseCount() != null){
            report.setPassRate(report.getPassCount() * 0.1 * 10/ report.getExecuteCount());
        }else {
            report.setPassRate(0.0);
        }

        report.setName(testPlan.getName());
        if (template == null || template.getPlatform().equals("metersphere")) {
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
        if (MapUtils.isNotEmpty(testplanRunRequest.getEnvMap())) {
            this.setPlanCaseEnv(testplanRunRequest.getTestPlanId(), testplanRunRequest.getEnvMap());
        }

        ApiRunConfigDTO api = new ApiRunConfigDTO();
        api.setMode(testplanRunRequest.getMode());
        api.setResourcePoolId(testplanRunRequest.getResourcePoolId());
        api.setOnSampleError(Boolean.parseBoolean(testplanRunRequest.getOnSampleError()));
        if (StringUtils.isBlank(testplanRunRequest.getReportType())) {
            api.setReportType("iddReport");
        } else {
            api.setReportType(testplanRunRequest.getReportType());
        }
        String apiRunConfig = JSONObject.toJSONString(api);
        return this.run(testplanRunRequest.getTestPlanId(), testplanRunRequest.getProjectId(),
                testplanRunRequest.getUserId(), testplanRunRequest.getTriggerMode(), apiRunConfig);
    }

    public void setPlanCaseEnv(String planId, Map<String, String> envMap) {
        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> planApiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getId).collect(Collectors.toList());
        testPlanApiCaseService.setApiCaseEnv(planApiCaseIds, envMap);

        TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(scenarioExample);
        List<String> planScenarioIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getId).collect(Collectors.toList());
        testPlanScenarioCaseService.setScenarioEnv(planScenarioIds, envMap);
    }

    public void editReportConfig(TestPlanDTO testPlanDTO) {
        TestPlanWithBLOBs testPlan = new TestPlanWithBLOBs();
        testPlan.setId(testPlanDTO.getId());
        testPlan.setReportConfig(testPlanDTO.getReportConfig());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }
}
