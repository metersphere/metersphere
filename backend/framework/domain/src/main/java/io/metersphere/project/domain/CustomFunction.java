package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class CustomFunction implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_function.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{custom_function.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "函数名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_function.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{custom_function.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "函数描述")
    private String description;

    private static final long serialVersionUID = 1L;
}