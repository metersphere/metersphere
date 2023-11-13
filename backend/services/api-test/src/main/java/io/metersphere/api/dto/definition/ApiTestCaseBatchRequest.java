package io.metersphere.api.dto.definition;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiTestCaseBatchRequest extends TableBatchProcessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "接口ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String apiDefinitionId;

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String moduleId;

    @Schema(description = "协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protocol;

}
