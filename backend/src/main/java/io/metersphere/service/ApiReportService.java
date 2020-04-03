package io.metersphere.service;

import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.base.domain.ApiTestReportExample;
import io.metersphere.base.mapper.ApiTestReportMapper;
import io.metersphere.base.mapper.ext.ExtApiTestReportMapper;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ApiReportDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiReportService {

    @Resource
    private ApiTestReportMapper ApiTestReportMapper;
    @Resource
    private ExtApiTestReportMapper extApiTestReportMapper;

    public List<ApiTestReport> getRecentReportList(ReportRequest request) {
        ApiTestReportExample example = new ApiTestReportExample();
        example.setOrderByClause("update_time desc");
        return ApiTestReportMapper.selectByExample(example);
    }

    public List<ApiReportDTO> getReportList(ReportRequest request) {
        return extApiTestReportMapper.getReportList(request);
    }

    public void deleteReport(String reportId) {
        ApiTestReportMapper.deleteByPrimaryKey(reportId);
    }

    public ApiReportDTO getReportTestAndProInfo(String reportId) {
        return extApiTestReportMapper.getReportTestAndProInfo(reportId);
    }

}
