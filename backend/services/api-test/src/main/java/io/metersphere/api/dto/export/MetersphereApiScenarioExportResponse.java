package io.metersphere.api.dto.export;

import io.metersphere.api.domain.ApiScenarioCsv;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.dto.scenario.ApiScenarioStepDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
@Data
public class MetersphereApiScenarioExportResponse extends ApiScenarioExportResponse {
    @Schema(description = "是否包含关联资源")
    private boolean hasRelatedResource;
    @Schema(description = "导出的场景")
    private List<ApiScenarioDetail> exportScenarioList = new ArrayList<>();

    @Schema(description = "场景CSV相关的数据")
    private List<ApiScenarioCsv> apiScenarioCsvList = new ArrayList<>();

    @Schema(description = "所有场景步骤")
    private List<ApiScenarioStepDTO> scenarioStepList = new ArrayList<>();

    @Schema(description = "所有场景步骤内容")
    private Map<String, String> scenarioStepBlobMap = new HashMap<>();


    @Schema(description = "有关联的接口定义")
    private List<ApiDefinitionDetail> relatedApiDefinitions = new ArrayList<>();

    @Schema(description = "有关联的接口用例")
    private List<ApiTestCaseDTO> relatedApiTestCaseList = new ArrayList<>();

    @Schema(description = "有关联的场景")
    private List<ApiScenarioDetail> relatedScenarioList = new ArrayList<>();

    public void addExportApi(ApiDefinitionDetail detail) {
        relatedApiDefinitions.add(detail);
    }

    public void addExportApiCase(ApiTestCaseDTO apiCase) {
        relatedApiTestCaseList.add(apiCase);
    }

    public void addExportScenario(ApiScenarioDetail apiScenarioDetail) {
        exportScenarioList.add(apiScenarioDetail);
    }
}
