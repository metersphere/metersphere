package io.metersphere.bug.dto.response;

import io.metersphere.plugin.platform.dto.SelectOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BugColumnsOptionDTO {
    @Schema(description = "用户相关的下拉选项")
    List<SelectOption> userOption;
    @Schema(description = "处理人下拉选项")
    List<SelectOption> handleUserOption;
    @Schema(description = "状态下拉选项")
    List<SelectOption> statusOption;
}
