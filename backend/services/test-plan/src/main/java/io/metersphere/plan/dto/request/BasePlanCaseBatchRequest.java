package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
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
public class BasePlanCaseBatchRequest extends TableBatchProcessDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "计划集id")
    private String collectionId;

    @Schema(description = "项目Id")
    private String projectId;

    @Schema(description = "是否包含空执行人")
    private boolean nullExecutorKey;
}
