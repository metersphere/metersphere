package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.scenario.ApiScenarioImportDetail;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiScenarioPreImportAnalysisResult {

    @Schema(description = "需要创建的模块数据")
    List<BaseTreeNode> insertScenarioModuleList = new ArrayList<>();

    @Schema(description = "需要新增的场景")
    List<ApiScenarioImportDetail> insertApiScenarioData = new ArrayList<>();

    @Schema(description = "需要更新的场景")
    List<ApiScenarioImportDetail> updateApiScenarioData = new ArrayList<>();

    @Schema(description = "需要新增的接口定义， ID已经生成好")
    private List<ApiDefinitionDetail> insertApiDefinitions = new ArrayList<>();

    @Schema(description = "需要新增的接口定义模块")
    private List<BaseTreeNode> insertApiModuleList = new ArrayList<>();

    @Schema(description = "需要新增的接口用例， ID已经生成好")
    private List<ApiTestCaseDTO> insertApiTestCaseList = new ArrayList<>();

    public void setApiDefinition(ApiDefinitionDetail insertApi) {
        this.insertApiDefinitions.add(insertApi);
    }

    public void setApiTestCase(ApiTestCaseDTO insertCase) {
        this.insertApiTestCaseList.add(insertCase);
    }

    public void setApiScenario(ApiScenarioImportDetail insertScenario) {
        this.insertApiScenarioData.add(insertScenario);
    }
}