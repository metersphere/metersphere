package io.metersphere.service;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportExample;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ReportDTO;
import io.metersphere.report.JtlResolver;
import io.metersphere.report.base.ChartsData;
import io.metersphere.report.base.Errors;
import io.metersphere.report.base.ReportTimeInfo;
import io.metersphere.report.base.TestOverview;
import io.metersphere.report.dto.ErrorsTop5DTO;
import io.metersphere.report.dto.RequestStatisticsDTO;
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
        loadTestReportMapper.deleteByPrimaryKey(reportId);
    }

    public ReportDTO getReportTestAndProInfo(String reportId) {
        return extLoadTestReportMapper.getReportTestAndProInfo(reportId);
    }

    public RequestStatisticsDTO getReport(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        RequestStatisticsDTO requestStatistics = JtlResolver.getRequestStatistics(content);
        return requestStatistics;
    }

    public List<Errors> getReportErrors(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<Errors> errors = JtlResolver.getErrorsList(content);
        return errors;
    }

    public ErrorsTop5DTO getReportErrorsTOP5(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        ErrorsTop5DTO errors = JtlResolver.getErrorsTop5DTO(content);
        return errors;
    }

    public TestOverview getTestOverview(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        TestOverview testOverview = JtlResolver.getTestOverview(content);
        return testOverview;
    }

    public ReportTimeInfo getReportTimeInfo(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        ReportTimeInfo reportTimeInfo = JtlResolver.getReportTimeInfo(content);
        return reportTimeInfo;
    }

    public List<ChartsData> getLoadChartData(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<ChartsData> chartsDataList = JtlResolver.getLoadChartData(content);
        return chartsDataList;
    }

    public List<ChartsData> getResponseTimeChartData(String id) {
        checkReportStatus(id);
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<ChartsData> chartsDataList = JtlResolver.getResponseTimeChartData(content);
        return chartsDataList;
    }

    public void checkReportStatus(String reportId) {
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        String reportStatus = loadTestReport.getStatus();
        if (StringUtils.equals(PerformanceTestStatus.Running.name(), reportStatus)) {
            MSException.throwException("Reporting in progress...");
        } else if (StringUtils.equals(PerformanceTestStatus.Error.name(), reportStatus)) {
            MSException.throwException("Report generation error!");
        }
    }

    public LoadTestReport getLoadTestReport(String id) {
        return extLoadTestReportMapper.selectByPrimaryKey(id);
    }
}
