package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportFile implements Serializable {
    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_file.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report_file.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_file.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report_file.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(title = "文件排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{load_test_report_file.sort.not_blank}", groups = {Created.class})
    private Integer sort;

    private static final long serialVersionUID = 1L;
}