package io.metersphere.functional.request;

import io.metersphere.functional.dto.MinderOptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class FunctionalCaseMinderEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "新增/修改的用例对象集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<FunctionalCaseChangeRequest> updateCaseList;

    @Schema(description = "新增/修改的模块集合（只记录操作的节点，节点下的子节点不需要记录）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<FunctionalCaseModuleEditRequest> updateModuleList;

    @Schema(description = "新增/修改的空白节点集合（只记录操作的节点，节点下的子节点不需要记录）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MindAdditionalNodeRequest> additionalNodeList;

    @Schema(description = "删除的模块/用例的集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MinderOptionDTO> deleteResourceList;

}
