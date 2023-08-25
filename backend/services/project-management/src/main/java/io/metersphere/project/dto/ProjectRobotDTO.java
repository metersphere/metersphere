package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProjectRobotDTO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "所属平台（飞书，钉钉，企业微信，自定义）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String platform;

    @Schema(description = "webhook", requiredMode = Schema.RequiredMode.REQUIRED)
    private String webhook;

    @Schema(description = "钉钉自定义和内部")
    private String type;

    @Schema(description = "钉钉AppKey")
    private String appKey;

    @Schema(description = "钉钉AppSecret")
    private String appSecret;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
}
