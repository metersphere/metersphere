package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.FucTestLog;
import io.metersphere.base.domain.FucTestReport;
import io.metersphere.base.domain.FucTestReportExample;
import io.metersphere.base.mapper.FucTestReportMapper;
import io.metersphere.base.mapper.ext.ExtFunctionalTestReportMapper;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.FunctionalReportDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalReportService {

    @Resource
    private FucTestReportMapper fucTestReportMapper;
    @Resource
    private ExtFunctionalTestReportMapper extFunctionalTestReportMapper;

    public List<FucTestReport> getRecentReportList(ReportRequest request) {
        FucTestReportExample example = new FucTestReportExample();
        example.setOrderByClause("update_time desc");
        return fucTestReportMapper.selectByExample(example);
    }

    public List<FunctionalReportDTO> getReportList(ReportRequest request) {
        return extFunctionalTestReportMapper.getReportList(request);
    }

    public void deleteReport(String reportId) {
        fucTestReportMapper.deleteByPrimaryKey(reportId);
    }

    public FunctionalReportDTO getReportTestAndProInfo(String reportId) {
        return extFunctionalTestReportMapper.getReportTestAndProInfo(reportId);
    }

}
