package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.ApiScenarioModule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiScenarioModuleDTO extends ApiScenarioModule {

    private String label;
    private List<ApiScenarioModuleDTO> children;

}
