package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReportBlob implements Serializable {
    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report_blob.report_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report_blob.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "压力配置")
    private byte[] loadConfiguration;

    @Schema(title = "JMX内容")
    private byte[] jmxContent;

    @Schema(title = "高级配置")
    private byte[] advancedConfiguration;

    @Schema(title = "环境信息 (JSON format)")
    private byte[] envInfo;

    private static final long serialVersionUID = 1L;
}