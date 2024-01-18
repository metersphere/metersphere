package io.metersphere.api.dto.definition;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiReportBatchRequest extends TableBatchProcessDTO {
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

}
