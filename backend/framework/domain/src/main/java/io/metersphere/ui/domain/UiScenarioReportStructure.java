package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioReportStructure implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_structure.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_structure.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "请求资源 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_structure.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_structure.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "资源步骤结构树", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_report_structure.resource_tree.not_blank}", groups = {Created.class})
    private byte[] resourceTree;

    private static final long serialVersionUID = 1L;
}