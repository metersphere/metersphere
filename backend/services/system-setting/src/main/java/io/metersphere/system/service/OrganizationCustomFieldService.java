package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.CustomFieldDTO;
import io.metersphere.sdk.dto.request.CustomFieldOptionRequest;
import io.metersphere.system.domain.CustomField;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationCustomFieldService extends BaseCustomFieldService {

    @Resource
    private OrganizationService organizationService;

    @Override
    public List<CustomField> list(String orgId, String scene) {
        organizationService.checkResourceExist(orgId);
        return super.list(orgId, scene);
    }

    @Override
    public CustomFieldDTO getCustomFieldDTOWithCheck(String id) {
        CustomFieldDTO customField = super.getCustomFieldDTOWithCheck(id);
        organizationService.checkResourceExist(customField.getScopeId());
        return customField;
    }

    @Override
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        // todo 校验是否开启组织模板
        // todo 同步创建项目级别模板
        organizationService.checkResourceExist(customField.getScopeId());
        customField.setScopeType(TemplateScopeType.ORGANIZATION.name());
        return super.add(customField, options);
    }

    @Override
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        // todo 校验是否开启组织模板
        // todo 同步修改项目级别模板
        CustomField originCustomField = getWithCheck(customField.getId());
        customField.setScopeId(originCustomField.getScopeId());
        organizationService.checkResourceExist(originCustomField.getScopeId());
        return super.update(customField, options);
    }

    @Override
    public void delete(String id) {
        CustomField customField = getWithCheck(id);
        checkInternal(customField);
        organizationService.checkResourceExist(customField.getScopeId());
        super.delete(id);
    }
}