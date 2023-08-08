package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuthSourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "认证源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;


    @Schema(title = "描述")
    private String description;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(title = "类型")
    private String type;

    @Schema(title = "认证源配置", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configuration;

    @Schema(title = "是否启用")
    private Boolean enable;
}
