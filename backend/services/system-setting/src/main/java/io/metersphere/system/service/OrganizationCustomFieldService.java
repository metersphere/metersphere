package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.sdk.CustomFieldDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldOptionRequest;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.mapper.ExtOrganizationCustomFieldMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.metersphere.system.controller.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationCustomFieldService extends BaseCustomFieldService {

    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private ExtOrganizationCustomFieldMapper extOrganizationCustomFieldMapper;

    @Override
    public List<CustomFieldDTO> list(String orgId, String scene) {
        OrganizationService.checkResourceExist(orgId);
        return super.list(orgId, scene);
    }

    @Override
    public CustomFieldDTO getCustomFieldDTOWithCheck(String id) {
        CustomFieldDTO customField = super.getCustomFieldDTOWithCheck(id);
        OrganizationService.checkResourceExist(customField.getScopeId());
        return customField;
    }

    @Override
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        OrganizationService.checkResourceExist(customField.getScopeId());
        customField.setScopeType(TemplateScopeType.ORGANIZATION.name());
        customField = super.add(customField, options);
        // 同步创建项目级别字段
        addRefProjectCustomField(customField, options);
        return customField;
    }

    /**
     * 同步创建项目级别字段
     * 当开启组织模板时，操作组织字段，同时维护与之关联的项目字段
     * 避免当开启项目模板时，需要将各个资源关联的模板和字段从组织切换为项目
     * 无论是否开启组织模板，各资源都只关联各自项目下的模板和字段
     *
     * @param orgCustomField
     * @param options
     */
    public void addRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        String orgId = orgCustomField.getScopeId();
        List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgId);
        CustomField customField = BeanUtils.copyBean(new CustomField(), orgCustomField);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        projectIds.forEach(projectId -> {
            customField.setScopeType(TemplateScopeType.PROJECT.name());
            customField.setScopeId(projectId);
            customField.setRefId(orgCustomField.getId());
            super.baseAdd(customField, customFieldOptions);
        });
    }

    @Override
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        CustomField originCustomField = getWithCheck(customField.getId());
        if (originCustomField.getInternal()) {
            // 内置字段不能修改名字
            customField.setName(null);
        }
        checkOrganizationTemplateEnable(customField.getScopeId(), originCustomField.getScene());
        customField.setScopeId(originCustomField.getScopeId());
        customField.setScene(originCustomField.getScene());
        OrganizationService.checkResourceExist(originCustomField.getScopeId());
        // 同步创建项目级别字段
        updateRefProjectCustomField(customField, options);
        return super.update(customField, options);
    }

    /**
     * 同步更新项目级别字段
     * 当开启组织模板时，操作组织字段，同时维护与之关联的项目字段
     * 避免当开启项目模板时，需要将各个资源关联的模板和字段从组织切换为项目
     * 无论是否开启组织模板，各资源都只关联各自项目下的模板和字段
     *
     * @param orgCustomField
     * @param options
     */
    public void updateRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        List<CustomField> projectFields = getByRefId(orgCustomField.getId());
        CustomField customField = BeanUtils.copyBean(new CustomField(), orgCustomField);
        projectFields.forEach(projectField -> {
            customField.setId(projectField.getId());
            customField.setScopeId(projectField.getScopeId());
            customField.setRefId(orgCustomField.getId());
            customField.setScene(orgCustomField.getScene());
            super.update(customField, options);
        });
    }

    public List<CustomField> getByRefId(String refId) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andRefIdEqualTo(refId);
        return customFieldMapper.selectByExample(example);
    }

    @Override
    public void delete(String id) {
        CustomField customField = getWithCheck(id);
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        checkInternal(customField);
        OrganizationService.checkResourceExist(customField.getScopeId());
        // 同步删除项目级别字段
        deleteRefProjectTemplate(id);
        super.delete(id);
    }

    /**
     * 同步删除项目级别字段
     * 当开启组织模板时，操作组织字段，同时维护与之关联的项目字段
     * 避免当开启项目模板时，需要将各个资源关联的模板和字段从组织切换为项目
     * 无论是否开启组织模板，各资源都只关联各自项目下的模板和字段
     *
     * @param orgCustomFieldId
     */
    public void deleteRefProjectTemplate(String orgCustomFieldId) {
        // 删除关联的项目字段
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andRefIdEqualTo(orgCustomFieldId);
        customFieldMapper.deleteByExample(example);

        // 删除字段选项
        List<String> projectCustomFieldIds = extOrganizationCustomFieldMapper.getCustomFieldByRefId(orgCustomFieldId);
        // 分批删除
        SubListUtils.dealForSubList(projectCustomFieldIds, 100, baseCustomFieldOptionService::deleteByFieldIds);
    }

    private void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new MSException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }
}