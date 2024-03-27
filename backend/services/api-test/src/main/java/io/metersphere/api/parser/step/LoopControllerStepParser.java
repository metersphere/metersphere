package io.metersphere.api.parser.step;

import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.api.dto.request.controller.MsLoopController;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;

public class LoopControllerStepParser extends StepParser {

    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        return parseConfig2TestElement(step, MsLoopController.class);
    }

    @Override
    public Object parseDetail(ApiScenarioStep step) {
        return null;
    }
}
