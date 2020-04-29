package io.metersphere.service;

import io.metersphere.base.domain.TestCaseReport;
import io.metersphere.base.domain.TestCaseReportExample;
import io.metersphere.base.mapper.TestCaseReportMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseReportService {

    @Resource
    TestCaseReportMapper testCaseReportMapper;

    public List<TestCaseReport> listTestCaseReport(TestCaseReport request) {
        TestCaseReportExample example = new TestCaseReportExample();
        if ( StringUtils.isNotBlank(request.getName()) ) {
            example.createCriteria().andNameEqualTo(request.getName());
        }
        if ( StringUtils.isNotBlank(request.getPlanId()) ) {
            example.createCriteria().andPlanIdEqualTo(request.getPlanId());
        }
        return testCaseReportMapper.selectByExample(example);
    }

    public TestCaseReport getTestCaseReport(Long id) {
        return testCaseReportMapper.selectByPrimaryKey(id);
    }

    public void addTestCaseReport(TestCaseReport TestCaseReport) {
        testCaseReportMapper.insert(TestCaseReport);
    }

    public void editTestCaseReport(TestCaseReport TestCaseReport) {
        testCaseReportMapper.updateByPrimaryKeyWithBLOBs(TestCaseReport);
    }

    public int deleteTestCaseReport(Long id) {
        return testCaseReportMapper.deleteByPrimaryKey(id);
    }
}
