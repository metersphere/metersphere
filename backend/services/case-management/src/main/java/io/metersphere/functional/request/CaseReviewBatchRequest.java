package io.metersphere.functional.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class CaseReviewBatchRequest extends TableBatchProcessDTO implements Serializable {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "目标模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveModuleId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

}
