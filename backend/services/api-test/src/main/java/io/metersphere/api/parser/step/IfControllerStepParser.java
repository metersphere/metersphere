package io.metersphere.api.parser.step;

import io.metersphere.api.dto.request.controller.MsIfController;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepDetailRequest;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;

public class IfControllerStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        return parseConfig2TestElement(step, MsIfController.class);
    }

    @Override
    public Object parseDetail(ApiScenarioStepDetailRequest step) {
        return null;
    }
}
