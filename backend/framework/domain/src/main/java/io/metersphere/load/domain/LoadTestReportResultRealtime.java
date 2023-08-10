package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportResultRealtime implements Serializable {
    @Schema(description =  "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_result_realtime.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report_result_realtime.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(description =  "报告项目key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_result_realtime.report_key.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{load_test_report_result_realtime.report_key.length_range}", groups = {Created.class, Updated.class})
    private String reportKey;

    @Schema(description =  "资源节点索引号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{load_test_report_result_realtime.resource_index.not_blank}", groups = {Created.class})
    private Integer resourceIndex;

    @Schema(description =  "报告项目内容排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{load_test_report_result_realtime.sort.not_blank}", groups = {Created.class})
    private Integer sort;

    @Schema(description =  "报告项目内容")
    private byte[] reportValue;

    private static final long serialVersionUID = 1L;
}