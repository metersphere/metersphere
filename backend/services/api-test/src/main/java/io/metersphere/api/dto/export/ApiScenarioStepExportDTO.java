package io.metersphere.api.dto.export;

import io.metersphere.api.domain.ApiScenarioStep;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import lombok.Data;

@Data
public class ApiScenarioStepExportDTO extends ApiScenarioStep {
    private AbstractMsTestElement stepComponent;
}
