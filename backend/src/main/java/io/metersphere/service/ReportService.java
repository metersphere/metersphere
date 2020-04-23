package io.metersphere.service;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportExample;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ReportDTO;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineFactory;
import io.metersphere.report.GenerateReport;
import io.metersphere.report.base.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportService {

    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;

    public List<LoadTestReport> getRecentReportList(ReportRequest request) {
        LoadTestReportExample example = new LoadTestReportExample();
        LoadTestReportExample.Criteria criteria = example.createCriteria();
        //

        example.setOrderByClause("update_time desc");
        return loadTestReportMapper.selectByExample(example);
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

    public List<Statistics> getReport(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        return GenerateReport.getRequestStatistics(content);
    }

    public List<Errors> getReportErrors(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<Errors> errors = GenerateReport.getErrorsList(content);
        return errors;
    }

    public List<ErrorsTop5> getReportErrorsTOP5(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<ErrorsTop5> errorsTop5 = GenerateReport.getErrorsTop5List(content);
        return errorsTop5;
    }

    public TestOverview getTestOverview(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        TestOverview testOverview = GenerateReport.getTestOverview(content);
        return testOverview;
    }

    public ReportTimeInfo getReportTimeInfo(String id) {
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        ReportTimeInfo reportTimeInfo = GenerateReport.getReportTimeInfo(content);
        return reportTimeInfo;
    }

    public List<ChartsData> getLoadChartData(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<ChartsData> chartsDataList = GenerateReport.getLoadChartData(content);
        return chartsDataList;
    }

    public List<ChartsData> getResponseTimeChartData(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<ChartsData> chartsDataList = GenerateReport.getResponseTimeChartData(content);
        return chartsDataList;
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
}
