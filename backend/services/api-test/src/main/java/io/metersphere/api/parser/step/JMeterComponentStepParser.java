package io.metersphere.api.parser.step;

import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.api.dto.request.MsJMeterComponent;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;

public class JMeterComponentStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        return ApiDataUtils.parseObject(stepDetail, MsJMeterComponent.class);
    }

    @Override
    public Object parseDetail(ApiScenarioStep step) {
        return null;
    }
}
