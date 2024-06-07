package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ScenarioSystemRequest {
    @Schema(description = "模块id集合")
    private List<@NotBlank String> moduleIds = new ArrayList<>();
    @Schema(description = "选中的id集合")
    private List<@NotBlank String> selectedIds = new ArrayList<>();
    @Schema(description = "未选中的id集合")
    private List<@NotBlank String> unselectedIds = new ArrayList<>();
    @Schema(description = "项目id")
    @NotBlank(message = "{api_scenario.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_scenario.project_id.length_range}")
    private String projectId;
    @Schema(description = "协议  接口和用例的时候  是必传的", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();
    @Schema(description = "版本id")
    private String versionId;
}
