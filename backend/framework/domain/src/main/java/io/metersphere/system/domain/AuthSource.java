package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class AuthSource implements Serializable {
    @Schema(title = "认证源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{auth_source.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{auth_source.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{auth_source.enable.not_blank}", groups = {Created.class})
    private Boolean enable;

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
    @NotNull(message = "{auth_source.configuration.not_blank}", groups = {Created.class})
    private byte[] configuration;

    private static final long serialVersionUID = 1L;
}