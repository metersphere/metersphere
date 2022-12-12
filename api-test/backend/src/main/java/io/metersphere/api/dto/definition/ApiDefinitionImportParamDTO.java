package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.mock.config.MockConfigImportDTO;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApiDefinitionImportParamDTO {
    private ApiDefinitionWithBLOBs apiDefinition;
    private ApiTestImportRequest apiTestImportRequest;
    private List<MockConfigImportDTO> mocks;
    private List<ApiDefinitionWithBLOBs> updateList;
    private List<ApiTestCaseWithBLOBs> caseList;

    private List<ApiDefinitionWithBLOBs> repeatList;






    public ApiDefinitionImportParamDTO() {
    }

    public ApiDefinitionImportParamDTO(ApiDefinitionWithBLOBs apiDefinition, ApiTestImportRequest apiTestImportRequest, List<MockConfigImportDTO> mocks, List<ApiDefinitionWithBLOBs> updateList, List<ApiTestCaseWithBLOBs> caseList) {
        this.apiDefinition = apiDefinition;
        this.apiTestImportRequest = apiTestImportRequest;
        this.mocks = mocks;
        this.updateList = updateList;
        this.caseList = caseList;
    }


}
