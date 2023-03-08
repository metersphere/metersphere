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
    private Boolean fullCoverage;
    private ApiModuleDTO chooseModule;
    private Map<String, String> idPathMap;
    private Map<String, ApiModule> moduleMap;
    private List<ApiDefinitionWithBLOBs> definitionWithBLOBs;
    private List<ApiTestCaseWithBLOBs> caseWithBLOBs;
    private List<ApiTestCaseWithBLOBs> repeatCaseList;
    private Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap;


}
