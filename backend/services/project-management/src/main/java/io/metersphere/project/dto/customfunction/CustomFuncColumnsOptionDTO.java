package io.metersphere.project.dto.customfunction;

import io.metersphere.plugin.platform.dto.SelectOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFuncColumnsOptionDTO {
    @Schema(description = "用户相关的下拉选项")
    List<SelectOption> userOption;
}
