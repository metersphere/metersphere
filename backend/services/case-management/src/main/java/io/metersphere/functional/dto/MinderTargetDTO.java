package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author guoyuqi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinderTargetDTO {

    @Schema(description = "移动排序的目标id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetId;

    @Schema(description = "移动方式(BEFORE AFTER)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveMode;
}
