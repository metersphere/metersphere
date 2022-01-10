package io.metersphere.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class TestPlanExecuteReportDTO {
    private Map<String,String> testPlanApiCaseIdAndReportIdMap;
    private Map<String,String> testPlanScenarioIdAndReportIdMap;
    private Map<String,String> testPlanLoadCaseIdAndReportIdMap;
}
