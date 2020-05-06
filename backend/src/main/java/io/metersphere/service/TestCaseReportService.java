package io.metersphere.service;

import io.metersphere.base.domain.TestCaseReport;
import io.metersphere.base.domain.TestCaseReportExample;
import io.metersphere.base.domain.TestCaseReportTemplate;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.mapper.TestCaseReportMapper;
import io.metersphere.base.mapper.TestCaseReportTemplateMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.controller.request.testCaseReport.CreateReportRequest;
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

    @Resource
    TestPlanMapper testPlanMapper;

    @Resource
    TestCaseReportTemplateMapper testCaseReportTemplateMapper;

    public List<TestCaseReport> listTestCaseReport(TestCaseReport request) {
        TestCaseReportExample example = new TestCaseReportExample();
        if ( StringUtils.isNotBlank(request.getName()) ) {
            example.createCriteria().andNameEqualTo(request.getName());
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

    public Long addTestCaseReportByTemplateId(CreateReportRequest request) {
        TestCaseReportTemplate template = testCaseReportTemplateMapper.selectByPrimaryKey(request.getTemplateId());
        TestCaseReport report = new TestCaseReport();
        BeanUtils.copyBean(report, template);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
        report.setName(testPlan.getName());
        report.setId(null);
        testCaseReportMapper.insert(report);
        testPlan.setReportId(report.getId());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
        return report.getId();
    }

}
