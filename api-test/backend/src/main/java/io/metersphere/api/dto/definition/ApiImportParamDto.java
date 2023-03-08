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
    private List<ApiDefinitionWithBLOBs> optionData;
    private Boolean fullCoverage;
    private ApiTestImportRequest request;
    private Map<String, ApiModule> moduleMap;
    private List<ApiDefinitionWithBLOBs> toUpdateList;
    private List<ApiTestCaseWithBLOBs> optionDataCases;

    private List<ApiDefinitionWithBLOBs> data;
    private Map<String, List<ApiModule>> pidChildrenMap;
    private Map<String, ApiModuleDTO> idModuleMap;
    private boolean urlRepeat;
    private List<ApiTestCaseWithBLOBs> importCases;

    private List<ApiTestCaseWithBLOBs> repeatCaseList;
    private Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap;


    public ApiImportParamDto(ApiModuleDTO chooseModule, Map<String, String> idPathMap, List<ApiDefinitionWithBLOBs> optionData, Boolean fullCoverage, ApiTestImportRequest request, Map<String, ApiModule> moduleMap, List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiTestCaseWithBLOBs> optionDataCases) {
        this.chooseModule = chooseModule;
        this.idPathMap = idPathMap;
        this.optionData = optionData;
        this.fullCoverage = fullCoverage;
        this.request = request;
        this.moduleMap = moduleMap;
        this.toUpdateList = toUpdateList;
        this.optionDataCases = optionDataCases;
    }


    public ApiImportParamDto(List<ApiDefinitionWithBLOBs> data, Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap, Map<String, ApiModuleDTO> idModuleMap, ApiTestImportRequest request,
                             Boolean fullCoverage, boolean urlRepeat, List<ApiTestCaseWithBLOBs> importCases) {
        this.data = data;
        this.pidChildrenMap = pidChildrenMap;
        this.idPathMap = idPathMap;
        this.idModuleMap = idModuleMap;
        this.request = request;
        this.fullCoverage = fullCoverage;
        this.urlRepeat = urlRepeat;
        this.importCases = importCases;
    }
}
