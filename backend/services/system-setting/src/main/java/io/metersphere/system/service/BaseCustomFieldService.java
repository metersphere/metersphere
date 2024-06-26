package io.metersphere.system.service;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.constants.TemplateRequiredCustomField;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.domain.TemplateCustomFieldExample;
import io.metersphere.system.dto.request.DefaultBugCustomField;
import io.metersphere.system.dto.request.DefaultFunctionalCustomField;
import io.metersphere.system.dto.sdk.CustomFieldDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldOptionRequest;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.ExtTemplateCustomFieldMapper;
import io.metersphere.system.mapper.TemplateCustomFieldMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    @Resource
    protected TemplateCustomFieldMapper templateCustomFieldMapper;
    @Resource
    private ExtTemplateCustomFieldMapper extTemplateCustomFieldMapper;

    private static final String CREATE_USER = "CREATE_USER";

    public List<CustomFieldDTO> list(String scopeId, String scene) {
        checkScene(scene);
        List<CustomField> customFields = getByScopeIdAndScene(scopeId, scene);
        List<String> userIds = customFields.stream().map(CustomField::getCreateUser).toList();
        List<String> usedFieldIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customFields)) {
            usedFieldIds.addAll(extTemplateCustomFieldMapper.selectUsedFieldIds(customFields.stream().map(CustomField::getId).toList()));
        }

        Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);
        List<CustomFieldOption> customFieldOptions = baseCustomFieldOptionService.getByFieldIds(customFields.stream().map(CustomField::getId).toList());
        Map<String, List<CustomFieldOption>> optionMap = customFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        return customFields.stream().map(item -> {
            item.setCreateUser(userNameMap.get(item.getCreateUser()));
            CustomFieldDTO customFieldDTO = new CustomFieldDTO();
            BeanUtils.copyBean(customFieldDTO, item);
            //判断有没有用到
            if (usedFieldIds.contains(item.getId())) {
                customFieldDTO.setUsed(true);
            }
            customFieldDTO.setOptions(optionMap.get(item.getId()));
            if (CustomFieldType.getHasOptionValueSet().contains(customFieldDTO.getType()) && customFieldDTO.getOptions() == null) {
                customFieldDTO.setOptions(List.of());
            }
            if (StringUtils.equalsAny(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                // 成员选项添加默认的选项
                CustomFieldOption createUserOption = new CustomFieldOption();
                createUserOption.setFieldId(item.getId());
                createUserOption.setText(Translator.get("message.domain.createUser"));
                createUserOption.setValue(CREATE_USER);
                createUserOption.setInternal(false);
                customFieldDTO.setOptions(List.of(createUserOption));
            }
            if (BooleanUtils.isTrue(item.getInternal())) {
                // 设置哪些内置字段是模板里必选的
                Set<String> templateRequiredCustomFieldSet = Arrays.stream(TemplateRequiredCustomField.values())
                        .map(TemplateRequiredCustomField::getName)
                        .collect(Collectors.toSet());
                customFieldDTO.setTemplateRequired(templateRequiredCustomFieldSet.contains(item.getName()));
                customFieldDTO.setInternalFieldKey(item.getName());
                // 翻译内置字段名称
                customFieldDTO.setName(translateInternalField(item.getName()));
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
            customFieldDTO.setInternalFieldKey(customField.getName());
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
        customField.setId(IDGenerator.nextStr());
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customField.setEnableOptionKey(BooleanUtils.isTrue(customField.getEnableOptionKey()));
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
        customField.setInternal(null);
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
        deleteTemplateCustomField(id);
    }

    public void deleteTemplateCustomField(String id) {
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria().andFieldIdEqualTo(id);
        templateCustomFieldMapper.deleteByExample(example);
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
                .andSceneEqualTo(customField.getScene())
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
                .andSceneEqualTo(customField.getScene())
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
        customField.setId(IDGenerator.nextStr());
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

    /**
     * 初始化缺陷模板默认字段
     * @param scopeType 模板范围
     * @param scopeId 范围ID
     * @return
     */
    public List<CustomField> initBugDefaultCustomField(TemplateScopeType scopeType, String scopeId) {
        List<CustomField> customFields = new ArrayList<>();
        for (DefaultBugCustomField defaultBugCustomField : DefaultBugCustomField.values()) {
            CustomField customField = new CustomField();
            customField.setName(defaultBugCustomField.getName());
            customField.setScene(TemplateScene.BUG.name());
            customField.setType(defaultBugCustomField.getType().name());
            customField.setScopeType(scopeType.name());
            customField.setScopeId(scopeId);
            customField.setEnableOptionKey(false);
            customFields.add(this.initDefaultCustomField(customField));
            // 初始化选项
            baseCustomFieldOptionService.addByFieldId(customField.getId(), defaultBugCustomField.getOptions());
        }
        return customFields;
    }
}
