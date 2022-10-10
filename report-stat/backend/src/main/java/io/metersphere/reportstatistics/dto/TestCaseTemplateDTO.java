package io.metersphere.reportstatistics.dto;

import io.metersphere.base.domain.TestCaseTemplateWithBLOBs;
import io.metersphere.dto.CustomFieldDao;
import lombok.Data;

import java.util.List;

@Data
public class TestCaseTemplateDTO extends TestCaseTemplateWithBLOBs {
    List<CustomFieldDao> customFields;
}
