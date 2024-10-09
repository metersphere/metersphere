package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import io.metersphere.system.dto.taskhub.TaskHubScheduleDTO;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "组织任务中心")
@RestController
@RequestMapping("/organization/task-center")
public class OrganizationTaskHubController {

    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    BaseProjectMapper baseProjectMapper;

    @PostMapping("/exec-task/page")
    @Operation(summary = "组织-任务中心-执行任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ)
    public Pager<List<TaskHubDTO>> projectList(@Validated @RequestBody BasePageRequest request) {
        return baseTaskHubService.getTaskList(request, SessionUtils.getCurrentOrganizationId(), null);
    }


    @PostMapping("/schedule/page")
    @Operation(summary = "组织-任务中心-后台执行任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_SCHEDULE_TASK_CENTER_READ)
    public Pager<List<TaskHubScheduleDTO>> scheduleList(@Validated @RequestBody BasePageRequest request) {
        List<OptionDTO> projectList = baseProjectMapper.getProjectOptionsByOrgId(SessionUtils.getCurrentOrganizationId());
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        return baseTaskHubService.getScheduleTaskList(request, projectIds);
    }
}
