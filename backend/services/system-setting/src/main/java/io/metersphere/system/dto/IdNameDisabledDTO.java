package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IdNameDisabledDTO extends IdNameStructureDTO{
    @Schema(description =  "是否已经关联过")
    private Boolean disabled = false;
}
