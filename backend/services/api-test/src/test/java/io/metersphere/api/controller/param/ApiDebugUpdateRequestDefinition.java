package io.metersphere.api.controller.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  14:33
 */
@Getter
@Setter
public class ApiDebugUpdateRequestDefinition {

    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.id.length_range}")
    private String id;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.name.not_blank}")
    @Size(min = 1, max = 255, message = "{api_debug.name.length_range}")
    private String name;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.module_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.module_id.length_range}")
    private String moduleId;

    @Schema(description = "请求内容")
    @NotBlank
    private String request;
}
