package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.debug.ApiDebugRunRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ApiDefinitionRunRequest extends ApiDebugRunRequest {
    @Schema(description = "环境ID")
    private String environmentId;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    @NotBlank
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    @NotBlank
    private String path;

    @Schema(description = "模块fk")
    private String moduleId;
}
