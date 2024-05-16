package io.metersphere.functional.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author guoyuqi
 */
@Data
public class FunctionalCaseModuleEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块id（新增的时候前端给一个uuid）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module.id.not_blank}")
    private String id;

    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "父模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentId = ModuleConstants.ROOT_NODE_PARENT_ID;

    @Schema(description = "操作类型（新增(ADD)/更新(UPDATE)）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.relation.not_blank}")
    private String type;

    @Schema(description = "移动方式（节点移动或新增时需要）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveMode;

    @Schema(description = "移动目标（节点移动或新增时需要）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetId;

}

