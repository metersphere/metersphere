package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiModuleDTO {
    @Schema(description = "模块ID")
    private String id;
    @Schema(description = "id的状态 None:手动选  Current:选中当前 All:全选中")
    private String status;
    @Schema(description = "是否禁用")
    private Boolean disabled = false;
}
