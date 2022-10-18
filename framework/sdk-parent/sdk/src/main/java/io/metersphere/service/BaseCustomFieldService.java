package io.metersphere.service;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldExample;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.ext.BaseCustomFieldMapper;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.CustomFieldResourceDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseCustomFieldService {

    @Resource
    BaseCustomFieldMapper baseCustomFieldMapper;

    @Resource
    CustomFieldMapper customFieldMapper;

    public CustomField get(String id) {
        return customFieldMapper.selectByPrimaryKey(id);
    }

    public List<CustomField> getFieldByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            CustomFieldExample example = new CustomFieldExample();
            example.createCriteria()
                    .andIdIn(ids);
            return customFieldMapper.selectByExampleWithBLOBs(example);
        }
        return new ArrayList<>();
    }

    public static List<CustomFieldItemDTO> getCustomFields(String customFieldsStr) {
        if (StringUtils.isNotBlank(customFieldsStr)) {
            List<CustomFieldItemDTO> customFieldItems = JSON.parseArray(customFieldsStr, CustomFieldItemDTO.class);
            if (CollectionUtils.isNotEmpty(customFieldItems)) {
                return customFieldItems;
            }
        }
        return new ArrayList<>();
    }

    public List<CustomField> getWorkspaceSystemFields(String scene, String workspaceId) {
        return baseCustomFieldMapper.getWorkspaceSystemFields(scene, workspaceId);
    }

    public List<CustomFieldResourceDTO> getCustomFieldResourceDTO(String customFieldsStr) {
        List<CustomFieldResourceDTO> list = new ArrayList<>();
        if (StringUtils.isNotBlank(customFieldsStr)) {
            List<CustomFieldItemDTO> items = getCustomFields(customFieldsStr);
            for (CustomFieldItemDTO item : items) {
                list.add(constructorCustomFieldResourceDTO(item));
            }
            return list;
        }
        return new ArrayList<>();
    }

    private CustomFieldResourceDTO constructorCustomFieldResourceDTO(CustomFieldItemDTO dto) {
        CustomFieldResourceDTO resource = new CustomFieldResourceDTO();
        resource.setFieldId(dto.getId());
        if (StringUtils.isNotBlank(dto.getType()) &&
                StringUtils.equalsAny(dto.getType(), CustomFieldType.RICH_TEXT.getValue(), CustomFieldType.TEXTAREA.getValue())) {
            resource.setTextValue(dto.getValue() == null ? StringUtils.EMPTY : dto.getValue().toString());
        } else {
            resource.setValue(JSON.toJSONString(dto.getValue()));
        }
        return resource;
    }

    public List<CustomField> getByProjectId(String projectId) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return customFieldMapper.selectByExample(example);
    }

    public Map<String, CustomField> getNameMapByProjectId(String projectId) {
        return this.getByProjectId(projectId)
                .stream()
                .collect(Collectors.toMap(i -> i.getName() + i.getScene(), i -> i, (val1, val2) -> val1));
    }

    public Map<String, CustomField> getGlobalNameMapByProjectId() {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andGlobalEqualTo(true);
        return customFieldMapper.selectByExample(example)
                .stream()
                .collect(Collectors.toMap(i -> i.getName() + i.getScene(), i -> i));
    }

    public CustomField getCustomFieldByName(String projectId, String fieldName) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andNameEqualTo(fieldName);
        List<CustomField> customFields = customFieldMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(customFields)) {
            return customFields.get(0);
        } else {
            example.clear();
            example.createCriteria()
                    .andGlobalEqualTo(true)
                    .andNameEqualTo(fieldName);
            customFields = customFieldMapper.selectByExample(example);
            return CollectionUtils.isNotEmpty(customFields) ? customFields.get(0) : null;
        }
    }
}
