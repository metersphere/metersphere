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
    private static StepParser defaultStepParser = new DefaultStepParser();

    static {
        stepParserMap.put(ApiScenarioStepType.API.name(), new ApiStepParser());
        stepParserMap.put(ApiScenarioStepType.API_CASE.name(), new ApiCaseStepParser());
        stepParserMap.put(ApiScenarioStepType.API_SCENARIO.name(), new ApiScenarioStepParser());
        stepParserMap.put(ApiScenarioStepType.CONSTANT_TIMER.name(), new ConstantTimerStepParser());
        stepParserMap.put(ApiScenarioStepType.LOOP_CONTROLLER.name(), new LoopControllerStepParser());
        stepParserMap.put(ApiScenarioStepType.ONCE_ONLY_CONTROLLER.name(), new OnceOnlyControllerStepParser());
        stepParserMap.put(ApiScenarioStepType.IF_CONTROLLER.name(), new IfControllerStepParser());
        stepParserMap.put(ApiScenarioStepType.JMETER_COMPONENT.name(), new JMeterComponentStepParser());

    }

    public static StepParser getStepParser(String stepType) {
        StepParser stepParser = stepParserMap.get(stepType);
        return stepParser == null ? defaultStepParser : stepParser;
    }
}
