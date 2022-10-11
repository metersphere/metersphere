package io.metersphere.xpack.track.dto;

import io.metersphere.base.domain.IssueTemplate;
import io.metersphere.dto.CustomFieldDao;
import lombok.Data;

import java.util.List;

@Data
public class IssueTemplateDao extends IssueTemplate {
    List<CustomFieldDao> customFields;
}
