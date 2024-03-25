package io.metersphere.api.parser.step;

import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.api.dto.request.controller.MsIfController;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;

public class IfControllerStepParser extends StepParser {
    private static final String CONDITION = "condition";
    private static final String VARIABLE = "variable";
    private static final String VALUE = "value";
    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        MsIfController msIfController = new MsIfController();
        BeanUtils.copyBean(msIfController, step);
        LinkedHashMap msIf = (LinkedHashMap) step.getConfig();
        msIfController.setCondition(String.valueOf(msIf.getOrDefault(CONDITION,StringUtils.EMPTY)));
        msIfController.setVariable(String.valueOf(msIf.getOrDefault(VARIABLE,StringUtils.EMPTY)));
        msIfController.setValue(String.valueOf(msIf.getOrDefault(VALUE, StringUtils.EMPTY)));
        return msIfController;
    }

    @Override
    public Object parseDetail(ApiScenarioStep step) {
        return null;
    }
}
