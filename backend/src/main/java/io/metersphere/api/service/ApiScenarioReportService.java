package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.util.Cache;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportService {

    private static Cache cache = Cache.newHardMemoryCache(0, 3600 * 24);
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

    public String getApiScenarioId(String name, String projectID) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andNameEqualTo(name).andProjectIdEqualTo(projectID).andStatusNotEqualTo("Trash");
        List<ApiScenario> list = apiScenarioMapper.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0).getId();
        }
    }

    public ApiScenarioReport complete(TestResult result, String runMode) {
        Object obj = cache.get(result.getTestId());
        if (obj == null) {
            MSException.throwException(Translator.get("api_report_is_null"));
        }
        APIScenarioReportResult report = (APIScenarioReportResult) obj;
        if (CollectionUtils.isNotEmpty(result.getScenarios())) {
            try {
                report.setName(result.getScenarios().get(0).getName() + "-" + DateUtils.getTimeString(System.currentTimeMillis()));
            } catch (Exception e) {
                LogUtil.error(e.getMessage());
            }
        }
        // report detail
        ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
        detail.setReportId(result.getTestId());
        detail.setProjectId(report.getProjectId());
        report.setTestId(result.getTestId());
        detail.setContent(JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        // report
        report.setUpdateTime(System.currentTimeMillis());
        if (!StringUtils.equals(report.getStatus(), APITestStatus.Debug.name())) {
            if (result.getError() > 0) {
                report.setStatus(APITestStatus.Error.name());
            } else {
                report.setStatus(APITestStatus.Success.name());
            }
        }
        report.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
        this.save(report, runMode);
        if (!report.getTriggerMode().equals(ReportTriggerMode.SCHEDULE.name())) {
            cache.put(report.getId(), report);
        }
        return report;
    }

    /**
     * 获取零时执行报告
     *
     * @param testId
     */
    public APIScenarioReportResult getCacheResult(String testId) {
        Object res = cache.get(testId);
        if (res != null) {
            APIScenarioReportResult reportResult = (APIScenarioReportResult) res;
            if (!reportResult.getStatus().equals(APITestStatus.Running.name())) {
                cache.remove(testId);
            }
            return reportResult;
        }
        return null;
    }


    public void addResult(APIScenarioReportResult res) {
        cache.put(res.getId(), res);
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

    public ApiScenarioReport createReport(APIScenarioReportResult test) {
        checkNameExist(test);
        ApiScenarioReport report = new ApiScenarioReport();
        report.setId(UUID.randomUUID().toString());
        report.setProjectId(test.getProjectId());
        report.setName(test.getName());
        report.setTriggerMode(test.getTriggerMode());
        report.setDescription(test.getDescription());
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(test.getStatus());
        report.setUserId(test.getUserId());
        report.setExecuteType(test.getExecuteType());
        apiScenarioReportMapper.insert(report);
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


    public String save(APIScenarioReportResult test, String runModel) {
        ApiScenarioReport report = createReport(test);
        ApiScenarioReportDetail detail = new ApiScenarioReportDetail();
        TestResult result = JSON.parseObject(test.getContent(), TestResult.class);
        // 更新场景
        if (result != null) {
            if (StringUtils.equals(runModel, ApiRunMode.SCENARIO_PLAN.name())) {
                updatePlanCase(result, test, report);
            } else {
                updateScenario(result.getScenarios(), test.getProjectId(), report.getId());
            }
        }
        detail.setContent(test.getContent().getBytes(StandardCharsets.UTF_8));
        detail.setReportId(report.getId());
        detail.setProjectId(test.getProjectId());
        apiScenarioReportDetailMapper.insert(detail);
        return report.getId();
    }

    public void updatePlanCase(TestResult result, APIScenarioReportResult test, ApiScenarioReport report) {
        TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
        testPlanApiScenario.setId(test.getId());
        ScenarioResult scenarioResult = result.getScenarios().get(0);
        if (scenarioResult.getError() > 0) {
            testPlanApiScenario.setLastResult("Fail");
        } else {
            testPlanApiScenario.setLastResult("Success");
        }
        String passRate = new DecimalFormat("0%").format((float) scenarioResult.getSuccess() / (scenarioResult.getSuccess() + scenarioResult.getError()));
        testPlanApiScenario.setPassRate(passRate);
        testPlanApiScenario.setReportId(report.getId());
        testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
    }

    public void updateScenario(List<ScenarioResult> Scenarios, String projectId, String reportId) {
        Scenarios.forEach(item -> {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andNameEqualTo(item.getName()).andProjectIdEqualTo(projectId);
            List<ApiScenario> list = apiScenarioMapper.selectByExample(example);
            if (list.size() > 0) {
                ApiScenario scenario = list.get(0);
                if (item.getError() > 0) {
                    scenario.setLastResult("Fail");
                } else {
                    scenario.setLastResult("Success");
                }
                String passRate = new DecimalFormat("0%").format((float) item.getSuccess() / (item.getSuccess() + item.getError()));
                scenario.setPassRate(passRate);
                scenario.setReportId(reportId);
                apiScenarioMapper.updateByPrimaryKey(scenario);
            }
        });
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
