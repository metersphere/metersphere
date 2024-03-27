package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioStepDTO extends ApiScenarioStepCommonDTO<ApiScenarioStepDTO> {

    @Schema(description = "场景id")
    private String scenarioId;

    @Schema(description = "序号")
    private Long sort;

    @Schema(description = "父级fk")
    private String parentId;
}
