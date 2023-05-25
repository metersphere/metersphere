package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportFile implements Serializable {
    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{load_test_report_file.report_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report_file.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{load_test_report_file.file_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report_file.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(title = "文件排序", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 10]")
    @NotBlank(message = "{load_test_report_file.sort.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 10, message = "{load_test_report_file.sort.length_range}", groups = {Created.class, Updated.class})
    private Integer sort;

    private static final long serialVersionUID = 1L;
}