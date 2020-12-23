package io.metersphere.track.Factory;

import io.metersphere.track.domain.*;
import io.metersphere.track.dto.TestPlanDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ReportComponentFactory {

    public static ReportComponent createComponent(String componentId, TestPlanDTO testPlan) {
        if (StringUtils.equals("1", componentId)) {
            return new ReportBaseInfoComponent(testPlan);
        } else if (StringUtils.equals("2", componentId)) {
            return new ReportResultComponent(testPlan);
        } else if (StringUtils.equals("3", componentId)) {
            return new ReportResultAdvancedChartComponent(testPlan);
//            return new ReportResultChartComponent(testPlan);
        } else if (StringUtils.equals("4", componentId)) {
//            return new ReportFailureResultComponent(testPlan);
            return new ReportFailureAdvanceResultComponent(testPlan);
        }
        return null;
    }

    public static List<ReportComponent> createComponents(List<String> componentIds, TestPlanDTO testPlan) {
        List<ReportComponent> components = new ArrayList<>();
        componentIds.forEach(id -> {
            ReportComponent component = createComponent(id, testPlan);
            if (component != null) {
                components.add(component);
            }
        });
        return components;
    }

}
