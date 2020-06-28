package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestReportDetailMapper;
import io.metersphere.base.mapper.ApiTestReportMapper;
import io.metersphere.base.mapper.ext.ExtApiTestReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.i18n.Translator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class APIReportService {

    @Resource
    private ApiTestReportMapper apiTestReportMapper;
    @Resource
    private ApiTestReportDetailMapper apiTestReportDetailMapper;
    @Resource
    private ExtApiTestReportMapper extApiTestReportMapper;

    public List<APIReportResult> list(QueryAPIReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiTestReportMapper.list(request);
    }

    public List<APIReportResult> recentTest(QueryAPIReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiTestReportMapper.list(request);
    }

    public APIReportResult get(String reportId) {
        APIReportResult result = extApiTestReportMapper.get(reportId);
        ApiTestReportDetail detail = apiTestReportDetailMapper.selectByPrimaryKey(reportId);
        if (detail != null) {
            result.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
        }
        return result;
    }

    public List<APIReportResult> listByTestId(String testId) {
        return extApiTestReportMapper.listByTestId(testId);
    }

    public void delete(DeleteAPIReportRequest request) {
        apiTestReportDetailMapper.deleteByPrimaryKey(request.getId());
        apiTestReportMapper.deleteByPrimaryKey(request.getId());
    }

    public void deleteByTestId(String testId) {
        ApiTestReportExample example = new ApiTestReportExample();
        example.createCriteria().andTestIdEqualTo(testId);
        apiTestReportMapper.deleteByExample(example);

        ApiTestReportDetailExample detailExample = new ApiTestReportDetailExample();
        detailExample.createCriteria().andTestIdEqualTo(testId);
        apiTestReportDetailMapper.deleteByExample(detailExample);
    }

    public void complete(TestResult result) {
        ApiTestReport report = getRunningReport(result.getTestId());
        if (report == null) {
            MSException.throwException(Translator.get("api_report_is_null"));
        }
        // report detail
        ApiTestReportDetail detail = new ApiTestReportDetail();
        detail.setReportId(report.getId());
        detail.setTestId(report.getTestId());
        detail.setContent(JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        apiTestReportDetailMapper.insert(detail);

        // report
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(APITestStatus.Completed.name());
        apiTestReportMapper.updateByPrimaryKeySelective(report);
    }

    public String create(ApiTest test, String triggerMode) {
        ApiTestReport running = getRunningReport(test.getId());
        if (running != null) {
            return running.getId();
        }

        ApiTestReport report = new ApiTestReport();
        report.setId(UUID.randomUUID().toString());
        report.setTestId(test.getId());
        report.setName(test.getName());
        report.setTriggerMode(triggerMode);
        report.setDescription(test.getDescription());
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(APITestStatus.Running.name());
        report.setUserId(test.getUserId());
        apiTestReportMapper.insert(report);

        return report.getId();
    }

    public ApiTestReport getRunningReport(String testId) {
        ApiTestReportExample example = new ApiTestReportExample();
        example.createCriteria().andTestIdEqualTo(testId).andStatusEqualTo(APITestStatus.Running.name());
        List<ApiTestReport> apiTestReports = apiTestReportMapper.selectByExample(example);
        if (apiTestReports.size() > 0) {
            return apiTestReports.get(0);
        } else {
            return null;
        }
    }

    public List<DashboardTestDTO> dashboardTests(String workspaceId) {
        Instant oneYearAgo = Instant.now().plus(-365, ChronoUnit.DAYS);
        long startTimestamp = oneYearAgo.toEpochMilli();
        return extApiTestReportMapper.selectDashboardTests(workspaceId, startTimestamp);
    }


}
