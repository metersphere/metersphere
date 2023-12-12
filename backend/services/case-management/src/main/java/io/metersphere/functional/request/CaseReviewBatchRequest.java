package io.metersphere.functional.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class CaseReviewBatchRequest extends TableBatchProcessDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "目标模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveModuleId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "我评审的")
    private String reviewByMe;

    @Schema(description = "我创建的")
    private String createByMe;

}
