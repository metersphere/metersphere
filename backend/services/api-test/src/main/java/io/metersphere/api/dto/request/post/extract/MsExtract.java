package io.metersphere.api.dto.request.post.extract;

import io.metersphere.plugin.api.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.TestElementDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MsExtract")
public class MsExtract extends TestElementDTO {
    private String xpathType;
    private List<MsExtractRegex> regex;
    private List<MsExtractJSONPath> json;
    private List<MsExtractXPath> xpath;
}
