package io.metersphere.api.parser.step;

import io.metersphere.api.dto.request.controller.MsOnceOnlyController;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepDetailRequest;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.BeanUtils;

public class OnceOnlyControllerStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        MsOnceOnlyController msOnceOnlyController = new MsOnceOnlyController();
        BeanUtils.copyBean(msOnceOnlyController, step);
        return msOnceOnlyController;
    }

    @Override
    public Object parseDetail(ApiScenarioStepDetailRequest step) {
        return null;
    }
}
