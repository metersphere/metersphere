package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.ApiScenario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiScenarioDTO extends ApiScenario {

    private String projectName;
    private String userName;
}
