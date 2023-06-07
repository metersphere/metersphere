package io.metersphere.api.dto.jmeter.processors;

import io.metersphere.plugin.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.BaseConfigDTO;
import io.metersphere.plugin.api.dto.TestElementDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MSJSR223Processor")
public class MSJSR223Processor extends TestElementDTO {
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;

    @Override
    public void toHashTree(HashTree tree, List<TestElementDTO> hashTree, BaseConfigDTO baseConfig) {
        if (!this.isEnable()) {
            return;
        }
    }
}
