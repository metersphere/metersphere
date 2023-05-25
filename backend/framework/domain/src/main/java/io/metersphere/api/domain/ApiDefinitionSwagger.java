package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "定时同步配置")
@Table("api_definition_swagger")
@Data
public class ApiDefinitionSwagger implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition_swagger.id.not_blank}", groups = {Updated.class})
    @Schema(title = "主键", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 500, message = "{api_definition_swagger.swagger_url.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_swagger.swagger_url.not_blank}", groups = {Created.class})
    @Schema(title = "url地址", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 500]")
    private String swaggerUrl;

    @Schema(title = "模块fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String moduleId;

    @Schema(title = "模块路径", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 500]")
    private String modulePath;

    @Schema(title = "鉴权配置信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, ]")
    private byte[] config;

    @Schema(title = "导入模式/覆盖/不覆盖", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 1]")
    private Boolean mode;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String projectId;

    @Schema(title = "导入版本", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String versionId;

}