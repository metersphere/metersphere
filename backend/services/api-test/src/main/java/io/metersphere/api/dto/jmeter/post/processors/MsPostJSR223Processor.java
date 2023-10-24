package io.metersphere.api.dto.jmeter.post.processors;

import io.metersphere.plugin.api.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.TestElementDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MSPostJSR223Processor")
public class MsPostJSR223Processor extends TestElementDTO {
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;

}
