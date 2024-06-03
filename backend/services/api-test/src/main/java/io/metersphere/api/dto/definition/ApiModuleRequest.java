package io.metersphere.api.dto.definition;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.system.dto.sdk.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ApiModuleRequest extends BaseCondition {
    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.protocol.not_blank}")
    @Size(min = 1, max = 20, message = "{api_definition_module.protocol.length_range}")
    private String protocol = ModuleConstants.NODE_PROTOCOL_HTTP;
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_module.project_id.length_range}")
    private String projectId;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "版本fk")
    @Size(max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "版本引用fk")
    @Size(max = 50, message = "{api_definition.ref_id.length_range}")
    private String refId;

    @Schema(description = "测试计划id")
    private String testPlanId;
}
