package io.metersphere.dashboard.service;

import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.PermissionCheckService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.user.UserDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoyuqi
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardService {


    @Resource
    private DashboardProjectService dashboardProjectService;



    public Map<String, Integer> createByMeCount(DashboardFrontPageRequest request, String userId) {
        Map<String, Integer> map = new HashMap<>();
        Map<String, Set<String>> permissionModuleProjectIds = dashboardProjectService.getPermissionModuleProjectIds(request.getOrganizationId(),request.getProjectIds(), userId);
        //功能用例
        //用例评审
        //接口
        //接口用例
        //接口场景
        //测试计划
        //缺陷管理

        return map;
    }


}
