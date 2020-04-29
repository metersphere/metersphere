package io.metersphere.service;

import io.metersphere.base.domain.TestCaseReportTemplate;
import io.metersphere.base.domain.TestCaseReportTemplateExample;
import io.metersphere.base.mapper.TestCaseReportMapper;
import io.metersphere.base.mapper.TestCaseReportTemplateMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseReportTemplateService {

    @Resource
    TestCaseReportTemplateMapper testCaseReportTemplateMapper;

    public List<TestCaseReportTemplate> listTestCaseReportTemplate(TestCaseReportTemplate request) {
        TestCaseReportTemplateExample example = new TestCaseReportTemplateExample();
        if ( StringUtils.isNotBlank(request.getName()) ) {
            example.createCriteria().andNameEqualTo(request.getName());
        }
        if ( StringUtils.isNotBlank(request.getWorkspaceId()) ) {
            example.createCriteria().andWorkspaceIdEqualTo(request.getWorkspaceId());
        }
        return testCaseReportTemplateMapper.selectByExample(example);
    }

    public TestCaseReportTemplate getTestCaseReportTemplate(Long id) {
        return testCaseReportTemplateMapper.selectByPrimaryKey(id);
    }

    public void addTestCaseReportTemplate(TestCaseReportTemplate testCaseReportTemplate) {
        testCaseReportTemplateMapper.insert(testCaseReportTemplate);
    }

    public void editTestCaseReportTemplate(TestCaseReportTemplate testCaseReportTemplate) {
        testCaseReportTemplateMapper.updateByPrimaryKeyWithBLOBs(testCaseReportTemplate);
    }

    public int deleteTestCaseReportTemplate(Long id) {
        return testCaseReportTemplateMapper.deleteByPrimaryKey(id);
    }

}
