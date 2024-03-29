package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioDetailDTO extends ApiScenarioDetail {
    @Schema(description = "创建人名称")
    private String createUserName;
    @Schema(description = "更新人名称")
    private String updateUserName;
    @Schema(description = "当前用户是否关注")
    private Boolean follow = false;
}
