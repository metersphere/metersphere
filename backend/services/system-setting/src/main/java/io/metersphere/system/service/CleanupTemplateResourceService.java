package io.metersphere.system.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CleanupTemplateResourceService implements CleanupProjectResourceService {
    @Resource
    private BaseTemplateService baseTemplateService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private BaseStatusFlowSettingService baseStatusFlowSettingService;

    @Override
    public void deleteResources(String projectId) {
        baseTemplateService.deleteByScopeId(projectId);
        baseCustomFieldService.deleteByScopeId(projectId);
        baseStatusFlowSettingService.deleteByScopeId(projectId);
    }

}
