package io.metersphere.api.dto.request.processors;

import io.metersphere.plugin.api.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.TestElementDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MsJSR223Processor")
public class MsJSR223Processor extends TestElementDTO {
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;
}
