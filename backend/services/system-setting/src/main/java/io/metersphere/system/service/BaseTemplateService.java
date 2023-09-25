package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.TemplateCustomFieldDTO;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.utils.ServiceUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateCustomField;
import io.metersphere.system.domain.TemplateExample;
import io.metersphere.system.mapper.TemplateMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import io.metersphere.system.uid.UUID;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.handler.result.CommonResultCode.*;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTemplateService {

    @Resource
    protected TemplateMapper templateMapper;
    @Resource
    protected BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    @Resource
    protected BaseUserService baseUserService;
    @Resource
    protected BaseCustomFieldService baseCustomFieldService;

    public List<Template> list(String scopeId, String scene) {
        checkScene(scene);
        List<Template> templates = getTemplates(scopeId, scene);
        List<String> userIds = templates.stream().map(Template::getCreateUser).toList();
        Map<String, String> userNameMap = baseUserService.getUserNameMap(userIds);
        templates.forEach(item -> {
            item.setCreateUser(userNameMap.get(item.getCreateUser()));
            if (item.getInternal()) {
                item.setName(translateInternalTemplate(item.getName()));
            }
        });
        return templates;
    }

    public List<Template> getTemplates(String scopeId, String scene) {
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeIdEqualTo(scopeId)
                .andSceneEqualTo(scene);
        return templateMapper.selectByExample(example);
    }

    public List<Template> getByScopeId(String scopeId) {
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeIdEqualTo(scopeId);
        return templateMapper.selectByExample(example);
    }

    public String translateInternalTemplate(String filedName) {
        return Translator.get("template." + filedName);
    }

    public Template getWithCheck(String id) {
        return checkResourceExist(id);
    }


    public TemplateDTO geDTOWithCheck(Template template) {
        List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());

        // 查找字段名称
        List<String> fieldIds = templateCustomFields.stream().map(TemplateCustomField::getFieldId).toList();
        Map<String, String> fieldNameMap = baseCustomFieldService.getByIds(fieldIds)
                .stream()
                .collect(Collectors.toMap(CustomField::getId, i -> {
                    if (i.getInternal()) {
                        return baseCustomFieldService.translateInternalField(i.getName());
                    }
                    return i.getName();
                }));

        // 封装字段信息
        List<TemplateCustomFieldDTO> fieldDTOS = templateCustomFields.stream().map(i -> {
            TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
            BeanUtils.copyBean(templateCustomFieldDTO, i);
            templateCustomFieldDTO.setFieldName(fieldNameMap.get(i.getFieldId()));
            return templateCustomFieldDTO;
        }).toList();

        TemplateDTO templateDTO = BeanUtils.copyBean(new TemplateDTO(), template);
        templateDTO.setCustomFields(fieldDTOS);
        return templateDTO;
    }

    public Template add(Template template, List<TemplateCustomFieldRequest> customFields) {
        template.setInternal(false);
        template.setEnableDefault(false);
        return this.baseAdd(template, customFields);
    }

    public Template baseAdd(Template template, List<TemplateCustomFieldRequest> customFields) {
        checkAddExist(template);
        template.setId(UUID.randomUUID().toString());
        template.setCreateTime(System.currentTimeMillis());
        template.setUpdateTime(System.currentTimeMillis());
        templateMapper.insert(template);
        baseTemplateCustomFieldService.deleteByTemplateId(template.getId());
        baseTemplateCustomFieldService.addByTemplateId(template.getId(), customFields);
        return template;
    }

    public void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new MSException(TEMPLATE_SCENE_ILLEGAL));
    }

    public Template update(Template template, List<TemplateCustomFieldRequest> customFields) {
        checkResourceExist(template.getId());
        checkUpdateExist(template);
        template.setUpdateTime(System.currentTimeMillis());
        template.setInternal(null);
        template.setScene(null);
        template.setScopeType(null);
        template.setScopeId(null);
        template.setCreateUser(null);
        template.setCreateTime(null);
        template.setEnableDefault(null);
        // customFields 为 null 则不修改
        if (customFields != null) {
            baseTemplateCustomFieldService.deleteByTemplateId(template.getId());
            baseTemplateCustomFieldService.addByTemplateId(template.getId(), customFields);
        }
        templateMapper.updateByPrimaryKeySelective(template);
        return template;
    }

    public void delete(String id) {
        Template template = checkResourceExist(id);
        checkInternal(template);
        checkDefault(template);
        templateMapper.deleteByPrimaryKey(id);
        baseTemplateCustomFieldService.deleteByTemplateId(id);
    }

    public void setDefaultTemplate(String id) {
        Template originTemplate = templateMapper.selectByPrimaryKey(id);
        // 将当前模板设置成默认模板
        Template template = new Template();
        template.setId(id);
        template.setEnableDefault(true);
        templateMapper.updateByPrimaryKeySelective(template);

        // 将其他模板设置成非默认模板
        template = new Template();
        template.setEnableDefault(false);
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andIdNotEqualTo(id)
                .andScopeIdEqualTo(originTemplate.getScopeId());
        templateMapper.updateByExampleSelective(template, example);
    }

    /**
     * 校验时候是内置模板
     * @param template
     */
    protected void checkInternal(Template template) {
        if (template.getInternal()) {
            throw new MSException(INTERNAL_TEMPLATE_PERMISSION);
        }
    }

    /**
     * 设置成默认模板后不能删除
     * @param template
     */
    protected void checkDefault(Template template) {
        if (template.getEnableDefault()) {
            throw new MSException(DEFAULT_TEMPLATE_PERMISSION);
        }
    }

    protected void checkAddExist(Template template) {
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeIdEqualTo(template.getScopeId())
                .andNameEqualTo(template.getName());
        if (CollectionUtils.isNotEmpty(templateMapper.selectByExample(example))) {
            throw new MSException(TEMPLATE_EXIST);
        }
    }

    protected void checkUpdateExist(Template template) {
        if (StringUtils.isBlank(template.getName())) {
            return;
        }
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeIdEqualTo(template.getScopeId())
                .andIdNotEqualTo(template.getId())
                .andNameEqualTo(template.getName());
        if (CollectionUtils.isNotEmpty(templateMapper.selectByExample(example))) {
            throw new MSException(TEMPLATE_EXIST);
        }
    }

    private Template checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(templateMapper.selectByPrimaryKey(id), "permission.system_template.name");
    }

    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        return baseCustomFieldService.isOrganizationTemplateEnable(orgId, scene);
    }

    public void deleteByScopeId(String scopeId) {
        // 删除模板
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeIdEqualTo(scopeId);
        templateMapper.deleteByExample(example);

        // 删除模板和字段的关联关系
        List<String> ids = templateMapper.selectByExample(example)
                .stream()
                .map(Template::getId)
                .toList();
        baseTemplateCustomFieldService.deleteByTemplateIds(ids);
    }

    /**
     * 将创建组织模板与字段的关联关系请求
     * 转换为项目模板与字段的关联关系请求
     *
     * @param customFields
     * @return
     */
    public List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(String projectId, List<TemplateCustomFieldRequest> customFields) {
        if (customFields == null) {
            return null;
        }
        List<String> fieldIds = customFields.stream()
                .map(TemplateCustomFieldRequest::getFieldId).toList();
        // 查询当前组织字段所对应的项目字段，构建map，键为组织字段ID，值为项目字段ID
        Map<String, String> refFieldMap = baseCustomFieldService.getByRefIdsAndScopeId(fieldIds, projectId)
                .stream()
                .collect(Collectors.toMap(CustomField::getRefId, CustomField::getId));
        // 根据组织字段ID，替换为项目字段ID
        return customFields.stream()
                .map(item -> {
                    TemplateCustomFieldRequest request = BeanUtils.copyBean(new TemplateCustomFieldRequest(), item);
                    request.setFieldId(refFieldMap.get(item.getFieldId()));
                    return request;
                })
                .toList();
    }

    /**
     * 初始化功能用例模板
     * 创建组织的时候调用初始化组织模板
     * 创建项目的时候调用初始化项目模板
     * @param scopeId
     * @param scopeType
     */
    public void initFunctionalDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        // 初始化字段
        List<CustomField> customFields = baseCustomFieldService.initFunctionalDefaultCustomField(scopeType, scopeId);
        // 初始化模板
        Template template = this.initDefaultTemplate(scopeId, "functional_default", scopeType, TemplateScene.FUNCTIONAL);
        // 初始化模板和字段的关联关系
        List<TemplateCustomFieldRequest> templateCustomFieldRequests = customFields.stream().map(customField -> {
            TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
            templateCustomFieldRequest.setRequired(true);
            templateCustomFieldRequest.setFieldId(customField.getId());
            return templateCustomFieldRequest;
        }).toList();
        baseTemplateCustomFieldService.addByTemplateId(template.getId(), templateCustomFieldRequests);
    }

    /**
     * 初始化缺陷模板
     * 创建组织的时候调用初始化组织模板
     * 创建项目的时候调用初始化项目模板
     * @param scopeId
     * @param scopeType
     */
    public void initIssueDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        this.initDefaultTemplate(scopeId, "issue_default", scopeType, TemplateScene.ISSUE);
    }
    public void initApiDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        this.initDefaultTemplate(scopeId, "api_default", scopeType, TemplateScene.API);
    }

    public void initUiDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        this.initDefaultTemplate(scopeId, "ui_default", scopeType, TemplateScene.UI);
    }

    public void initTestPlanDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        this.initDefaultTemplate(scopeId, "test_plan_default", scopeType, TemplateScene.TEST_PLAN);
    }

    public Template initDefaultTemplate(String scopeId, String name, TemplateScopeType scopeType, TemplateScene scene) {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName(name);
        template.setInternal(true);
        template.setUpdateTime(System.currentTimeMillis());
        template.setCreateTime(System.currentTimeMillis());
        template.setCreateUser("admin");
        template.setScopeType(scopeType.name());
        template.setScopeId(scopeId);
        template.setEnableThirdPart(false);
        template.setEnableDefault(true);
        template.setScene(scene.name());
        templateMapper.insert(template);
        return template;
    }
}