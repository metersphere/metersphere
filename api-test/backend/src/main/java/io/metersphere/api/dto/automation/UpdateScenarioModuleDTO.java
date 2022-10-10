package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateScenarioModuleDTO {
    private List<ApiScenarioModule> moduleList;
    private List<ApiScenarioWithBLOBs> needUpdateList;
    private List<ApiScenarioWithBLOBs> apiScenarioWithBLOBsList;
}
