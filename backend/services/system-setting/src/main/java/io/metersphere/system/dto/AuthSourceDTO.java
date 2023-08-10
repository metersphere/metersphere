package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuthSourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description =  "认证源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;


    @Schema(description =  "描述")
    private String description;

    @Schema(description =  "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description =  "类型")
    private String type;

    @Schema(description =  "认证源配置", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configuration;

    @Schema(description =  "是否启用")
    private Boolean enable;
}
