package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionExportDetail extends ApiDefinitionDetail {

    @Schema(description = "接口用例")
    private List<ApiTestCaseDTO> apiTestCaseList = new ArrayList<>();

    @Schema(description = "Mock")
    private List<ApiDefinitionMockDTO> apiMockList = new ArrayList<>();

}
