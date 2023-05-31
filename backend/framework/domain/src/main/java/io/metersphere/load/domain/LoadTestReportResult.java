package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportResult implements Serializable {
    @Schema(title = "主键无实际意义", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_result.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report_result.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_result.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report_result.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "报告项目key")
    private String reportKey;

    @Schema(title = "报告项目内容")
    private byte[] reportValue;

    private static final long serialVersionUID = 1L;
}