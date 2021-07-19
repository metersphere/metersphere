package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDataViewMapper;
import io.metersphere.base.mapper.ApiTestReportDetailMapper;
import io.metersphere.base.mapper.ApiTestReportMapper;
import io.metersphere.base.mapper.ext.ExtApiTestReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class APIReportService {

    @Resource
    private ApiTestReportMapper apiTestReportMapper;
    @Resource
    private ApiTestReportDetailMapper apiTestReportDetailMapper;
    @Resource
    private ExtApiTestReportMapper extApiTestReportMapper;
    @Resource
    private ApiDataViewMapper apiDataViewMapper;

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
        apiDataViewMapper.deleteByReportId(request.getId());

    }

    public void deleteByTestId(String testId) {
        ApiTestReportExample example = new ApiTestReportExample();
        example.createCriteria().andTestIdEqualTo(testId);
        apiTestReportMapper.deleteByExample(example);

        ApiTestReportDetailExample detailExample = new ApiTestReportDetailExample();
        detailExample.createCriteria().andTestIdEqualTo(testId);
        apiTestReportDetailMapper.deleteByExample(detailExample);
    }

    public void complete(TestResult result, ApiTestReport report) {
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
        if (!StringUtils.equals(report.getStatus(), APITestStatus.Debug.name())) {
            //新增每一条接口记录到api_data_view表中
            creatApiDataView(new String(detail.getContent(), StandardCharsets.UTF_8), report.getId());
            if (result.getError() > 0) {
                report.setStatus(APITestStatus.Error.name());
            } else {
                report.setStatus(APITestStatus.Success.name());
            }
        }
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        apiTestReportMapper.updateByPrimaryKeySelective(report);
    }

    private void creatApiDataView(String jsonString, String reportId) {
        List<ApiDataView> listApiDataView = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(jsonString, JSONObject.class);
        JSONArray jsonArray = jsonObject.getJSONArray("scenarios");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonInArray = jsonArray.getJSONObject(i);
                JSONArray jsonRequestResults = jsonInArray.getJSONArray("requestResults");
                for (int j = 0; j < jsonRequestResults.size(); j++) {
                    JSONObject jsonInResponseResult = jsonRequestResults.getJSONObject(j).getJSONObject("responseResult");
                    String responseTime = jsonInResponseResult.getString("responseTime");
                    String responseCode = jsonInResponseResult.getString("responseCode");
                    String startTime = jsonRequestResults.getJSONObject(j).getString("startTime");
                    String name = jsonRequestResults.getJSONObject(j).getString("name");
                    String url = jsonRequestResults.getJSONObject(j).getString("url");
                    if (StringUtils.isBlank(url)) {
                        //如果非http请求不入库
                        continue;
                    }
                    ApiDataView apiDataView = new ApiDataView();
                    apiDataView.setId(UUID.randomUUID().toString());
                    apiDataView.setReportId(reportId);
                    apiDataView.setApiName(name);
                    apiDataView.setUrl(StringUtils.substringBefore(url, "?"));
                    apiDataView.setResponseTime(responseTime);
                    apiDataView.setStartTime(sdf.format(new Date(Long.parseLong(startTime))));
                    apiDataView.setResponseCode(responseCode);
                    listApiDataView.add(apiDataView);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (listApiDataView.size() > 0) {
            apiDataViewMapper.insertListApiData(listApiDataView);
        }
    }

    public String create(ApiTest test, String triggerMode) {
        ApiTestReport running = getRunningReport(test.getId());
        if (running != null) {
            return running.getId();
        }
        ApiTestReport report = buildReport(test, triggerMode, APITestStatus.Running.name());
        apiTestReportMapper.insert(report);
        return report.getId();
    }

    public String createDebugReport(ApiTest test) {
        ApiTestReport report = buildReport(test, ReportTriggerMode.MANUAL.name(), APITestStatus.Debug.name());
        apiTestReportMapper.insert(report);
        return report.getId();
    }

    public ApiTestReport buildReport(ApiTest test, String triggerMode, String status) {
        ApiTestReport report = new ApiTestReport();
        report.setId(UUID.randomUUID().toString());
        report.setTestId(test.getId());
        report.setName(test.getName());
        report.setTriggerMode(triggerMode);
        report.setDescription(test.getDescription());
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(status);
        report.setUserId(test.getUserId());
        return report;
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


    public void deleteAPIReportBatch(DeleteAPIReportRequest reportRequest) {
        ApiTestReportDetailExample apiTestReportDetailExample = new ApiTestReportDetailExample();
        apiTestReportDetailExample.createCriteria().andReportIdIn(reportRequest.getIds());
        apiTestReportDetailMapper.deleteByExample(apiTestReportDetailExample);

        ApiTestReportExample apiTestReportExample = new ApiTestReportExample();
        apiTestReportExample.createCriteria().andIdIn(reportRequest.getIds());
        apiTestReportMapper.deleteByExample(apiTestReportExample);
    }

    public long countByProjectIdAndCreateInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiTestReportMapper.countByProjectIDAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countByProjectIdGroupByExecuteResult(String projectId) {
        return extApiTestReportMapper.countByProjectIdGroupByExecuteResult(projectId);
    }
}
