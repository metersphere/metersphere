package io.metersphere.api.dto.scenario;

import io.metersphere.system.dto.sdk.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ApiScenarioModuleRequest extends BaseCondition {
    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<@NotBlank String> moduleIds;
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.project_id.not_blank}")
    private String projectId;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "版本fk")
    private String versionId;

    @Schema(description = "版本引用fk")
    private String refId;
}
