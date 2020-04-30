package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportLogMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.LoadTestReportResultMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportKeys;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ReportDTO;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineFactory;
import io.metersphere.report.base.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportService {

    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private LoadTestReportResultMapper loadTestReportResultMapper;
    @Resource
    private LoadTestReportLogMapper loadTestReportLogMapper;
    @Resource
    private TestResourceService testResourceService;

    public List<ReportDTO> getRecentReportList(ReportRequest request) {
        return extLoadTestReportMapper.getReportList(request);
    }

    public List<ReportDTO> getReportList(ReportRequest request) {
        return extLoadTestReportMapper.getReportList(request);
    }

    public void deleteReport(String reportId) {
        if (StringUtils.isBlank(reportId)) {
            MSException.throwException("report id cannot be null");
        }

        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(loadTestReport.getTestId());

        LogUtil.info("Delete report started, report ID: %s" + reportId);

        final Engine engine = EngineFactory.createEngine(loadTest);
        if (engine == null) {
            MSException.throwException(String.format("Delete report fail. create engine fail，report ID：%s", reportId));
        }

        String reportStatus = loadTestReport.getStatus();
        boolean isRunning = StringUtils.equals(reportStatus, PerformanceTestStatus.Running.name());
        boolean isStarting = StringUtils.equals(reportStatus, PerformanceTestStatus.Starting.name());
        boolean isError = StringUtils.equals(reportStatus, PerformanceTestStatus.Error.name());
        if (isRunning || isStarting || isError) {
            LogUtil.info("Start stop engine, report status: %s" + reportStatus);
            stopEngine(loadTest, engine);
        }

        loadTestReportMapper.deleteByPrimaryKey(reportId);
    }

    private void stopEngine(LoadTestWithBLOBs loadTest, Engine engine) {
        engine.stop();
        loadTest.setStatus(PerformanceTestStatus.Saved.name());
        loadTestMapper.updateByPrimaryKeySelective(loadTest);
    }

    public ReportDTO getReportTestAndProInfo(String reportId) {
        return extLoadTestReportMapper.getReportTestAndProInfo(reportId);
    }

    private String getContent(String id, ReportKeys reportKey) {
        LoadTestReportResultExample example = new LoadTestReportResultExample();
        example.createCriteria().andReportIdEqualTo(id).andReportKeyEqualTo(reportKey.name());
        List<LoadTestReportResult> loadTestReportResults = loadTestReportResultMapper.selectByExampleWithBLOBs(example);
        if (loadTestReportResults.size() == 0) {
            MSException.throwException("get report result error.");
        }
        return loadTestReportResults.get(0).getReportValue();
    }

    public List<Statistics> getReport(String id) {
        checkReportStatus(id);
        String reportValue = getContent(id, ReportKeys.RequestStatistics);
        return JSON.parseArray(reportValue, Statistics.class);
    }

    public List<Errors> getReportErrors(String id) {
        checkReportStatus(id);
        String content = getContent(id, ReportKeys.Errors);
        return JSON.parseArray(content, Errors.class);
    }

    public List<ErrorsTop5> getReportErrorsTOP5(String id) {
        checkReportStatus(id);
        String content = getContent(id, ReportKeys.ErrorsTop5);
        return JSON.parseArray(content, ErrorsTop5.class);
    }

    public TestOverview getTestOverview(String id) {
        checkReportStatus(id);
        String content = getContent(id, ReportKeys.Overview);
        return JSON.parseObject(content, TestOverview.class);
    }

    public ReportTimeInfo getReportTimeInfo(String id) {
        checkReportStatus(id);
        String content = getContent(id, ReportKeys.TimeInfo);
        return JSON.parseObject(content, ReportTimeInfo.class);
    }

    public List<ChartsData> getLoadChartData(String id) {
        checkReportStatus(id);
        String content = getContent(id, ReportKeys.LoadChart);
        return JSON.parseArray(content, ChartsData.class);
    }

    public List<ChartsData> getResponseTimeChartData(String id) {
        checkReportStatus(id);
        String content = getContent(id, ReportKeys.ResponseTimeChart);
        return JSON.parseArray(content, ChartsData.class);
    }

    public void checkReportStatus(String reportId) {
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        String reportStatus = loadTestReport.getStatus();
        if (StringUtils.equals(PerformanceTestStatus.Running.name(), reportStatus)) {
            MSException.throwException("Reporting in progress...");
        } else if (StringUtils.equals(PerformanceTestStatus.Reporting.name(), reportStatus)) {
            MSException.throwException("Reporting in progress...");
        } else if (StringUtils.equals(PerformanceTestStatus.Error.name(), reportStatus)) {
            MSException.throwException("Report generation error!");
        }
    }

    public LoadTestReport getLoadTestReport(String id) {
        return extLoadTestReportMapper.selectByPrimaryKey(id);
    }

    public Map<String, String> log(String reportId) {
        Map<String, String> logMap = new HashMap<>();
        LoadTestReportLogExample example = new LoadTestReportLogExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<LoadTestReportLog> loadTestReportLogs = loadTestReportLogMapper.selectByExampleWithBLOBs(example);
        loadTestReportLogs.stream().map(log -> {
            Map<String, String> result = new HashMap<>();
            TestResource testResource = testResourceService.getTestResource(log.getResourceId());
            if (testResource == null) {
                result.put(log.getResourceId(), log.getContent());
                return result;
            }
            String configuration = testResource.getConfiguration();
            JSONObject object = JSON.parseObject(configuration);
            if (StringUtils.isNotBlank(object.getString("masterUrl"))) {
                result.put(object.getString("masterUrl"), log.getContent());
                return result;
            }
            if (StringUtils.isNotBlank(object.getString("ip"))) {
                result.put(object.getString("ip"), log.getContent());
                return result;

            }
            result.put(log.getResourceId(), log.getContent());
            return result;
        }).forEach(log -> logMap.putAll(log.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
        );

        return logMap;
    }
}
