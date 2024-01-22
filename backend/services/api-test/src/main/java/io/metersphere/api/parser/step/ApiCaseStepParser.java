package io.metersphere.api.parser.step;

import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public class ApiCaseStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parse(ApiScenarioStepRequest step, String resourceBlob, String stepDetail) {
        if (isRef(step.getRefType())) {
            return StringUtils.isBlank(resourceBlob) ? null : parse2MsTestElement(resourceBlob);
        } else {
            if (StringUtils.isBlank(stepDetail)) {
                return null;
            }
            return StringUtils.isBlank(stepDetail) ? null : ApiDataUtils.parseObject(stepDetail, AbstractMsTestElement.class);
        }
    }
}
