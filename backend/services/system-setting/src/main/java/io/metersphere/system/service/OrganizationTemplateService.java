package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.system.domain.Template;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationTemplateService extends BaseTemplateService {

    @Resource
    private OrganizationService organizationService;

    @Override
    public List<Template> list(String organizationId, String scene) {
        organizationService.checkResourceExist(organizationId);
        return super.list(organizationId, scene);
    }

    public TemplateDTO geDTOWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkOrgResourceExist(template);
        return super.geDTOWithCheck(template);
    }

    @Override
    public Template add(Template template, List<TemplateCustomFieldRequest> customFields) {
        checkOrgResourceExist(template);
        // todo 校验是否开启组织模板
        // todo 同步创建项目级别模板
        template.setScopeType(TemplateScopeType.ORGANIZATION.name());
        return super.add(template, customFields);
    }

    public void checkOrgResourceExist(Template template) {
        organizationService.checkResourceExist(template.getScopeId());
    }

    @Override
    public Template update(Template template, List<TemplateCustomFieldRequest> customFields) {
        Template originTemplate = super.getWithCheck(template.getId());
        template.setScopeId(originTemplate.getScopeId());
        checkOrgResourceExist(originTemplate);
        return super.update(template, customFields);
    }

    @Override
    public void delete(String id) {
        this.getWithCheck(id);
        Template template = getWithCheck(id);
        checkInternal(template);
        super.delete(id);
    }
}