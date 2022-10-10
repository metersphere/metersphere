package io.metersphere.api.dto.plan;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class TestPlanApiReportInfoDTO {

    private Map<String, String> planApiCaseIdMap = new LinkedHashMap<>();
    private Map<String, String> planScenarioIdMap = new LinkedHashMap<>();
    private TestPlanReportRunInfoDTO runInfoDTO;
}
