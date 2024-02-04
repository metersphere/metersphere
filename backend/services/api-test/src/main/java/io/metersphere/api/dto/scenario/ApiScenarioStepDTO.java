package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioStepDTO extends ApiScenarioStepCommonDTO {

    @Schema(description = "步骤名称")
    private String name;

    @Schema(description = "资源编号")
    private String resourceNum;

    @Schema(description = "版本号")
    private String versionId;

    @Schema(description = "场景id")
    private String scenarioId;

    @Schema(description = "序号")
    private Long sort;

    @Schema(description = "父级fk")
    private String parentId;
}
