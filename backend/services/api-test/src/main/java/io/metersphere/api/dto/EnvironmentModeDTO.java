package io.metersphere.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 选择的环境类型
 * @Author: jianxing
 * @CreateTime: 2024-01-29  15:30
 */
@Data
public class EnvironmentModeDTO {
    @Schema(description = "是否为环境组")
    private Boolean grouped;

    @Schema(description = "环境或者环境组ID")
    private String environmentId;
}
