package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.api.dto.definition.request.assertions.MsAssertionDuration;
import io.metersphere.api.dto.definition.request.assertions.MsAssertionJSR223;
import io.metersphere.api.dto.definition.request.assertions.MsAssertionJsonPath;
import io.metersphere.api.dto.definition.request.assertions.MsAssertionRegex;
import io.metersphere.api.dto.definition.request.assertions.MsAssertionXPath2;
import io.metersphere.api.dto.definition.request.assertions.document.MsAssertionDocument;
import io.metersphere.commons.constants.ElementConstants;
import lombok.Data;

import java.util.List;

@Data
public class EnvAssertions extends BaseEnvElement {
    private String clazzName = EnvAssertions.class.getCanonicalName();

    private boolean scenarioAss;
    private List<MsAssertionRegex> regex;
    private List<MsAssertionJsonPath> jsonPath;
    private List<MsAssertionJSR223> jsr223;
    private List<MsAssertionXPath2> xpath2;
    private MsAssertionDuration duration;
    private String type = ElementConstants.ASSERTIONS;
    private MsAssertionDocument document;
    private String xpathType;
}
