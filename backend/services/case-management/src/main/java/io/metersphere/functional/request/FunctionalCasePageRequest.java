package io.metersphere.functional.request;

import io.metersphere.functional.dto.AssociationDTO;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCasePageRequest extends BasePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "版本来源")
    private String refId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "匹配模式 所有/任一", allowableValues = {"AND", "OR"})
    private String searchMode = "AND";


    @Schema(description = "关联用例")
    private AssociationDTO associationCase;

    @Schema(description = "关联需求")
    private AssociationDTO associationDemand;

    @Schema(description = "关联缺陷")
    private AssociationDTO associationBug;

}
