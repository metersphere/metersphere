package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdNameDisabledDTO extends IdNameStructureDTO{
    @Schema(description =  "是否已经关联过")
    private Boolean disabled = false;
}
