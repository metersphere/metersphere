package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.mockconfig.MockConfigImportDTO;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MsApiExportResult extends ApiExportResult {
    private String projectName;
    private String protocol;
    private String projectId;
    private String version;
    private List<ApiDefinitionWithBLOBs> data;
    private List<ApiTestCaseWithBLOBs> cases;
    private List<MockConfigImportDTO> mocks;
}
