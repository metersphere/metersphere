package io.metersphere.api.dto.scenario;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioBatchRequest extends TableBatchProcessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "场景pk")
    private String apiScenarioId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "模块ID")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "版本fk")
    private String versionId;

    @Schema(description = "版本来源")
    private String refId;

}
