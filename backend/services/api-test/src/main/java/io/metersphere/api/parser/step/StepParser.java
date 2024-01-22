package io.metersphere.api.parser.step;

import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public abstract class StepParser {

    public abstract AbstractMsTestElement parse(ApiScenarioStepRequest step, String resourceBlob, String stepDetail);

    protected boolean isRef(String refType) {
        return StringUtils.equalsAny(refType, ApiScenarioStepRefType.REF.name(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    protected static AbstractMsTestElement parse2MsTestElement(String blobContent) {
        return ApiDataUtils.parseObject(blobContent, AbstractMsTestElement.class);
    }
}
