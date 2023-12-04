package io.metersphere.functional.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author guoyuqi
 */
@Data
public class CaseReviewBatchRequest extends TableBatchProcessDTO implements Serializable {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleId;

}
