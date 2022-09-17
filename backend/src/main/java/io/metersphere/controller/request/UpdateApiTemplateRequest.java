package io.metersphere.controller.request;

import io.metersphere.base.domain.ApiTemplate;
import io.metersphere.base.domain.CustomFieldTemplate;
import lombok.Data;

import java.util.List;

@Data
public class UpdateApiTemplateRequest extends ApiTemplate {
    List<CustomFieldTemplate> customFields;
}
