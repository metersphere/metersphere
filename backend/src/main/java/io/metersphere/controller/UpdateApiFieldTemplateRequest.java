package io.metersphere.controller;

import io.metersphere.base.domain.ApiTemplate;
import io.metersphere.base.domain.CustomFieldTemplate;
import lombok.Data;

import java.util.List;

@Data
public class UpdateApiFieldTemplateRequest extends ApiTemplate {
    List<CustomFieldTemplate> customFields;
}
