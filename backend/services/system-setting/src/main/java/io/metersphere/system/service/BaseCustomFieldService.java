package io.metersphere.system.service;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.constants.DefaultFunctionalCustomField;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.CustomFieldDTO;
import io.metersphere.sdk.dto.request.CustomFieldOptionRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.uid.UUID;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.handler.result.CommonResultCode.*;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseCustomFieldService {
    @Resource
    protected CustomFieldMapper customFieldMapper;
    @Resource
    protected UserLoginService userLoginService;
    @Resource
    protected BaseCustomFieldOptionService baseCustomFieldOptionService;
    @Resource
    protected BaseOrganizationParameterService baseOrganizationParameterService;

    public List<CustomFieldDTO> list(String scopeId, String scene) {
        checkScene(scene);
        List<CustomField> customFields = getByScopeIdAndScene(scopeId, scene);
        List<String> userIds = customFields.stream().map(CustomField::getCreateUser).toList();
        Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);
        List<CustomFieldOption> customFieldOptions = baseCustomFieldOptionService.getByFieldIds(customFields.stream().map(CustomField::getId).toList());
        Map<String, List<CustomFieldOption>> optionMap = customFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        return customFields.stream().map(item -> {
            item.setCreateUser(userNameMap.get(item.getCreateUser()));
            if (item.getInternal()) {
                item.setName(translateInternalField(item.getName()));
            }
            CustomFieldDTO customFieldDTO = new CustomFieldDTO();
            BeanUtils.copyBean(customFieldDTO, item);
            customFieldDTO.setOptions(optionMap.get(item.getId()));
            if (CustomFieldType.getHasOptionValueSet().contains(customFieldDTO.getType()) && customFieldDTO.getOptions() == null) {
                customFieldDTO.setOptions(List.of());
            }
            return customFieldDTO;
        }).toList();
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
        customField.setInternal(false);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        return this.baseAdd(customField, customFieldOptions);
    }

    public List<CustomFieldOption> parseCustomFieldOptionRequest2Option(List<CustomFieldOptionRequest> options) {
        return options == null ? null : options.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyBean(customFieldOption, item);
            return customFieldOption;
        }).toList();
    }

    /**
     * 添加自定义字段，不设置是否是内置字段
     * @param customField
     * @param options
     * @return
     */
    public CustomField baseAdd(CustomField customField, List<CustomFieldOption> options) {
        checkAddExist(customField);
        customField.setId(UUID.randomUUID().toString());
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
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
        customField.setType(null);
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

    public List<CustomField> getByRefIdsAndScopeId(List<String> fieldIds, String scopeId) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return new ArrayList<>(0);
        }
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andRefIdIn(fieldIds)
                .andScopeIdEqualTo(scopeId);
        return customFieldMapper.selectByExample(example);
    }

    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        String key = baseOrganizationParameterService.getOrgTemplateEnableKeyByScene(scene);
        String value = baseOrganizationParameterService.getValue(orgId, key);
        // 没有配置默认为 true
        return !StringUtils.equals(BooleanUtils.toStringTrueFalse(false), value);
    }

    public void deleteByScopeId(String copeId) {
        // 删除字段
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andScopeIdEqualTo(copeId);
        customFieldMapper.deleteByExample(example);

        // 删除字段选项
        List<String> ids = customFieldMapper.selectByExample(example)
                .stream()
                .map(CustomField::getId)
                .toList();
        baseCustomFieldOptionService.deleteByFieldIds(ids);
    }

    public List<CustomField> getByScopeId(String scopeId) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andScopeIdEqualTo(scopeId);
        return customFieldMapper.selectByExample(example);
    }

    public CustomField initDefaultCustomField(CustomField customField) {
        customField.setId(UUID.randomUUID().toString());
        customField.setInternal(true);
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customField.setCreateUser("admin");
        customFieldMapper.insert(customField);
        return customField;
    }

    /**
     * 初始化功能用例模板
     * @param scopeType
     * @param scopeId
     * @return
     */
    public List<CustomField> initFunctionalDefaultCustomField(TemplateScopeType scopeType, String scopeId) {
        List<CustomField> customFields = new ArrayList<>();
        for (DefaultFunctionalCustomField defaultFunctionalCustomField : DefaultFunctionalCustomField.values()) {
            CustomField customField = new CustomField();
            customField.setName(defaultFunctionalCustomField.getName());
            customField.setScene(TemplateScene.FUNCTIONAL.name());
            customField.setType(defaultFunctionalCustomField.getType().name());
            customField.setScopeType(scopeType.name());
            customField.setScopeId(scopeId);
            customField.setEnableOptionKey(false);
            customFields.add(this.initDefaultCustomField(customField));
            // 初始化选项
            baseCustomFieldOptionService.addByFieldId(customField.getId(), defaultFunctionalCustomField.getOptions());
        }
       return customFields;
    }
}
