package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiDefinitionSwagger implements Serializable {
    @Schema(title = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_swagger.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "url地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_swagger.swagger_url.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 500, message = "{api_definition_swagger.swagger_url.length_range}", groups = {Created.class, Updated.class})
    private String swaggerUrl;

    @Schema(title = "模块fk")
    private String moduleId;

    @Schema(title = "模块路径")
    private String modulePath;

    @Schema(title = "导入模式/覆盖/不覆盖")
    private Boolean mode;

    @Schema(title = "项目fk")
    private String projectId;

    @Schema(title = "导入版本")
    private String versionId;

    @Schema(title = "鉴权配置信息")
    private byte[] config;

    private static final long serialVersionUID = 1L;
}