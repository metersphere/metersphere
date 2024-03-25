package io.metersphere.api.parser.step;

import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.api.dto.request.controller.LoopType;
import io.metersphere.api.dto.request.controller.MsLoopController;
import io.metersphere.api.dto.request.controller.loop.*;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;

public class LoopControllerStepParser extends StepParser {
    private static final String LOOP_TYPE = "loopType";
    private static final String LOOP_COUNT = "msCountController";
    private static final String FOREACH = "forEachController";
    private static final String WHILE = "whileController";
    private static final String LOOPS = "loops";
    private static final String LOOP_TIME = "loopTime";
    private static final String VALUE = "value";
    private static final String VARIABLE = "variable";
    private static final String CONDITION_TYPE = "conditionType";
    private static final String CONDITION_ = "CONDITION";
    private static final String WHILE_VARIABLE = "msWhileVariable";
    private static final String WHILE_SCRIPT = "msWhileScript";
    private static final String TIMEOUT = "timeout";
    private static final String SCRIPT = "scriptValue";
    private static final String CONDITION = "condition";

    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        MsLoopController msLoopController = new MsLoopController();
        BeanUtils.copyBean(msLoopController, step);
        LinkedHashMap msLoop = (LinkedHashMap) step.getConfig();
        String loopType = String.valueOf(msLoop.get(LOOP_TYPE));
        msLoopController.setLoopType(loopType);
        LinkedHashMap msCount = (LinkedHashMap) msLoop.get(LOOP_COUNT);
        LinkedHashMap msForeach = (LinkedHashMap) msLoop.get(FOREACH);
        LinkedHashMap msWhile = (LinkedHashMap) msLoop.get(WHILE);
        switch (LoopType.valueOf(loopType)) {
            case LoopType.FOREACH:
                MsForEachController msForEachController = new MsForEachController();
                msForEachController.setValue(String.valueOf(msForeach.get(VALUE)));
                msForEachController.setVariable(String.valueOf(msForeach.get(VARIABLE)));
                msForEachController.setLoopTime(Long.parseLong(String.valueOf(msForeach.get(LOOP_TIME))));
                msLoopController.setForEachController(msForEachController);
                break;
            case LoopType.WHILE:
                MsWhileController whileController = new MsWhileController();
                LinkedHashMap whileVariable = (LinkedHashMap)msWhile.get(WHILE_VARIABLE);
                LinkedHashMap whileScript = (LinkedHashMap) msWhile.get(WHILE_SCRIPT);
                String conditionType = String.valueOf(msWhile.get(CONDITION_TYPE));
                whileController.setConditionType(conditionType);
                whileController.setTimeout(Long.parseLong(String.valueOf(msWhile.get(TIMEOUT))));
                if (StringUtils.equals(conditionType, CONDITION_)) {
                    MsWhileVariable msWhileVariable = new MsWhileVariable();
                    msWhileVariable.setVariable(String.valueOf(whileVariable.get(VARIABLE)));
                    msWhileVariable.setValue(String.valueOf(whileVariable.get(VALUE)));
                    msWhileVariable.setCondition(String.valueOf(whileVariable.get(CONDITION)));
                    whileController.setMsWhileVariable(msWhileVariable);
                } else {
                    MsWhileScript msWhileScript = new MsWhileScript();
                    msWhileScript.setScriptValue(whileScript.get(SCRIPT).toString());
                    whileController.setMsWhileScript(msWhileScript);
                }
                break;
            default:
                MsCountController msCountController = new MsCountController();
                msCountController.setLoops(String.valueOf(msCount.get(LOOPS)));
                msCountController.setLoopTime(Long.parseLong(String.valueOf(msForeach.get(LOOP_TIME))));
                msLoopController.setMsCountController(msCountController);
                break;
        }
        return msLoopController;

    }

    @Override
    public Object parseDetail(ApiScenarioStep step) {
        return null;
    }
}
