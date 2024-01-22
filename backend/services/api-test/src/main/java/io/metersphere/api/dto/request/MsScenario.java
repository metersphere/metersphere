package io.metersphere.api.dto.request;

import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsScenario extends AbstractMsTestElement {
    private ScenarioConfig scenarioConfig;
}
