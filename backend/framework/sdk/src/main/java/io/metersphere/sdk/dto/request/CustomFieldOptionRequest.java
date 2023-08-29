package io.metersphere.sdk.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomFieldOptionRequest {
    @Schema(title = "选项值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_option.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(title = "选项值名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.text.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{custom_field_option.text.length_range}", groups = {Created.class, Updated.class})
    private String text;
}
