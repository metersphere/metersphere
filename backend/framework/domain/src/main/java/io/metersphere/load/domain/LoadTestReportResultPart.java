package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportResultPart implements Serializable {
    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_result_part.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report_result_part.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "报告项目key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_result_part.report_key.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{load_test_report_result_part.report_key.length_range}", groups = {Created.class, Updated.class})
    private String reportKey;

    @Schema(title = "资源节点索引号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{load_test_report_result_part.resource_index.not_blank}", groups = {Created.class})
    private Integer resourceIndex;

    @Schema(title = "报告项目内容")
    private byte[] reportValue;

    private static final long serialVersionUID = 1L;
}