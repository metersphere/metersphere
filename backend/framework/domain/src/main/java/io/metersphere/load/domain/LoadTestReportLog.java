package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportLog implements Serializable {
    @Schema(description =  "主键无实际意义", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_log.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report_log.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_log.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report_log.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(description =  "资源节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_log.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report_log.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description =  "日志内容分段")
    private Long part;

    @Schema(description =  "jmeter.log 内容")
    private byte[] content;

    private static final long serialVersionUID = 1L;
}