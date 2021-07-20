package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.APIReportBatchRequest;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.api.jmeter.ReportCounter;
import io.metersphere.api.jmeter.ScenarioResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportDetailMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TestPlanApiExecuteStatus;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.track.service.TestPlanReportService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportService {
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;

    public ApiScenarioReport complete(TestResult result, String runMode) {
        // 更新场景
        if (result != null) {
            if (StringUtils.equals(runMode, ApiRunMode.SCENARIO_PLAN.name())) {
                return updatePlanCase(result, runMode);
            } else if (StringUtils.equalsAny(runMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                return updateSchedulePlanCase(result, runMode);
            } else {
                updateScenarioStatus(result.getTestId());
                return updateScenario(result);
            }
        }
        return null;
    }

    public APIScenarioReportResult get(String reportId) {
        APIScenarioReportResult reportResult = extApiScenarioReportMapper.get(reportId);
        ApiScenarioReportDetail detail = apiScenarioReportDetailMapper.selectByPrimaryKey(reportId);
        if (detail != null) {
            reportResult.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
        }
        return reportResult;
    }

    public List<APIScenarioReportResult> list(QueryAPIReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiScenarioReportMapper.list(request);
    }

    public List<String> idList(QueryAPIReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiScenarioReportMapper.idList(request);
    }

    private void checkNameExist(APIScenarioReportResult request) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andExecuteTypeEqualTo(ExecuteType.Saved.name()).andIdNotEqualTo(request.getId());
        if (apiScenarioReportMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    public APIScenarioReportResult createScenarioReport(String scenarioIds, String reportName, String status, String scenarioNames, String triggerMode, String projectId, String userID) {
        APIScenarioReportResult report = new APIScenarioReportResult();
        if (triggerMode.equals(ApiRunMode.SCENARIO.name()) || triggerMode.equals(ApiRunMode.DEFINITION.name())) {
            triggerMode = ReportTriggerMode.MANUAL.name();
        }
        report.setId(UUID.randomUUID().toString());
        report.setName(reportName);
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(status);
        if (StringUtils.isNotEmpty(userID)) {
            report.setUserId(userID);
        } else {
            report.setUserId(SessionUtils.getUserId());
        }
        report.setTriggerMode(triggerMode);
        report.setExecuteType(ExecuteType.Saved.name());
        report.setProjectId(projectId);
        report.setScenarioName(scenarioNames);
        report.setScenarioId(scenarioIds);
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiScenarioReportMapper.insert(report);
        return report;
    }

    public ApiScenarioReport editReport(ScenarioResult test, long startTime) {
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(test.getName());
        if (report != null) {
            report.setId(report.getId());
            report.setName(report.getScenarioName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
            report.setCreateTime(startTime);
            report.setUpdateTime(startTime);
            String status = test.getError() == 0 ? "Success" : "Error";
            report.setStatus(status);
            if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
                report.setTriggerMode(TriggerMode.MANUAL.name());
            }
            apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        }
        return report;
    }

    public ApiScenarioReport updateReport(APIScenarioReportResult test) {
        checkNameExist(test);
        ApiScenarioReport report = new ApiScenarioReport();
        report.setId(test.getId());
        report.setProjectId(test.getProjectId());
        report.setName(test.getName());
        report.setScenarioName(test.getScenarioName());
        report.setScenarioId(test.getScenarioId());
        report.setTriggerMode(test.getTriggerMode());
        report.setDescription(test.getDescription());
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(test.getStatus());
        report.setUserId(test.getUserId());
        report.setExecuteType(test.getExecuteType());
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
    }

    private TestResult createTestResult(TestResult result) {
        TestResult testResult = new TestResult();
        testResult.setTestId(result.getTestId());
        testResult.setTotal(result.getTotal());
        testResult.setError(result.getError());
        testResult.setPassAssertions(result.getPassAssertions());
        testResult.setSuccess(result.getSuccess());
        testResult.setTotalAssertions(result.getTotalAssertions());
        testResult.setConsole(result.getConsole());
        return testResult;
    }

    private TestResult createTestResult(String testId, ScenarioResult scenarioResult) {
        TestResult testResult = new TestResult();
        testResult.setTestId(testId);
        testResult.setTotal(scenarioResult.getTotal());
        testResult.setError(scenarioResult.getError());
        testResult.setPassAssertions(scenarioResult.getPassAssertions());
        testResult.setSuccess(scenarioResult.getSuccess());
        testResult.setTotalAssertions(scenarioResult.getTotalAssertions());
        return testResult;
    }

    public ApiScenarioReport updatePlanCase(TestResult result, String runMode) {
        List<ScenarioResult> scenarioResultList = result.getScenarios();
        ApiScenarioReport returnReport = null;
        StringBuilder scenarioIds = new StringBuilder();
        StringBuilder scenarioNames = new StringBuilder();
        TestResult fullResult = createTestResult(result);
        List<String> reportIds = new LinkedList<>();
        for (ScenarioResult scenarioResult : scenarioResultList) {
            long startTime = System.currentTimeMillis();
            if (CollectionUtils.isNotEmpty(scenarioResult.getRequestResults()) && scenarioResult.getRequestResults().get(0).getStartTime() > 0) {
                startTime = scenarioResult.getRequestResults().get(0).getStartTime();
            }
            ApiScenarioReport report = editReport(scenarioResult, startTime);
            if (!StringUtils.equals(ReportTriggerMode.API.name(), report.getTriggerMode())) {
                report.setTriggerMode(ReportTriggerMode.MANUAL.name());
                apiScenarioReportMapper.updateByPrimaryKeySelective(report);
            }

            // 报告详情内容
            ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
            TestResult newResult = createTestResult(result.getTestId(), scenarioResult);
            scenarioResult.setName(report.getScenarioName());
            newResult.addScenario(scenarioResult);

            detail.setContent(JSON.toJSONString(newResult).getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(report.getProjectId());
            apiScenarioReportDetailMapper.insert(detail);

            fullResult.addScenario(scenarioResult);
            scenarioIds.append(scenarioResult.getName()).append(",");
            scenarioNames.append(report.getName()).append(",");

            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
            if (testPlanApiScenario != null) {
                report.setScenarioId(testPlanApiScenario.getApiScenarioId());
                if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
                    report.setTriggerMode(TriggerMode.MANUAL.name());
                }
                apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                if (scenarioResult.getError() > 0) {
                    testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
                } else {
                    testPlanApiScenario.setLastResult(ScenarioStatus.Success.name());
                }
                String passRate = new DecimalFormat("0%").format((float) scenarioResult.getSuccess() / (scenarioResult.getSuccess() + scenarioResult.getError()));
                testPlanApiScenario.setPassRate(passRate);
                testPlanApiScenario.setReportId(report.getId());
                testPlanApiScenario.setUpdateTime(report.getCreateTime());
                report.setTestPlanScenarioId(testPlanApiScenario.getId());
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
            }
            returnReport = report;
            reportIds.add(report.getId());
        }
        // margeReport(result, scenarioIds, scenarioNames, runMode, projectId, userId, reportIds);
        return returnReport;
    }

    public ApiScenarioReport  updateSchedulePlanCase(TestResult result, String runMode) {
        ApiScenarioReport lastReport = null;
        List<ScenarioResult> scenarioResultList = result.getScenarios();

        List<String> testPlanReportIdList = new ArrayList<>();
        StringBuilder scenarioNames = new StringBuilder();

        List<String> reportIds = new ArrayList<>();
        List<String> scenarioIdList = new ArrayList<>();
        Map<String,String> scenarioAndErrorMap = new HashMap<>();
        for (ScenarioResult scenarioResult : scenarioResultList) {

            // 存储场景报告
            long startTime = System.currentTimeMillis();
            if (CollectionUtils.isNotEmpty(scenarioResult.getRequestResults())) {
                startTime = scenarioResult.getRequestResults().get(0).getStartTime();
            }

            ApiScenarioReport report = editReport(scenarioResult, startTime);

            TestResult newResult = createTestResult(result.getTestId(), scenarioResult);
            newResult.setConsole(result.getConsole());
            scenarioResult.setName(report.getScenarioName());
            newResult.addScenario(scenarioResult);
            /**
             * 测试计划的定时任务场景执行时，主键是提前生成的【测试报告ID】。也就是TestResult.id是【测试报告ID】。
             * report.getScenarioId中存放的是 TestPlanApiScenario.id:TestPlanReport.id 由于参数限制，只得将两个ID拼接起来
             * 拆分report.getScenarioId, 查出ScenarioId，将真正的场景ID赋值回去
             * 同时将testPlanReportID存入集合，逻辑走完后更新TestPlanReport
             */
            String[] idArr = report.getScenarioId().split(":");
            String planScenarioId = null;
            if (idArr.length > 1) {
                planScenarioId = idArr[0];
                String planReportID = idArr[1];
                if (!testPlanReportIdList.contains(planReportID)) {
                    testPlanReportIdList.add(planReportID);
                }
            } else {
                planScenarioId = report.getScenarioId();
            }
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(planScenarioId);
            report.setScenarioId(testPlanApiScenario.getApiScenarioId());
            report.setTestPlanScenarioId(planScenarioId);
            apiScenarioReportMapper.updateByPrimaryKeySelective(report);

            if (scenarioResult.getError() > 0) {
                scenarioAndErrorMap.put(testPlanApiScenario.getApiScenarioId(), TestPlanApiExecuteStatus.FAILD.name());
                testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
            } else {
                scenarioAndErrorMap.put(testPlanApiScenario.getApiScenarioId(), TestPlanApiExecuteStatus.SUCCESS.name());
                testPlanApiScenario.setLastResult(ScenarioStatus.Success.name());
            }
            String passRate = new DecimalFormat("0%").format((float) scenarioResult.getSuccess() / (scenarioResult.getSuccess() + scenarioResult.getError()));
            testPlanApiScenario.setPassRate(passRate);
            // 报告详情内容
            ApiScenarioReportDetail detail = new ApiScenarioReportDetail();

            detail.setContent(JSON.toJSONString(newResult).getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(report.getProjectId());
            if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
                report.setTriggerMode(TriggerMode.MANUAL.name());
            }
            apiScenarioReportDetailMapper.insert(detail);

            testPlanApiScenario.setReportId(report.getId());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
            scenarioIdList.add(testPlanApiScenario.getApiScenarioId());
            scenarioNames.append(report.getName()).append(",");

            lastReport = report;
            reportIds.add(report.getId());
        }
        TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
        for (String planId :testPlanReportIdList) {
            testPlanReportService.updateExecuteApis(planId,null,scenarioAndErrorMap,null);
        }
//        testPlanReportService.updateReport(testPlanReportIdList, runMode, lastReport.getTriggerMode(), scenarioIdList);

        return lastReport;
    }

    /**
     * 批量更新状态，防止重复刷新报告
     *
     * @param reportId
     */
    public void updateScenarioStatus(String reportId) {
        if (StringUtils.isNotEmpty(reportId)) {
            List<String> list = new LinkedList<>();
            try {
                list = JSON.parseObject(reportId, List.class);
            } catch (Exception e) {
                list.add(reportId);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                ApiScenarioReportExample scenarioReportExample = new ApiScenarioReportExample();
                scenarioReportExample.createCriteria().andIdIn(list);
                List<ApiScenarioReport> reportList = apiScenarioReportMapper.selectByExample(scenarioReportExample);
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                ApiScenarioReportMapper scenarioReportMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
                if (CollectionUtils.isNotEmpty(reportList)) {
                    reportList.forEach(report -> {
                        report.setUpdateTime(System.currentTimeMillis());
                        String status = "Error";
                        report.setStatus(status);
                        scenarioReportMapper.updateByPrimaryKeySelective(report);
                        // 把上一条调试的数据内容清空
                        ApiScenarioReport prevResult = extApiScenarioReportMapper.selectPreviousReportByScenarioId(report.getScenarioId(), reportId);
                        if (prevResult != null) {
                            ApiScenarioReportDetailExample example = new ApiScenarioReportDetailExample();
                            example.createCriteria().andReportIdEqualTo(prevResult.getId());
                            apiScenarioReportDetailMapper.deleteByExample(example);
                        }
                    });
                    sqlSession.flushStatements();
                }
            }
        }
    }

    public void margeReport(String reportId, List<String> reportIds) {
        // 合并生成一份报告
        if (CollectionUtils.isNotEmpty(reportIds)) {
            TestResult testResult = new TestResult();
            testResult.setTestId(UUID.randomUUID().toString());
            ApiScenarioReportDetailExample example = new ApiScenarioReportDetailExample();
            example.createCriteria().andReportIdIn(reportIds);
            List<ApiScenarioReportDetail> details = apiScenarioReportDetailMapper.selectByExampleWithBLOBs(example);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            for (ApiScenarioReportDetail detail : details) {
                try {
                    String content = new String(detail.getContent(), StandardCharsets.UTF_8);
                    TestResult scenarioResult = mapper.readValue(content, new TypeReference<TestResult>() {
                    });
                    testResult.getScenarios().addAll(scenarioResult.getScenarios());
                    testResult.setTotal(testResult.getTotal() + scenarioResult.getTotal());
                    testResult.setError(testResult.getError() + scenarioResult.getError());
                    testResult.setPassAssertions(testResult.getPassAssertions() + scenarioResult.getPassAssertions());
                    testResult.setSuccess(testResult.getSuccess() + scenarioResult.getSuccess());
                    testResult.setTotalAssertions(scenarioResult.getTotalAssertions() + testResult.getTotalAssertions());
                    testResult.setScenarioTotal(testResult.getScenarioTotal() + scenarioResult.getScenarioTotal());
                    testResult.setScenarioSuccess(testResult.getScenarioSuccess() + scenarioResult.getScenarioSuccess());
                    testResult.setScenarioError(testResult.getScenarioError() + scenarioResult.getScenarioError());
                    testResult.setConsole(scenarioResult.getConsole());
                    testResult.setScenarioStepError(scenarioResult.getScenarioStepError() + testResult.getScenarioStepError());
                    testResult.setScenarioStepSuccess(scenarioResult.getScenarioStepSuccess() + testResult.getScenarioStepSuccess());
                    testResult.setScenarioStepTotal(scenarioResult.getScenarioStepTotal() + testResult.getScenarioStepTotal());
                } catch (Exception e) {
                    LogUtil.error(e.getMessage());
                }
            }

            ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
            if (report != null) {
                report.setExecuteType(ExecuteType.Saved.name());
                report.setStatus(testResult.getError() > 0 ? "Error" : "Success");
                if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
                    report.setTriggerMode(TriggerMode.MANUAL.name());
                }
                apiScenarioReportMapper.updateByPrimaryKey(report);

                ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
                detail.setContent(JSON.toJSONString(testResult).getBytes(StandardCharsets.UTF_8));
                detail.setReportId(report.getId());
                detail.setProjectId(report.getProjectId());
                apiScenarioReportDetailMapper.insert(detail);
            }
            // 更新场景状态
            if (StringUtils.isNotEmpty(report.getScenarioId())) {
                List<String> strings = JSON.parseObject(report.getScenarioId(), List.class);
                ApiScenarioExample scenarioExample = new ApiScenarioExample();
                scenarioExample.createCriteria().andIdIn(strings);

                List<ApiScenario> scenarios = apiScenarioMapper.selectByExample(scenarioExample);
                if (CollectionUtils.isNotEmpty(scenarios)) {
                    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                    ApiScenarioMapper scenarioReportMapper = sqlSession.getMapper(ApiScenarioMapper.class);
                    scenarios.forEach(scenario -> {
                        if (testResult.getError() > 0) {
                            scenario.setLastResult("Fail");
                        } else {
                            scenario.setLastResult("Success");
                        }
                        String passRate = new DecimalFormat("0%").format((float) testResult.getSuccess() / (testResult.getSuccess() + testResult.getError()));
                        scenario.setPassRate(passRate);
                        scenario.setReportId(report.getId());
                        scenarioReportMapper.updateByPrimaryKey(scenario);
                    });
                    sqlSession.flushStatements();
                }
            }
            // 清理其他报告保留一份合并后的报告
            this.deleteByIds(reportIds);

        }
    }

    private void counter(TestResult result) {
        if (CollectionUtils.isEmpty(result.getScenarios())) {
            if (StringUtils.isNotEmpty(result.getTestId())) {
                List<String> list = new LinkedList<>();
                try {
                    list = JSON.parseObject(result.getTestId(), List.class);
                } catch (Exception e) {
                    list.add(result.getTestId());
                }
                ApiScenarioReportExample scenarioReportExample = new ApiScenarioReportExample();
                scenarioReportExample.createCriteria().andIdIn(list);
                List<ApiScenarioReport> reportList = apiScenarioReportMapper.selectByExample(scenarioReportExample);
                for (ApiScenarioReport report : reportList) {
                    if (report.getExecuteType().equals(ExecuteType.Marge.name())) {
                        Object obj = MessageCache.cache.get(report.getScenarioId());
                        if (obj != null) {
                            ReportCounter counter = (ReportCounter) obj;
                            counter.setNumber(counter.getNumber() + 1);
                            MessageCache.cache.put(report.getScenarioId(), counter);
                        }
                    }
                }
            }
        }
    }

    public ApiScenarioReport updateScenario(TestResult result) {
        // 针对未正常返回结果的报告计数
        counter(result);

        ApiScenarioReport lastReport = null;
        for (ScenarioResult item : result.getScenarios()) {
            // 更新报告状态
            long startTime = System.currentTimeMillis();
            if (CollectionUtils.isNotEmpty(item.getRequestResults())) {
                startTime = item.getRequestResults().get(0).getStartTime();
            }
            ApiScenarioReport report = editReport(item, startTime);
            if (report != null) {
                // 合并并行报告
                TestResult newResult = createTestResult(result.getTestId(), item);
                newResult.setConsole(result.getConsole());
                item.setName(report.getScenarioName());
                newResult.addScenario(item);
                // 报告详情内容
                ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
                detail.setContent(JSON.toJSONString(newResult).getBytes(StandardCharsets.UTF_8));
                detail.setReportId(report.getId());
                detail.setProjectId(report.getProjectId());
                apiScenarioReportDetailMapper.insert(detail);
                // 更新场景状态
                ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
                if (scenario != null) {
                    if (item.getError() > 0) {
                        scenario.setLastResult("Fail");
                    } else {
                        scenario.setLastResult("Success");
                    }
                    String passRate = new DecimalFormat("0%").format((float) item.getSuccess() / (item.getSuccess() + item.getError()));
                    scenario.setPassRate(passRate);
                    scenario.setReportId(report.getId());
                    apiScenarioMapper.updateByPrimaryKey(scenario);
                }
                lastReport = report;
            }
            if (report.getExecuteType().equals(ExecuteType.Marge.name())) {
                Object obj = MessageCache.cache.get(report.getScenarioId());
                if (obj != null) {
                    ReportCounter counter = (ReportCounter) obj;
                    counter.setNumber(counter.getNumber() + 1);
                    MessageCache.cache.put(report.getScenarioId(), counter);
                }
            }
        }

        return lastReport;
    }

    public String update(APIScenarioReportResult test) {
        ApiScenarioReport report = updateReport(test);
        ApiScenarioReportDetail detail = apiScenarioReportDetailMapper.selectByPrimaryKey(test.getId());
        if (detail == null) {
            detail = new ApiScenarioReportDetail();
            detail.setContent(test.getContent().getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(test.getProjectId());
            apiScenarioReportDetailMapper.insert(detail);
        } else {
            detail.setContent(test.getContent().getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(test.getProjectId());
            apiScenarioReportDetailMapper.updateByPrimaryKey(detail);
        }
        return report.getId();
    }

    public static List<String> getReportIds(String content) {
        try {
            return JSON.parseObject(content, List.class);
        } catch (Exception e) {
            return null;
        }
    }


    public void delete(DeleteAPIReportRequest request) {
        apiScenarioReportDetailMapper.deleteByPrimaryKey(request.getId());
        // 补充逻辑，如果是集成报告则把零时报告全部删除
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getId());
        if (report != null && StringUtils.isNotEmpty(report.getScenarioId())) {
            List<String> list = getReportIds(report.getScenarioId());
            if (CollectionUtils.isNotEmpty(list)) {
                APIReportBatchRequest reportRequest = new APIReportBatchRequest();
                reportRequest.setIds(list);
                this.deleteAPIReportBatch(reportRequest);
            }
        }
        apiScenarioReportMapper.deleteByPrimaryKey(request.getId());
    }

    public void delete(String id) {
        apiScenarioReportDetailMapper.deleteByPrimaryKey(id);
        apiScenarioReportMapper.deleteByPrimaryKey(id);
    }

    public void deleteByIds(List<String> ids) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andIdIn(ids);
        ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
        detailExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportDetailMapper.deleteByExample(detailExample);
        apiScenarioReportMapper.deleteByExample(example);
    }

    public void deleteAPIReportBatch(APIReportBatchRequest reportRequest) {
        List<String> ids = reportRequest.getIds();
        if (reportRequest.isSelectAllDate()) {
            ids = this.idList(reportRequest);
            if (reportRequest.getUnSelectIds() != null) {
                ids.removeAll(reportRequest.getUnSelectIds());
            }
        }
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andIdIn(reportRequest.getIds());
        List<ApiScenarioReport> reportList = apiScenarioReportMapper.selectByExample(example);
        // 取出可能是集成报告的ID 放入删除
        reportList.forEach(item -> {
            List<String> reportIds = getReportIds(item.getScenarioId());
            if (CollectionUtils.isNotEmpty(reportIds)) {
                reportRequest.getIds().addAll(reportIds);
            }
        });
        List<String> myList = reportRequest.getIds().stream().distinct().collect(Collectors.toList());
        reportRequest.setIds(myList);
        //为预防数量太多，调用删除方法时引起SQL过长的Bug，此处采取分批执行的方式。
        //每次处理的数据数量
        int handleCount = 7000;
        //每次处理的集合
        List<String> handleIdList = new ArrayList<>(handleCount);
        while (ids.size() > handleCount) {
            handleIdList = new ArrayList<>(handleCount);
            List<String> otherIdList = new ArrayList<>();
            for (int index = 0; index < ids.size(); index++) {
                if (index < handleCount) {
                    handleIdList.add(ids.get(index));
                } else {
                    otherIdList.add(ids.get(index));
                }
            }
            //处理本次的数据
            ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
            detailExample.createCriteria().andReportIdIn(handleIdList);
            apiScenarioReportDetailMapper.deleteByExample(detailExample);
            ApiScenarioReportExample apiTestReportExample = new ApiScenarioReportExample();
            apiTestReportExample.createCriteria().andIdIn(handleIdList);
            apiScenarioReportMapper.deleteByExample(apiTestReportExample);
            //转存剩余的数据
            ids = otherIdList;
        }

        //处理最后剩余的数据
        if (!ids.isEmpty()) {
            ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
            detailExample.createCriteria().andReportIdIn(ids);
            apiScenarioReportDetailMapper.deleteByExample(detailExample);
            ApiScenarioReportExample apiTestReportExample = new ApiScenarioReportExample();
            apiTestReportExample.createCriteria().andIdIn(ids);
            apiScenarioReportMapper.deleteByExample(apiTestReportExample);
        }
    }

    public long countByProjectID(String projectId) {
        return extApiScenarioReportMapper.countByProjectID(projectId);
    }

    public long countByProjectIdAndCreateAndByScheduleInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioReportMapper.countByProjectIdAndCreateAndByScheduleInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public long countByProjectIdAndCreateInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioReportMapper.countByProjectIdAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countByProjectIdGroupByExecuteResult(String projectId) {
        return extApiScenarioReportMapper.countByProjectIdGroupByExecuteResult(projectId);
    }

    public List<ApiScenarioReport> selectLastReportByIds(List<String> ids) {
        if (!ids.isEmpty()) {
            return extApiScenarioReportMapper.selectLastReportByIds(ids);
        } else {
            return new ArrayList<>(0);
        }
    }

    public String getLogDetails(String id) {
        ApiScenarioReport bloBs = apiScenarioReportMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ModuleReference.moduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ids)) {
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(ids);
            List<ApiScenarioReport> reportList = apiScenarioReportMapper.selectByExample(example);
            List<String> names = reportList.stream().map(ApiScenarioReport::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), reportList.get(0).getProjectId(), String.join(",", names), reportList.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }
}
