package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseTemplateWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class TestCaseTemplateDao extends TestCaseTemplateWithBLOBs {
    List<CustomFieldDao> customFields;
}
