package io.metersphere.sdk.dto;

import io.metersphere.system.domain.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TemplateDTO extends Template {
    @Schema(description = "相关的自定义字段")
    List<TemplateCustomFieldDTO> customFields;
}
