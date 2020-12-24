package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.jmeter.ScenarioResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportDetailMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    public ApiScenarioReport complete(TestResult result, String runMode) {
        // 更新场景
        if (result != null) {
            if (StringUtils.equals(runMode, ApiRunMode.SCENARIO_PLAN.name())) {
                return updatePlanCase(result);
            } else {
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
        report.setTriggerMode(test.getTriggerMode());
        report.setDescription(test.getDescription());
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(test.getStatus());
        report.setUserId(test.getUserId());
        report.setExecuteType(test.getExecuteType());
        apiScenarioReportMapper.updateByPrimaryKey(report);
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
        return testResult;
    }

    public ApiScenarioReport updatePlanCase(TestResult result) {
        TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(result.getTestId());
        ScenarioResult scenarioResult = result.getScenarios().get(0);
        if (scenarioResult.getError() > 0) {
            testPlanApiScenario.setLastResult("Fail");
        } else {
            testPlanApiScenario.setLastResult("Success");
        }
        String passRate = new DecimalFormat("0%").format((float) scenarioResult.getSuccess() / (scenarioResult.getSuccess() + scenarioResult.getError()));
        testPlanApiScenario.setPassRate(passRate);
        // 存储场景报告
        ApiScenarioReport report = editReport(scenarioResult);
        // 报告详情内容
        ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
        TestResult newResult = createTestResult(result);
        List<ScenarioResult> scenarioResults = new ArrayList();
        scenarioResult.setName(report.getScenarioName());
        scenarioResults.add(scenarioResult);
        newResult.setScenarios(scenarioResults);
        detail.setContent(JSON.toJSONString(newResult).getBytes(StandardCharsets.UTF_8));
        detail.setReportId(report.getId());
        detail.setProjectId(report.getProjectId());
        apiScenarioReportDetailMapper.insert(detail);

        testPlanApiScenario.setReportId(report.getId());
        testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
        return report;
    }

    public ApiScenarioReport updateScenario(TestResult result) {
        ApiScenarioReport lastReport = null;
        for (ScenarioResult item : result.getScenarios()) {
            // 更新报告状态
            ApiScenarioReport report = editReport(item);

            // 报告详情内容
            ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
            TestResult newResult = createTestResult(result);
            List<ScenarioResult> scenarioResults = new ArrayList();
            item.setName(report.getScenarioName());
            scenarioResults.add(item);
            newResult.setScenarios(scenarioResults);
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

    public void deleteAPIReportBatch(DeleteAPIReportRequest reportRequest) {
        ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
        detailExample.createCriteria().andReportIdIn(reportRequest.getIds());
        apiScenarioReportDetailMapper.deleteByExample(detailExample);

        ApiScenarioReportExample apiTestReportExample = new ApiScenarioReportExample();
        apiTestReportExample.createCriteria().andIdIn(reportRequest.getIds());
        apiScenarioReportMapper.deleteByExample(apiTestReportExample);
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
}
