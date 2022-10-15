package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.commons.constants.ElementConstants;
import lombok.Data;

@Data
public class EnvJSR223PostProcessor extends BaseEnvElement {
    private String type = ElementConstants.JSR223_POST;
    private String clazzName = EnvJSR223PostProcessor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;
}
