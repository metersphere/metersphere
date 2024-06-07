package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 */
@Data
public class ApiDefinitionDocRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "接口pk")
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
    private String apiId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();

    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "类型(ALL,MODULE,API)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "版本fk")
    @Size(min = 1, max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "删除状态(状态为 1 时为回收站数据)")
    private Boolean deleted = false;
}
