package io.metersphere.track.domain;

import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ext.ExtTestCaseNodeMapper;
import io.metersphere.commons.constants.IssueRefType;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.MathUtils;
import io.metersphere.track.dto.*;
import io.metersphere.track.service.IssuesService;
import io.metersphere.track.service.TestCaseNodeService;
import io.metersphere.track.service.TestPlanProjectService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ReportResultComponent extends ReportComponent {

    private List<TestCaseNodeDTO> nodeTrees = new ArrayList<>();
    private Map<String, Set<String>> childIdMap = new HashMap<>();
    private Map<String, TestCaseReportModuleResultDTO> moduleResultMap = new HashMap<>();

    public ReportResultComponent(TestPlanDTO testPlan) {
        super(testPlan);
        componentId = "2";
        init();
    }

    public void init() {
        TestCaseNodeService testCaseNodeService = (TestCaseNodeService) CommonBeanFactory.getBean("testCaseNodeService");
        ExtTestCaseNodeMapper extTestCaseNodeMapper = (ExtTestCaseNodeMapper) CommonBeanFactory.getBean("extTestCaseNodeMapper");
        TestPlanProjectService testPlanProjectService = (TestPlanProjectService) CommonBeanFactory.getBean("testPlanProjectService");
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(testPlan.getId());
        if (CollectionUtils.isEmpty(projectIds)) {
            projectIds.add(testPlan.getProjectId());
        }
        List<TestCaseNodeDTO> nodes = extTestCaseNodeMapper.getNodeTreeByProjectIds(projectIds);
        nodeTrees = testCaseNodeService.getNodeTrees(nodes);
        nodeTrees.forEach(item -> {
            Set<String> childIds = new HashSet<>();
            getChildIds(item, childIds);
            childIdMap.put(item.getId(), childIds);
        });
    }

    @Override
    public void readRecord(TestPlanCaseDTO testCase) {
        getModuleResultMap(childIdMap, moduleResultMap, testCase, nodeTrees);
    }

    @Override
    public void afterBuild(TestCaseReportMetricDTO testCaseReportMetric) {

        nodeTrees.forEach(rootNode -> {
            TestCaseReportModuleResultDTO moduleResult = moduleResultMap.get(rootNode.getId());
            if (moduleResult != null) {
                TestCaseNodeService testCaseNodeService = (TestCaseNodeService) CommonBeanFactory.getBean("testCaseNodeService");
                Project project = testCaseNodeService.getProjectByNode(rootNode.getId());
                moduleResult.setProjectName(project.getName());
                moduleResult.setModuleName(rootNode.getName());
            }
        });

        for (TestCaseReportModuleResultDTO moduleResult : moduleResultMap.values()) {
            moduleResult.setPassRate(MathUtils.getPercentWithDecimal(moduleResult.getPassCount() * 1.0f / moduleResult.getCaseCount()));
            if (moduleResult.getCaseCount() <= 0) {
                moduleResultMap.remove(moduleResult.getModuleId());
            }
        }
        testCaseReportMetric.setModuleExecuteResult(new ArrayList<>(moduleResultMap.values()));
    }

    private void getChildIds(TestCaseNodeDTO rootNode, Set<String> childIds) {

        childIds.add(rootNode.getId());

        List<TestCaseNodeDTO> children = rootNode.getChildren();

        if (children != null) {
            Iterator<TestCaseNodeDTO> iterator = children.iterator();
            while (iterator.hasNext()) {
                getChildIds(iterator.next(), childIds);
            }
        }
    }

    private void getModuleResultMap(Map<String, Set<String>> childIdMap, Map<String, TestCaseReportModuleResultDTO> moduleResultMap, TestPlanCaseDTO testCase, List<TestCaseNodeDTO> nodeTrees) {
        IssuesService issuesService = (IssuesService) CommonBeanFactory.getBean("issuesService");
        childIdMap.forEach((rootNodeId, childIds) -> {

            if (childIds.contains(testCase.getNodeId())) {
                TestCaseReportModuleResultDTO moduleResult = moduleResultMap.get(rootNodeId);
                if (moduleResult == null) {
                    moduleResult = new TestCaseReportModuleResultDTO();
                    moduleResult.setCaseCount(0);
                    moduleResult.setPassCount(0);
                    moduleResult.setIssuesCount(0);
                    moduleResult.setFailureCount(0);
                    moduleResult.setBlockingCount(0);
                    moduleResult.setPrepareCount(0);
                    moduleResult.setSkipCount(0);
                    moduleResult.setUnderwayCount(0);
                    moduleResult.setModuleId(rootNodeId);
                }
                moduleResult.setCaseCount(moduleResult.getCaseCount() + 1);
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Pass.name())) {
                    moduleResult.setPassCount(moduleResult.getPassCount() + 1);
                }
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Prepare.name())) {
                    moduleResult.setPrepareCount(moduleResult.getPrepareCount() + 1);
                }
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Underway.name())) {
                    moduleResult.setUnderwayCount(moduleResult.getUnderwayCount() + 1);
                }
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Failure.name())) {
                    moduleResult.setFailureCount(moduleResult.getFailureCount() + 1);
                }
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Skip.name())) {
                    moduleResult.setSkipCount(moduleResult.getSkipCount() + 1);
                }
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Blocking.name())) {
                    moduleResult.setBlockingCount(moduleResult.getBlockingCount() + 1);
                }
                moduleResult.setIssuesCount(moduleResult.getIssuesCount() + issuesService.getIssues(testCase.getId(), IssueRefType.PLAN_FUNCTIONAL.name()).size());
                moduleResultMap.put(rootNodeId, moduleResult);
                return;
            }
        });

    }
}
