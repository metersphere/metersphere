package io.metersphere.api.dto.automation;

import io.metersphere.dto.TreeNodeDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiScenarioModuleDTO extends TreeNodeDTO<ApiScenarioModuleDTO> {
    private String path;
}
