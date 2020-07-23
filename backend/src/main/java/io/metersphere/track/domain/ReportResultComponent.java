package io.metersphere.track.domain;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.base.domain.TestCaseNodeExample;
import io.metersphere.base.mapper.TestCaseNodeMapper;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.MathUtils;
import io.metersphere.track.dto.*;
import io.metersphere.track.service.TestCaseNodeService;
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
        TestCaseNodeMapper testCaseNodeMapper = (TestCaseNodeMapper) CommonBeanFactory.getBean("testCaseNodeMapper");
        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(testPlan.getProjectId());
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);
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
        childIdMap.forEach((rootNodeId, childIds) -> {
            if (childIds.contains(testCase.getNodeId())) {
                TestCaseReportModuleResultDTO moduleResult = moduleResultMap.get(rootNodeId);
                if (moduleResult == null) {
                    moduleResult = new TestCaseReportModuleResultDTO();
                    moduleResult.setCaseCount(0);
                    moduleResult.setPassCount(0);
                    moduleResult.setIssuesCount(0);
                    moduleResult.setModuleId(rootNodeId);
                }
                moduleResult.setCaseCount(moduleResult.getCaseCount() + 1);
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Pass.name())) {
                    moduleResult.setPassCount(moduleResult.getPassCount() + 1);
                }
                if (StringUtils.isNotBlank(testCase.getIssues())) {
                    if (JSON.parseObject(testCase.getIssues()).getBoolean("hasIssues")) {
                        moduleResult.setIssuesCount(moduleResult.getIssuesCount() + 1);
                    }
                    ;
                }
                moduleResultMap.put(rootNodeId, moduleResult);
                return;
            }
        });
    }
}
