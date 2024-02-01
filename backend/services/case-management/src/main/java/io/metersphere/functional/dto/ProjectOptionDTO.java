package io.metersphere.functional.dto;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectOptionDTO extends OptionDTO {

    @Schema(description = "是否是默认模板")
    private String projectName;
}
