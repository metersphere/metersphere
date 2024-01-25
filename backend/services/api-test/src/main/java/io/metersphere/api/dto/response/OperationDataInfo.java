package io.metersphere.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationDataInfo {
    @Schema(description = "数据id")
    private String id;
    @Schema(description = "数据编号")
    private Long num;
    @Schema(description = "数据名称")
    private String name;
}
