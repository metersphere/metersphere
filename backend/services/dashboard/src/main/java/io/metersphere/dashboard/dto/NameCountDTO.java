package io.metersphere.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NameCountDTO {

    @Schema(description =  "名称")
    private String name;

    @Schema(description =  "数量")
    private Object count;
}
