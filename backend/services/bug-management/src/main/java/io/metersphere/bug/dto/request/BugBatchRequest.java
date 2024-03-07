package io.metersphere.bug.dto.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugBatchRequest extends TableBatchProcessDTO {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "是否回收站")
    private boolean useTrash;

    @Schema(description = "排序参数")
    private String sort;
}
