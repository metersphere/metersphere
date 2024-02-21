package io.metersphere.api.service;

import io.metersphere.project.constants.ProjectMenuConstants;
import io.metersphere.system.service.CommonProjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class ApiValidateService {
    @Resource
    private CommonProjectService commonProjectService;

    //校验接口菜单是否开启
    public void validateApiMenuInProject(String resourceId, String resourceType) {
        String tableName = resourceType.toLowerCase();
        commonProjectService.checkProjectHasModuleMenu(Collections.singletonList(ProjectMenuConstants.MODULE_MENU_API_TEST), resourceId, tableName);
    }
}
