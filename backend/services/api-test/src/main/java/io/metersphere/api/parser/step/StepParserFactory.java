package io.metersphere.api.parser.step;

import io.metersphere.api.constants.ApiScenarioStepType;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public abstract class StepParserFactory {

    private static Map<String, StepParser> stepParserMap = new HashMap<>();

    static {
        stepParserMap.put(ApiScenarioStepType.API.name(), new ApiStepParser());
        stepParserMap.put(ApiScenarioStepType.API_CASE.name(), new ApiCaseStepParser());
        stepParserMap.put(ApiScenarioStepType.API_SCENARIO.name(), new ApiScenarioStepParser());
    }

    public static StepParser getStepParser(String stepType) {
        return stepParserMap.get(stepType);
    }
}
