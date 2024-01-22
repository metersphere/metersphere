package io.metersphere.api.parser.step;

import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.JSON;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public class ApiScenarioStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parse(ApiScenarioStepRequest step, String resourceBlob, String stepDetail) {
        MsScenario msScenario = new MsScenario();
        if (isRef(step.getRefType())) {
            if (StringUtils.isNotBlank(resourceBlob)) {
                msScenario.setScenarioConfig(JSON.parseObject(resourceBlob, ScenarioConfig.class));
            }
        } else {
            if (StringUtils.isNotBlank(stepDetail)) {
                msScenario.setScenarioConfig(JSON.parseObject(stepDetail, ScenarioConfig.class));
            }
        }
        return msScenario;
    }
}
