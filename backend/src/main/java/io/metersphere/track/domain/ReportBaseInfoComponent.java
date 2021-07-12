package io.metersphere.track.domain;

import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.dto.TestPlanDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ReportBaseInfoComponent extends ReportComponent {
    private Set<String> executorsSet = new HashSet<>();

    public ReportBaseInfoComponent(TestPlanDTO testPlan) {
        super(testPlan);
        componentId = "1";
    }

    @Override
    public void readRecord(TestPlanCaseDTO testCase) {
        executorsSet.add(testCase.getExecutor());
    }

    @Override
    public void afterBuild(TestCaseReportMetricDTO testCaseReportMetric) {
        testCaseReportMetric.setProjectName(testPlan.getProjectName());
        testCaseReportMetric.setPrincipal(testPlan.getPrincipal());
        testCaseReportMetric.setExecutors(new ArrayList<>(this.executorsSet));
        List<String> userIds = new ArrayList<>();
        userIds.add(testPlan.getPrincipal());
        userIds.addAll(testCaseReportMetric.getExecutors());
        Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
        testCaseReportMetric.setPrincipalName(userMap.get(testCaseReportMetric.getPrincipal()));
        List<String> names = new ArrayList<>();
        testCaseReportMetric.getExecutors().forEach(item -> {
            if (StringUtils.isNotBlank(item)) {
                names.add(userMap.get(item));
            }
        });
        testCaseReportMetric.setExecutorNames(names);
    }
}
