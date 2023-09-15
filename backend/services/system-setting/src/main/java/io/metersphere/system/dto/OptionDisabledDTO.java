package io.metersphere.system.dto;

import io.metersphere.sdk.dto.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionDisabledDTO extends OptionDTO {
    @Schema(description =  "是否已经关联过")
    private Boolean disabled = false;
}
