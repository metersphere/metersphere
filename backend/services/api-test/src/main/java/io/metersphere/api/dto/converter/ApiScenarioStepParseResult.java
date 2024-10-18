package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ApiScenarioStepParseResult {
    private List<ApiScenarioStepRequest> stepList = new ArrayList<>();
    private Map<String, Object> stepDetails = new HashMap<>();
}