package io.metersphere.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.dto.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AssociateCaseModuleRequest extends BaseCondition {
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

    @Schema(description = "版本fk")
    private String versionId;

    @Schema(description = "版本引用fk")
    private String refId;

    @Schema(description = "关联关系表里主ID eg:功能用例关联接口用例时为功能用例ID, 缺陷关联用例为缺陷ID")
    @Size(min = 1, max = 50, message = "{relate_source_id_length_range}")
    private String sourceId;

    @Schema(description = "关联类型(FUNCTIONAL, API, SCENARIO, UI, PERFORMANCE)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{relate_source_type_not_blank}")
    private String sourceType;
}
