package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseCustomFieldService {

    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;

    /**
     * 保存 用例-自定义字段关系
     *
     * @param customsFields
     */
    public void saveCustomField(String caseId, List<CaseCustomsFieldDTO> customsFields) {
        customsFields.forEach(customsField -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(customsField.getFieldId());
            customField.setValue(customsField.getValue());
            functionalCaseCustomFieldMapper.insertSelective(customField);
        });
    }
}