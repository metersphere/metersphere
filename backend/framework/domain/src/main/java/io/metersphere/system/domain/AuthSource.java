package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class AuthSource implements Serializable {
    @Schema(title = "认证源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{auth_source.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{auth_source.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "状态 启用 禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{auth_source.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{auth_source.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "名称")
    private String name;

    @Schema(title = "类型")
    private String type;

    @Schema(title = "认证源配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{auth_source.configuration.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 65535, message = "{auth_source.configuration.length_range}", groups = {Created.class, Updated.class})
    private byte[] configuration;

    private static final long serialVersionUID = 1L;
}