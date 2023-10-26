package io.metersphere.api.dto.request.assertion;

import io.metersphere.plugin.api.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.TestElementDTO;
import io.metersphere.project.dto.environment.assertions.document.MsAssertionDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MsAssertions")
public class MsAssertions extends TestElementDTO {
    private String xpathType;
    private boolean scenarioAss;
    private List<MsAssertionRegex> regex;
    private List<MsAssertionJsonPath> jsonPath;
    private List<MsAssertionJSR223> jsr223;
    private List<MsAssertionXPath2> xpath2;
    private MsAssertionDuration duration;
    private MsAssertionDocument document;
}
