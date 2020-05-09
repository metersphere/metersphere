package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.controller.request.testCaseReport.CreateReportRequest;
import io.metersphere.controller.request.testcase.QueryTestPlanRequest;
import io.metersphere.controller.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

    @Resource
    ExtTestCaseMapper extTestCaseMapper;

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

    public TestCaseReportMetricDTO getMetric(String planId) {

        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(planId);
        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);

        Set<String> executors = new HashSet<>();
        Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap = new HashMap<>();

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(testPlan.getProjectId());
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);

        List<TestCaseNodeDTO> nodeTrees = testCaseNodeService.getNodeTrees(nodes);

        Map<String, Set<String>> childIdMap = new HashMap<>();
        nodeTrees.forEach(item -> {
            Set<String> childIds = new HashSet<>();
            getChildIds(item, childIds);
            childIdMap.put(item.getId(), childIds);
        });

        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        List<TestPlanCaseDTO> testPlanTestCases = extTestCaseMapper.getTestPlanTestCases(request);

        Map<String, TestCaseReportModuleResultDTO> moduleResultMap = new HashMap<>();

        for (TestPlanCaseDTO testCase: testPlanTestCases) {
            executors.add(testCase.getExecutor());
            getStatusResultMap(reportStatusResultMap, testCase);
            getModuleResultMap(childIdMap, moduleResultMap, testCase, nodeTrees);
        }


        nodeTrees.forEach(rootNode -> {
            TestCaseReportModuleResultDTO moduleResult = moduleResultMap.get(rootNode.getId());
            if (moduleResult != null) {
                moduleResult.setModuleName(rootNode.getName());
            }
        });

        for (TestCaseReportModuleResultDTO moduleResult : moduleResultMap.values()) {
            moduleResult.setPassRate(new BigDecimal(moduleResult.getPassCount()*1.0f/moduleResult.getCaseCount())
                            .setScale(2, BigDecimal.ROUND_HALF_UP)
                            .doubleValue() * 100);
            if (moduleResult.getCaseCount() <= 0) {
                moduleResultMap.remove(moduleResult.getModuleId());
            }
        }

        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        testCaseReportMetricDTO.setProjectName(testPlan.getProjectName());
        testCaseReportMetricDTO.setPrincipal(testPlan.getPrincipal());
        testCaseReportMetricDTO.setExecutors(new ArrayList<>(executors));
        testCaseReportMetricDTO.setExecuteResult(new ArrayList<>(reportStatusResultMap.values()));
        testCaseReportMetricDTO.setModuleExecuteResult(new ArrayList<>(moduleResultMap.values()));

        return testCaseReportMetricDTO;
    }

    private void getStatusResultMap(Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap, TestPlanCaseDTO testCase) {
        TestCaseReportStatusResultDTO statusResult = reportStatusResultMap.get(testCase.getStatus());
        if (statusResult == null) {
            statusResult = new TestCaseReportStatusResultDTO();
            statusResult.setStatus(testCase.getStatus());
            statusResult.setCount(0);
        }
        statusResult.setCount(statusResult.getCount() + 1);
        reportStatusResultMap.put(testCase.getStatus(), statusResult);
    }

    private void getModuleResultMap(Map<String, Set<String>> childIdMap, Map<String, TestCaseReportModuleResultDTO> moduleResultMap, TestPlanCaseDTO testCase, List<TestCaseNodeDTO> nodeTrees) {
        childIdMap.forEach((rootNodeId, childIds) -> {
            if (childIds.contains(testCase.getNodeId())) {
                TestCaseReportModuleResultDTO moduleResult = moduleResultMap.get(rootNodeId);
                if (moduleResult == null) {
                    moduleResult = new TestCaseReportModuleResultDTO();
                    moduleResult.setCaseCount(0);
                    moduleResult.setPassCount(0);
                    moduleResult.setModuleId(rootNodeId);
                }
                moduleResult.setCaseCount(moduleResult.getCaseCount() + 1);
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Pass.name())) {
                    moduleResult.setPassCount(moduleResult.getPassCount() + 1);
                }
                moduleResultMap.put(rootNodeId, moduleResult);
                return;
            }
        });
    }

    private void getChildIds(TestCaseNodeDTO rootNode, Set<String> childIds) {

        childIds.add(rootNode.getId());

        List<TestCaseNodeDTO> children = rootNode.getChildren();

        if(children != null) {
            Iterator<TestCaseNodeDTO> iterator = children.iterator();
            while(iterator.hasNext()){
                getChildIds(iterator.next(), childIds);
            }
        }
    }

}
