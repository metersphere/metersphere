package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.controller.request.testCaseReport.CreateReportRequest;
import io.metersphere.controller.request.testcase.QueryTestPlanRequest;
import io.metersphere.dto.TestCaseNodeDTO;
import io.metersphere.dto.TestCaseReportMetricDTO;
import io.metersphere.dto.TestCaseReportResultDTO;
import io.metersphere.dto.TestPlanDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseReportService {

    @Resource
    TestCaseReportMapper testCaseReportMapper;

    @Resource
    TestPlanMapper testPlanMapper;

    @Resource
    ExtTestPlanMapper extTestPlanMapper;

    @Resource
    TestCaseReportTemplateMapper testCaseReportTemplateMapper;

    @Resource
    TestCaseNodeService testCaseNodeService;

    @Resource
    TestCaseNodeMapper testCaseNodeMapper;

    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;

    public List<TestCaseReport> listTestCaseReport(TestCaseReport request) {
        TestCaseReportExample example = new TestCaseReportExample();
        if ( StringUtils.isNotBlank(request.getName()) ) {
            example.createCriteria().andNameEqualTo(request.getName());
        }
        return testCaseReportMapper.selectByExample(example);
    }

    public TestCaseReport getTestCaseReport(String id) {
        return testCaseReportMapper.selectByPrimaryKey(id);
    }

    public void addTestCaseReport(TestCaseReport testCaseReport) {
        testCaseReport.setId(UUID.randomUUID().toString());
        testCaseReportMapper.insert(testCaseReport);
    }

    public void editTestCaseReport(TestCaseReport TestCaseReport) {
        testCaseReportMapper.updateByPrimaryKeyWithBLOBs(TestCaseReport);
    }

    public int deleteTestCaseReport(String id) {
        return testCaseReportMapper.deleteByPrimaryKey(id);
    }

    public String addTestCaseReportByTemplateId(CreateReportRequest request) {
        TestCaseReportTemplate template = testCaseReportTemplateMapper.selectByPrimaryKey(request.getTemplateId());
        TestCaseReport report = new TestCaseReport();
        BeanUtils.copyBean(report, template);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
        report.setName(testPlan.getName());
        report.setId(UUID.randomUUID().toString());
        testCaseReportMapper.insert(report);
        testPlan.setReportId(report.getId());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
        return report.getId();
    }

    public TestCaseReport getMetric(String planId) {
        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        testCaseReportMetricDTO.setExecutors(extTestPlanTestCaseMapper.getExecutors(planId));
        testCaseReportMetricDTO.setExecuteResult(extTestPlanTestCaseMapper.getReportMetric(planId));

        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(planId);
        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);
        testCaseReportMetricDTO.setProjectName(testPlan.getProjectName());
        testCaseReportMetricDTO.setPrincipal(testPlan.getPrincipal());

        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andPlanIdEqualTo(planId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(example);

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(testPlan.getProjectId());
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);

        return null;
    }

    private TestCaseReport get(List<TestCaseNode> nodes) {

//        List<TestCaseNode> rootNode = new ArrayList<>();
//        Map<String, List<String>> nodeMap = new HashMap<>();
//        nodes.forEach(node -> {
//            Integer level = node.getLevel();
//            if (level == 1) {
//                rootNode.add(node);
//                ArrayList<Object> objects = new ArrayList<>();
//            }
//        });
        return null;
    }


}
