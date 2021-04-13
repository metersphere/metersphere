package io.metersphere.controller.request;

import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.domain.TestCaseTemplateWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCaseFieldTemplateRequest extends TestCaseTemplateWithBLOBs {
    List<CustomFieldTemplate> customFields;
}
