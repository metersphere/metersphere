package io.metersphere.request;

import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.domain.IssueTemplate;
import lombok.Data;

import java.util.List;

@Data
public class UpdateIssueTemplateRequest extends IssueTemplate {
    List<CustomFieldTemplate> customFields;
}
