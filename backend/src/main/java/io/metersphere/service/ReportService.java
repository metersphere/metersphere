package io.metersphere.service;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportExample;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ReportDTO;
import io.metersphere.report.JtlResolver;
import io.metersphere.report.base.Errors;
import io.metersphere.report.base.RequestStatistics;
import io.metersphere.report.base.RequestStatisticsDTO;
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
        LoadTestReport loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        RequestStatisticsDTO requestStatistics = JtlResolver.getRequestStatistics(content);
        return requestStatistics;
    }

    public List<Errors> getReportErrors(String id) {
        LoadTestReport loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        String content = loadTestReport.getContent();
        List<Errors> errors = JtlResolver.getErrorsList(content);
        return errors;
    }
}
