package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class EnvironmentGroup implements Serializable {
    @Schema(description =  "环境组id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment_group.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "环境组名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{environment_group.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "所属组织", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment_group.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{environment_group.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description =  "环境组描述")
    private String description;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}