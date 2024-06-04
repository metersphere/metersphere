package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanBatchProcessRequest extends TableBatchProcessDTO {

    @Schema(description = "项目ID")
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description = "模块ID")
    private List<String> moduleIds;

    @Schema(description = "类型", allowableValues = {"ALL", "TEST_PLAN", "GROUP"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type = "ALL";

}
