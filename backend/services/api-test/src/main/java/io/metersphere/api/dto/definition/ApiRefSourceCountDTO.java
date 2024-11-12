package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiRefSourceCountDTO {

    @Schema(description = "被引用的资源ID")
    private String sourceId;

    @Schema(description = "引用者数量")
    private int count;
}
