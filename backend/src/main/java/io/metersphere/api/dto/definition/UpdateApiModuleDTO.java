package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UpdateApiModuleDTO {
    private Map<String, ApiModule> moduleMap;
    private List<ApiDefinitionWithBLOBs> needUpdateList;
    private List<ApiDefinitionWithBLOBs> definitionWithBLOBs;
    private List<ApiTestCaseWithBLOBs> caseWithBLOBs;

}
