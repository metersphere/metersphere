package io.metersphere.api.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "场景报告过程日志")
@Table("api_scenario_report_log")
@Data
public class ApiScenarioReportLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_report_log.report_id.not_blank}", groups = {Updated.class})
    @Schema(title = "请求资源 id", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String reportId;

    @Schema(title = "执行日志", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] console;

}