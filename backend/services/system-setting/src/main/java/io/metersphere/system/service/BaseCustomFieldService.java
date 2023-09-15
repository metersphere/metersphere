package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.dto.CustomFieldDTO;
import io.metersphere.sdk.dto.request.CustomFieldOptionRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.utils.ServiceUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.mapper.CustomFieldMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import io.metersphere.system.uid.UUID;
import static io.metersphere.system.controller.handler.result.CommonResultCode.*;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseCustomFieldService {
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseCustomFieldOptionService baseCustomFieldOptionService;

    public List<CustomField> list(String scopeId, String scene) {
        checkScene(scene);
        List<CustomField> customFields = getByScopeIdAndScene(scopeId, scene);
        List<String> userIds = customFields.stream().map(CustomField::getCreateUser).toList();
        Map<String, String> userNameMap = baseUserService.getUserNameMap(userIds);
        customFields.forEach(item -> {
            item.setCreateUser(userNameMap.get(item.getCreateUser()));
            if (item.getInternal()) {
                item.setName(translateInternalField(item.getName()));
            }
        });
        return customFields;
    }

    /**
     * 翻译内置字段
     * @param filedName
     * @return
     */
    public String translateInternalField(String filedName) {
        return Translator.get("custom_field." + filedName);
    }

    public List<CustomField> getByScopeIdAndScene(String scopeId, String scene) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andScopeIdEqualTo(scopeId)
                .andSceneEqualTo(scene);
        return customFieldMapper.selectByExample(example);
    }

    public CustomField getWithCheck(String id) {
        checkResourceExist(id);
        return customFieldMapper.selectByPrimaryKey(id);
    }

    public CustomFieldDTO getCustomFieldDTOWithCheck(String id) {
        checkResourceExist(id);
        CustomField customField = customFieldMapper.selectByPrimaryKey(id);
        CustomFieldDTO customFieldDTO = new CustomFieldDTO();
        BeanUtils.copyBean(customFieldDTO, customField);
        customFieldDTO.setOptions(baseCustomFieldOptionService.getByFieldId(customFieldDTO.getId()));
        if (customField.getInternal()) {
            customField.setName(translateInternalField(customField.getName()));
        }
        return customFieldDTO;
    }

    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkAddExist(customField);
        customField.setId(UUID.randomUUID().toString());
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customField.setInternal(false);
        customFieldMapper.insert(customField);
        baseCustomFieldOptionService.addByFieldId(customField.getId(), options);
        return customField;
    }

    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkUpdateExist(customField);
        checkResourceExist(customField.getId());
        customField.setScopeId(null);
        customField.setScene(null);
        customField.setScopeType(null);
        customField.setInternal(false);
        customField.setCreateUser(null);
        customField.setCreateTime(null);
        customField.setUpdateTime(System.currentTimeMillis());
        customFieldMapper.updateByPrimaryKeySelective(customField);
        if (options != null) {
            baseCustomFieldOptionService.updateByFieldId(customField.getId(), options);
        }
        return customField;
    }

    public CustomField checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(customFieldMapper.selectByPrimaryKey(id), "permission.organization_custom_field.name");
    }

    public void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new MSException(TEMPLATE_SCENE_ILLEGAL));
    }

    public void delete(String id) {
        customFieldMapper.deleteByPrimaryKey(id);
        baseCustomFieldOptionService.deleteByFieldId(id);
    }

    protected void checkInternal(CustomField customField) {
        if (customField.getInternal()) {
            throw new MSException(INTERNAL_CUSTOM_FIELD_PERMISSION);
        }
    }

    protected void checkAddExist(CustomField customField) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andScopeIdEqualTo(customField.getScopeId())
                .andNameEqualTo(customField.getName());
        if (CollectionUtils.isNotEmpty(customFieldMapper.selectByExample(example))) {
            throw new MSException(CUSTOM_FIELD_EXIST);
        }
    }

    protected void checkUpdateExist(CustomField customField) {
        if (StringUtils.isBlank(customField.getName())) {
            return;
        }
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andScopeIdEqualTo(customField.getScopeId())
                .andIdNotEqualTo(customField.getId())
                .andNameEqualTo(customField.getName());
        if (CollectionUtils.isNotEmpty(customFieldMapper.selectByExample(example))) {
            throw new MSException(CUSTOM_FIELD_EXIST);
        }
    }

    public List<CustomField> getByIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return new ArrayList<>(0);
        }
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andIdIn(fieldIds);
        return customFieldMapper.selectByExample(example);
    }
}
