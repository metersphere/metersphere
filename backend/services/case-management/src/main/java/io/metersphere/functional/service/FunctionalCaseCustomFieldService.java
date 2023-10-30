package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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


    /**
     * 获取用例保存的自定义字段值
     *
     * @param fieldId
     * @param caseId
     * @return
     */
    public FunctionalCaseCustomField getCustomField(String fieldId, String caseId) {
        FunctionalCaseCustomFieldExample example = new FunctionalCaseCustomFieldExample();
        example.createCriteria().andCaseIdEqualTo(caseId).andFieldIdEqualTo(fieldId);
        List<FunctionalCaseCustomField> functionalCaseCustomFields = functionalCaseCustomFieldMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(functionalCaseCustomFields)) {
            return functionalCaseCustomFields.get(0);
        }
        return null;
    }


    /**
     * 更新自定义字段
     *
     * @param caseId
     * @param customsFields
     */
    public void updateCustomField(String caseId, List<CaseCustomsFieldDTO> customsFields) {
        List<String> fieldIds = customsFields.stream().map(CaseCustomsFieldDTO::getFieldId).collect(Collectors.toList());
        FunctionalCaseCustomFieldExample example = new FunctionalCaseCustomFieldExample();
        example.createCriteria().andFieldIdIn(fieldIds).andCaseIdEqualTo(caseId);
        List<FunctionalCaseCustomField> defaultFields = functionalCaseCustomFieldMapper.selectByExample(example);
        Map<String, FunctionalCaseCustomField> collect = defaultFields.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getFieldId, (item) -> item));
        List<CaseCustomsFieldDTO> addFields = new ArrayList<>();
        List<CaseCustomsFieldDTO> updateFields = new ArrayList<>();
        customsFields.forEach(customsField -> {
            if (collect.containsKey(customsField.getFieldId())) {
                updateFields.add(customsField);
            } else {
                addFields.add(customsField);
            }
        });
        if (CollectionUtils.isNotEmpty(addFields)) {
            saveCustomField(caseId, addFields);
        }
        ;
        if (CollectionUtils.isNotEmpty(updateFields)) {
            updateField(caseId, updateFields);
        }
    }

    private void updateField(String caseId, List<CaseCustomsFieldDTO> updateFields) {
        updateFields.forEach(customsField -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(customsField.getFieldId());
            customField.setValue(customsField.getValue());
            functionalCaseCustomFieldMapper.updateByPrimaryKeySelective(customField);
        });
    }
}