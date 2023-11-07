package io.metersphere.api.dto.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiDebugSimpleDTO {
    @Schema(description = "接口ID")
    private String id;

    @Schema(description = "接口名称")
    private String name;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "模块fk")
    private String moduleId;
}
