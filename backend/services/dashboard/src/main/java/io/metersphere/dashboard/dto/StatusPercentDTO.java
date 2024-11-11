package io.metersphere.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusPercentDTO {

    @Schema(description =  "状态")
    private String status;

    @Schema(description =  "数量")
    private Integer count;

    @Schema(description =  "百分比")
    private String percentValue;
}
