package io.metersphere.system.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuthSourceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description =  "认证源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 50, message = "{auth_source.id.length_range}", groups = {Created.class, Updated.class})
    private String id;


    @Schema(description =  "描述")
    private String description;

    @Schema(description =  "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{authsource_name_is_null}")
    private String name;

    @Schema(description =  "类型")
    @NotBlank(message = "{authsource_type_is_null}")
    private String type;

    @Schema(description =  "认证源配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{authsource_configuration_is_null}", groups = {Created.class})
    private String configuration;
}
