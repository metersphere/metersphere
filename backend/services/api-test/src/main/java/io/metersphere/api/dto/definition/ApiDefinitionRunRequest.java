package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.debug.ApiDebugRunRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ApiDefinitionRunRequest extends ApiDebugRunRequest {
    @Schema(description = "环境ID")
    private String environmentId;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    @Schema(description = "模块fk")
    private String moduleId;

    @Schema(description = "接口编号  mock执行需要")
    private Long num;
}
