package io.metersphere.api.service;

import io.metersphere.api.constants.ApiResource;
import io.metersphere.project.constants.ProjectMenuConstants;
import io.metersphere.system.service.CommonProjectService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class ApiValidateService {
    @Resource
    private CommonProjectService commonProjectService;

    //校验接口菜单是否开启
    public void validateApiMenuInProject(String resourceId, String resourceType) {
        String tableName = null;
        if (StringUtils.equals(resourceType, ApiResource.PROJECT.name())) {
            tableName = "project";
        } else if (StringUtils.equals(resourceType, ApiResource.API_DEFINITION.name())) {
            tableName = "api_definition";
        } else if (StringUtils.equals(resourceType, ApiResource.API_TEST_CASE.name())) {
            tableName = "api_test_case";
        } else if (StringUtils.equals(resourceType, ApiResource.API_SCENARIO.name())) {
            tableName = "api_scenario";
        }
        commonProjectService.checkProjectHasModuleMenu(Collections.singletonList(ProjectMenuConstants.MODULE_MENU_API_TEST), resourceId, tableName);
    }
}
