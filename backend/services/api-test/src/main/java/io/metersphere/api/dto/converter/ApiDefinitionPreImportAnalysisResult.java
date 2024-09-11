package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口导入数据准备结果
 */
@Data
public class ApiDefinitionPreImportAnalysisResult {
    @Schema(description = "需要创建的模块数据")
    List<BaseTreeNode> insertModuleList = new ArrayList<>();

    @Schema(description = "需要新增的接口")
    List<ApiDefinitionDetail> insertApiData = new ArrayList<>();

    @Schema(description = "需要更新的接口")
    List<ApiDefinitionDetail> updateApiData = new ArrayList<>();
    @Schema(description = "只需要更所属模块的接口")
    List<ApiDefinitionDetail> updateModuleApiList = new ArrayList<>();


    @Schema(description = "需要新增的接口用例")
    List<ApiTestCaseDTO> insertApiCaseList = new ArrayList<>();
    @Schema(description = "需要修改的接口用例")
    List<ApiTestCaseDTO> updateApiCaseList = new ArrayList<>();

    @Schema(description = "需要新增的Mock")
    List<ApiDefinitionMockDTO> insertApiMockList = new ArrayList<>();
    @Schema(description = "需要修改的Mock")
    List<ApiDefinitionMockDTO> updateApiMockList = new ArrayList<>();
}
