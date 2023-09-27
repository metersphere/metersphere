package io.metersphere.project.dto;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectRobotDTO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_robot.id.not_blank}", groups = {Updated.class})
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_robot.name.not_blank}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "所属平台（飞书:LARK，钉钉:DING_TALK，企业微信:WE_COM，自定义:CUSTOM, 站内信:IN_SITE, 邮件:MAIL）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String platform;

    @Schema(description = "webhook", requiredMode = Schema.RequiredMode.REQUIRED)
    private String webhook;

    @Schema(description = "钉钉机器人的种类: 自定义:CUSTOM, 企业内部:ENTERPRISE")
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
    @NotBlank(message = "{project_robot.project_id.not_blank}", groups = {Created.class, Updated.class})
    private String projectId;
}
