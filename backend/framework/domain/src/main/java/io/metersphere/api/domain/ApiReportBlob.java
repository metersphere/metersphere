package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiReportBlob implements Serializable {
    @Schema(title = "接口报告fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "结果内容详情")
    private byte[] content;

    @Schema(title = "执行环境配置")
    private byte[] config;

    @Schema(title = "执行过程日志")
    private byte[] console;

    private static final long serialVersionUID = 1L;
}