package io.metersphere.functional.request;

import io.metersphere.functional.dto.MinderOptionDTO;
import io.metersphere.functional.dto.MinderTargetDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class FunctionalCaseMinderRemoveRequest {

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "成为移动节点的父节点的目标id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentTargetId;

    @Schema(description = "成为移动节点拖拽目标的用例对象", requiredMode = Schema.RequiredMode.REQUIRED)
    private MinderTargetDTO caseMinderTargetDTO;

    @Schema(description = "成为移动节点拖拽目标的模块对象", requiredMode = Schema.RequiredMode.REQUIRED)
    private MinderTargetDTO moduleMinderTargetDTO;

    @Schema(description = "节点和节点类型的集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MinderOptionDTO> resourceList;

    @Schema(description = "如果是修改步骤描述的顺序，将修改后的json赋值到这里")
    private String steps;

}
