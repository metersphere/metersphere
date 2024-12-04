package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiScenarioStepRequest extends ApiScenarioStepCommonDTO<ApiScenarioStepRequest> {
    /**
     * 记录是从哪个步骤复制来的
     * 如果没有传步骤详情
     * 保存时需要根据这个字段查询原步骤详情保存
     */
    @Schema(description = "复制的目标步骤ID")
    private String copyFromStepId;
    /**
     * 记录当前步骤是否是新增的步骤
     */
    @Schema(description = "当前步骤是否是新增的步骤")
    private Boolean isNew;
}
