package io.metersphere.api.dto.definition;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionPageRequest extends BasePageRequest {

    @Schema(description = "接口pk")
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
    private String id;

    @Schema(description = "接口名称")
    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}")
    private String name;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "版本fk")
    @Size(min = 1, max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "版本引用fk")
    @Size(min = 1, max = 50, message = "{api_definition.ref_id.length_range}")
    private String refId;

    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "删除状态(状态为 1 时为回收站数据)")
    private Boolean deleted = false;

    @Schema(description = "本次查询包含的ID(一般由后台计算后得出)")
    private List<String> includeIds = new ArrayList<>();

    @Schema(description = "本次查询不包含的ID(一般由后台计算后得出)")
    private List<String> excludeIds = new ArrayList<>();
}
