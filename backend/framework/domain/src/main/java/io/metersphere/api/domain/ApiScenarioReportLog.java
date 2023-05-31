package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReportLog implements Serializable {
    @Schema(title = "请求资源 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report_log.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report_log.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "执行日志")
    private byte[] console;

    private static final long serialVersionUID = 1L;
}