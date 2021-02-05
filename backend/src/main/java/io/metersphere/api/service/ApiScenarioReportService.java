package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.jmeter.ScenarioResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportDetailMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.i18n.Translator;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
                return updatePlanCase(result);
            } else if (StringUtils.equals(runMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name())) {
                return updateSchedulePlanCase(result);
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

    private void checkNameExist(APIScenarioReportResult request) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andExecuteTypeEqualTo(ExecuteType.Saved.name()).andIdNotEqualTo(request.getId());
        if (apiScenarioReportMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    public ApiScenarioReport editReport(ScenarioResult test) {
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(test.getName());
        report.setId(report.getId());
        report.setName(report.getScenarioName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        report.setUpdateTime(System.currentTimeMillis());
        String status = test.getError() == 0 ? "Success" : "Error";
        report.setStatus(status);
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
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
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
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

    public ApiScenarioReport updatePlanCase(TestResult result) {
//        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(result.getTestId());
        List<ScenarioResult> scenarioResultList = result.getScenarios();
        ApiScenarioReport returnReport = null;
        for (ScenarioResult scenarioResult :
                scenarioResultList) {
            ApiScenarioReport report = editReport(scenarioResult);
            // 报告详情内容
            ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
            TestResult newResult = createTestResult(result.getTestId(), scenarioResult);
            scenarioResult.setName(report.getScenarioName());
            newResult.addScenario(scenarioResult);

            detail.setContent(JSON.toJSONString(newResult).getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(report.getProjectId());
            apiScenarioReportDetailMapper.insert(detail);

            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
            if(testPlanApiScenario!=null){
                report.setScenarioId(testPlanApiScenario.getApiScenarioId());
                apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                if (scenarioResult.getError() > 0) {
                    testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
                } else {
                    testPlanApiScenario.setLastResult(ScenarioStatus.Success.name());
                }
                String passRate = new DecimalFormat("0%").format((float) scenarioResult.getSuccess() / (scenarioResult.getSuccess() + scenarioResult.getError()));
                testPlanApiScenario.setPassRate(passRate);
                testPlanApiScenario.setReportId(report.getId());
                testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
            }
            returnReport = report;
        }

        return returnReport;
    }

    public ApiScenarioReport updateSchedulePlanCase(TestResult result) {
        ApiScenarioReport lastReport = null;
        List<ScenarioResult> scenarioResultList = result.getScenarios();

        List<String> testPlanReportIdList = new ArrayList<>();
        for (ScenarioResult scenarioResult : scenarioResultList) {
            // 存储场景报告
            ApiScenarioReport report = editReport(scenarioResult);

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
            apiScenarioReportMapper.updateByPrimaryKeySelective(report);
            if (scenarioResult.getError() > 0) {
                testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
            } else {
                testPlanApiScenario.setLastResult(ScenarioStatus.Success.name());
            }
            String passRate = new DecimalFormat("0%").format((float) scenarioResult.getSuccess() / (scenarioResult.getSuccess() + scenarioResult.getError()));
            testPlanApiScenario.setPassRate(passRate);
            // 报告详情内容
            ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
            TestResult newResult = createTestResult(result.getTestId(), scenarioResult);
            List<ScenarioResult> scenarioResults = new ArrayList();
            scenarioResult.setName(report.getScenarioName());
            scenarioResults.add(scenarioResult);
            newResult.setScenarios(scenarioResults);
            detail.setContent(JSON.toJSONString(newResult).getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(report.getProjectId());
            apiScenarioReportDetailMapper.insert(detail);

            testPlanApiScenario.setReportId(report.getId());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);

            lastReport = report;
        }

        TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
        testPlanReportService.updateReport(testPlanReportIdList, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ReportTriggerMode.SCHEDULE.name());

        return lastReport;
    }

    /**
     * 批量更新状态，防止重复刷新报告
     *
     * @param reportIds
     */
    private void updateScenarioStatus(String reportIds) {

        if (StringUtils.isNotEmpty(reportIds)) {
            List<String> list = new ArrayList<>();
            try {
                list = JSON.parseArray(reportIds, String.class);
            } catch (Exception e) {
                list.add(reportIds);
            }
            ApiScenarioReportExample scenarioReportExample = new ApiScenarioReportExample();
            scenarioReportExample.createCriteria().andIdIn(list);
            List<ApiScenarioReport> reportList = apiScenarioReportMapper.selectByExample(scenarioReportExample);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiScenarioReportMapper scenarioReportMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
            if (CollectionUtils.isNotEmpty(reportList)) {
                reportList.forEach(report -> {
                    report.setUpdateTime(System.currentTimeMillis());
                    String status = "Success";
                    report.setStatus(status);
                    scenarioReportMapper.updateByPrimaryKeySelective(report);
                });
                sqlSession.flushStatements();
            }
        }
    }

    public ApiScenarioReport updateScenario(TestResult result) {
        ApiScenarioReport lastReport = null;
        for (ScenarioResult item : result.getScenarios()) {
            // 更新报告状态
            ApiScenarioReport report = editReport(item);
            // 报告详情内容
            ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
            TestResult newResult = createTestResult(result.getTestId(), item);
            item.setName(report.getScenarioName());
            newResult.addScenario(item);
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

    public void delete(DeleteAPIReportRequest request) {
        apiScenarioReportDetailMapper.deleteByPrimaryKey(request.getId());
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

    public void deleteAPIReportBatch(DeleteAPIReportRequest reportRequest) {
        List<String> ids = reportRequest.getIds();
        if (reportRequest.isSelectAllDate()) {
            QueryAPIReportRequest selectRequest = new QueryAPIReportRequest();
            selectRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
            selectRequest.setName(reportRequest.getName());
            selectRequest.setProjectId(reportRequest.getProjectId());
            List<APIScenarioReportResult> list = extApiScenarioReportMapper.list(selectRequest);
            List<String> allIds = list.stream().map(APIScenarioReportResult::getId).collect(Collectors.toList());
            ids = allIds.stream().filter(id -> !reportRequest.getUnSelectIds().contains(id)).collect(Collectors.toList());
        }

        //为预防数量太多，调用删除方法时引起SQL过长的Bug，此处采取分批执行的方式。
        //每次处理的数据数量
        int handleCount = 7000;
        //每次处理的集合
        List<String> handleIdList = new ArrayList<>(handleCount);
        while (ids.size() > handleCount){
            handleIdList = new ArrayList<>(handleCount);
            List<String> otherIdList = new ArrayList<>();
            for (int index = 0;index < ids.size();index++){
                if(index<handleCount){
                    handleIdList.add(ids.get(index));
                }else{
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
        if(!ids.isEmpty()){
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
        if(!ids.isEmpty()){
            return extApiScenarioReportMapper.selectLastReportByIds(ids);
        }else {
            return new ArrayList<>(0);
        }
    }
}
