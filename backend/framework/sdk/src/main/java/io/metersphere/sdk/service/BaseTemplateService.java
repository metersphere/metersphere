package io.metersphere.sdk.service;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.dto.TemplateCustomFieldDTO;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.ServiceUtils;
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
import io.metersphere.sdk.uid.UUID;
import java.util.stream.Collectors;

import static io.metersphere.sdk.controller.handler.result.CommonResultCode.*;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTemplateService {

    @Resource
    private TemplateMapper templateMapper;
    @Resource
    private BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;

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
        List<Template> templates = templateMapper.selectByExample(example);
        return templates;
    }

    public String translateInternalTemplate(String filedName) {
        return Translator.get("template.") + filedName;
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
        checkAddExist(template);
        template.setId(UUID.randomUUID().toString());
        template.setCreateTime(System.currentTimeMillis());
        template.setUpdateTime(System.currentTimeMillis());
        template.setInternal(false);
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
        // customFields 为 null 则不修改
        if (customFields != null) {
            baseTemplateCustomFieldService.deleteByTemplateId(template.getId());
            baseTemplateCustomFieldService.addByTemplateId(template.getId(), customFields);
        }
        templateMapper.updateByPrimaryKeySelective(template);
        return template;
    }

    public void delete(String id) {
        checkResourceExist(id);
        templateMapper.deleteByPrimaryKey(id);
        baseTemplateCustomFieldService.deleteByTemplateId(id);
    }

    protected void checkInternal(Template template) {
        if (template.getInternal()) {
            throw new MSException(INTERNAL_TEMPLATE_PERMISSION);
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
}