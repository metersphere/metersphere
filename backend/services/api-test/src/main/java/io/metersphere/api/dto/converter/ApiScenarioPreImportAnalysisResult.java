package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.scenario.ApiScenarioImportDetail;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口导入数据准备结果
 */
@Data
public class ApiScenarioPreImportAnalysisResult {

    @Schema(description = "需要创建的模块数据")
    List<BaseTreeNode> insertModuleList = new ArrayList<>();

    @Schema(description = "需要新增的场景")
    List<ApiScenarioImportDetail> insertApiScenarioData = new ArrayList<>();

    @Schema(description = "需要更新的场景")
    List<ApiScenarioImportDetail> updateApiScenarioData = new ArrayList<>();

}
