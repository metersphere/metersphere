package io.metersphere.api.dto.definition;

import io.metersphere.sdk.dto.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiModuleRequest extends BaseCondition {
    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();
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

    @Schema(description = "本次查询包含的ID(一般由后台计算后得出)")
    private List<String> includeIds = new ArrayList<>();

    @Schema(description = "本次查询不包含的ID(一般由后台计算后得出)")
    private List<String> excludeIds = new ArrayList<>();

}
