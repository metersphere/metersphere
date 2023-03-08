package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class ApiImportParamDto {
    private ApiModuleDTO chooseModule;
    private Map<String, String> idPathMap;
    private List<ApiDefinitionWithBLOBs> optionDatas;
    private Boolean fullCoverage;
    private ApiTestImportRequest request;
    private Map<String, ApiModule> moduleMap;
    private List<ApiDefinitionWithBLOBs> toUpdateList;
    private List<ApiTestCaseWithBLOBs> optionDataCases;
    private List<ApiTestCaseWithBLOBs> repeatCaseList;
    private Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap;

    public ApiImportParamDto(ApiModuleDTO chooseModule, Map<String, String> idPathMap, List<ApiDefinitionWithBLOBs> optionDatas, Boolean fullCoverage, ApiTestImportRequest request, Map<String, ApiModule> moduleMap, List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiTestCaseWithBLOBs> optionDataCases) {
        this.chooseModule = chooseModule;
        this.idPathMap = idPathMap;
        this.optionDatas = optionDatas;
        this.fullCoverage = fullCoverage;
        this.request = request;
        this.moduleMap = moduleMap;
        this.toUpdateList = toUpdateList;
        this.optionDataCases = optionDataCases;
    }
}
