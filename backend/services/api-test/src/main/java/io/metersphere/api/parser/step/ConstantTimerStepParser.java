package io.metersphere.api.parser.step;

import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.api.dto.request.controller.MsConstantTimerController;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.BeanUtils;

import java.util.LinkedHashMap;

public class ConstantTimerStepParser extends StepParser {
    private static final String WAIT_TIME = "waitTime";
    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        MsConstantTimerController msConstantTimerController = new MsConstantTimerController();
        BeanUtils.copyBean(msConstantTimerController, step);
        LinkedHashMap msTime = (LinkedHashMap) step.getConfig();
        msConstantTimerController.setDelay(Long.valueOf((Integer) msTime.getOrDefault(WAIT_TIME, 0)));
        return msConstantTimerController;
    }

    @Override
    public Object parseDetail(ApiScenarioStep step) {
        return null;
    }
}
