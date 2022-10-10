package io.metersphere.controller;

import io.metersphere.dto.SystemStatisticData;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.UserService;
import io.metersphere.service.WorkspaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Resource
    private UserService userService;
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private BaseProjectService baseProjectService;

    @GetMapping("/statistics/data")
    public SystemStatisticData getStatisticsData() {
        SystemStatisticData systemStatisticData = new SystemStatisticData();
        long userSize = userService.getUserSize();
        long workspaceSize = workspaceService.getWorkspaceSize();
        long projectSize = baseProjectService.getProjectSize();
        systemStatisticData.setUserSize(userSize);
        systemStatisticData.setWorkspaceSize(workspaceSize);
        systemStatisticData.setProjectSize(projectSize);
        return systemStatisticData;
    }
}
