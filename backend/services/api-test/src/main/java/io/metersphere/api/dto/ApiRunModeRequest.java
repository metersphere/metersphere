package io.metersphere.api.dto;

import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: jianxing
 * @CreateTime: 2024-03-18  10:49
 */
@Data
public class ApiRunModeRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @EnumValue(enumClass = ApiBatchRunMode.class)
    @Schema(description = "运行模式(SERIAL 串行/ PARALLEL并行)")
    @NotBlank
    private String runMode;

    @Schema(description = "是否是集成报告")
    private Boolean integratedReport;

    @Schema(description = "集合报告名称")
    private String integratedReportName;

    @Schema(description = "失败停止")
    private Boolean stopOnFailure  = false;

    @Schema(description = "资源池ID")
    private String poolId;

    @Schema(description = "是否为环境组")
    private Boolean grouped = false;

    @Schema(description = "环境或者环境组ID")
    private String environmentId;
}
