package io.metersphere.api.dto.automation;


import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiScenarioBatchRequest extends ApiScenarioWithBLOBs {
    private List<String> ids;
    private String projectId;
    private String environmentId;

    private ApiScenarioRequest condition;
}
