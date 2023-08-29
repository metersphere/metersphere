package io.metersphere.sdk.dto;

import io.metersphere.system.domain.Template;
import lombok.Data;

import java.util.List;

@Data
public class TemplateDTO extends Template {
    List<TemplateCustomFieldDTO> customFields;
}
