package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioReportEnvironment implements Serializable {
    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_environment.report_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_environment.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "项目ID")
    private String projectId;

    @Schema(title = "环境ID")
    private String environmentId;

    private static final long serialVersionUID = 1L;
}