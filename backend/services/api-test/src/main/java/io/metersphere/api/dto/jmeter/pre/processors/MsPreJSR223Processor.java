package io.metersphere.api.dto.jmeter.pre.processors;

import io.metersphere.plugin.api.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.TestElementDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MSPreJSR223Processor")
public class MsPreJSR223Processor extends TestElementDTO {
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;
}
