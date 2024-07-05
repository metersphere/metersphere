package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseCustomFieldDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Resource
    private ExtFunctionalCaseCustomFieldMapper extFunctionalCaseCustomFieldMapper;

    /**
     * 保存 用例-自定义字段关系
     *
     * @param customFields
     */
    public void saveCustomField(String caseId, List<CaseCustomFieldDTO> customFields) {
        customFields.forEach(custom -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(custom.getFieldId());
            customField.setValue(custom.getValue());
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
            return functionalCaseCustomFields.getFirst();
        }
        return null;
    }


    /**
     * 更新自定义字段
     *
     * @param caseId
     * @param customFields
     */
    public void updateCustomField(String caseId, List<CaseCustomFieldDTO> customFields) {
        List<String> fieldIds = customFields.stream().map(CaseCustomFieldDTO::getFieldId).collect(Collectors.toList());
        FunctionalCaseCustomFieldExample example = new FunctionalCaseCustomFieldExample();
        example.createCriteria().andFieldIdIn(fieldIds).andCaseIdEqualTo(caseId);
        List<FunctionalCaseCustomField> defaultFields = functionalCaseCustomFieldMapper.selectByExample(example);
        Map<String, FunctionalCaseCustomField> collect = defaultFields.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getFieldId, (item) -> item));
        List<CaseCustomFieldDTO> addFields = new ArrayList<>();
        List<CaseCustomFieldDTO> updateFields = new ArrayList<>();
        customFields.forEach(customField -> {
            if (collect.containsKey(customField.getFieldId())) {
                updateFields.add(customField);
            } else {
                addFields.add(customField);
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

    private void updateField(String caseId, List<CaseCustomFieldDTO> updateFields) {
        updateFields.forEach(custom -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(custom.getFieldId());
            customField.setValue(custom.getValue());
            functionalCaseCustomFieldMapper.updateByPrimaryKeySelective(customField);
        });
    }

    public List<FunctionalCaseCustomField> getCustomFieldByCaseIds(List<String> ids) {
        return extFunctionalCaseCustomFieldMapper.getCustomFieldByCaseIds(ids);
    }

    public Map<String, List<FunctionalCaseCustomField>> getCustomFieldMapByCaseIds(List<String> ids) {
        List<FunctionalCaseCustomField> customFieldList = getCustomFieldByCaseIds(ids);
        Map<String, List<FunctionalCaseCustomField>> caseCustomFieldMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(customFieldList)) {
            caseCustomFieldMap = customFieldList.stream().collect(Collectors.groupingBy(FunctionalCaseCustomField::getCaseId));
        }
        return caseCustomFieldMap;
    }

    public void batchSaveCustomField(List<FunctionalCaseCustomField> customFields) {
        functionalCaseCustomFieldMapper.batchInsert(customFields);
    }

    /**
     * 批量更新自定义字段
     *
     * @param customField
     * @param ids
     */
    public void batchUpdate(CaseCustomFieldDTO customField, List<String> ids) {
        FunctionalCaseCustomField functionalCaseCustomField = new FunctionalCaseCustomField();
        functionalCaseCustomField.setFieldId(customField.getFieldId());
        functionalCaseCustomField.setValue(customField.getValue());
        extFunctionalCaseCustomFieldMapper.batchDelete(functionalCaseCustomField, ids);
        List<FunctionalCaseCustomField> list = new ArrayList<>();
        ids.forEach(id -> {
            FunctionalCaseCustomField field = new FunctionalCaseCustomField();
            field.setCaseId(id);
            field.setFieldId(customField.getFieldId());
            field.setValue(customField.getValue());
            list.add(field);
        });
        functionalCaseCustomFieldMapper.batchInsert(list);
    }

    public List<FunctionalCaseCustomFieldDTO> getCustomFieldsByCaseIds(List<String> ids) {
        return extFunctionalCaseCustomFieldMapper.getCustomFieldsByCaseIds(ids);
    }
}