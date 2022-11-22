package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.commons.constants.ElementConstants;
import lombok.Data;

@Data
public class EnvJSR223Processor extends BaseEnvElement {
    private String type = ElementConstants.JSR223;
    private String clazzName = EnvJSR223Processor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;
}
