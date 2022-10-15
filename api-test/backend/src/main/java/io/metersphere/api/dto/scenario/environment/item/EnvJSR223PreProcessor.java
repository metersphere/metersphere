package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.commons.constants.ElementConstants;
import lombok.Data;

@Data
public class EnvJSR223PreProcessor extends BaseEnvElement {
    private String type = ElementConstants.JSR223_PRE;
    private String clazzName = EnvJSR223PreProcessor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;
}
