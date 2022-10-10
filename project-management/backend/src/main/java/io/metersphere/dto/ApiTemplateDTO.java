package io.metersphere.dto;

import io.metersphere.base.domain.ApiTemplate;
import lombok.Data;

import java.util.List;

@Data
public class ApiTemplateDTO extends ApiTemplate {
    List<CustomFieldDao> customFields;
}
