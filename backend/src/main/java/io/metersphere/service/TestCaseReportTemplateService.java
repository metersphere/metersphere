package io.metersphere.service;

import io.metersphere.base.domain.TestCaseReportTemplate;
import io.metersphere.base.domain.TestCaseReportTemplateExample;
import io.metersphere.base.mapper.TestCaseReportMapper;
import io.metersphere.base.mapper.TestCaseReportTemplateMapper;
import io.metersphere.controller.request.testCaseReport.QueryTemplateRequest;
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

    public List<TestCaseReportTemplate> listTestCaseReportTemplate(QueryTemplateRequest request) {
        TestCaseReportTemplateExample example = new TestCaseReportTemplateExample();
        TestCaseReportTemplateExample.Criteria criteria1 = example.createCriteria();
        TestCaseReportTemplateExample.Criteria criteria2 = example.createCriteria();
        if ( StringUtils.isNotBlank(request.getName()) ) {
            criteria1.andNameLike("%" + request.getName() + "%");
            criteria2.andNameLike("%" + request.getName() + "%");
        }
        if ( StringUtils.isNotBlank(request.getWorkspaceId()) ) {
            criteria1.andWorkspaceIdEqualTo(request.getWorkspaceId());
        }
        if (request.getQueryDefault() != null) {
            criteria2.andWorkspaceIdIsNull();
            example.or(criteria2);
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
