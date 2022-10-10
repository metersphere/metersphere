package io.metersphere.dto;

import io.metersphere.base.domain.IssueTemplate;
import lombok.Data;

import java.util.List;

@Data
public class IssueTemplateDao extends IssueTemplate {
    List<CustomFieldDao> customFields;
}
