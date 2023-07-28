package io.metersphere.api.dto.jmeter.processors;

import io.metersphere.plugin.api.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.TestElementDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MSJSR223Processor")
public class MSJSR223Processor extends TestElementDTO {
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;
}
