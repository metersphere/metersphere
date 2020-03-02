package io.metersphere.service;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportExample;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.controller.request.ReportRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportService {

    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    public List<LoadTestReport> getRecentReportList(ReportRequest request) {
        LoadTestReportExample example = new LoadTestReportExample();
        LoadTestReportExample.Criteria criteria = example.createCriteria();
        //

        example.setOrderByClause("update_time desc");
        return loadTestReportMapper.selectByExample(example);
    }
}
