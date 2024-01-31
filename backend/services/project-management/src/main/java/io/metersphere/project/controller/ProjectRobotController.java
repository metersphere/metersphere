package io.metersphere.project.controller;


import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.dto.ProjectRobotDTO;
import io.metersphere.project.service.MessageTaskLogService;
import io.metersphere.project.service.ProjectRobotService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目管理-消息管理-机器人")
@RestController
@RequestMapping("/project/robot/")
public class ProjectRobotController {

    @Resource
    private ProjectRobotService projectRobotService;


    @GetMapping("/list/{projectId}")
    @Operation(summary = "项目管理-消息管理-获取机器人列表")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    public List<ProjectRobot> listResourcePools(@PathVariable(value = "projectId") String projectId) {
        return projectRobotService.getList(projectId);
    }

    @PostMapping("add")
    @Operation(summary = "项目管理-消息管理-新增机器人")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addRobotLog(#projectRobotDTO)", msClass = MessageTaskLogService.class)
    public ProjectRobot add(@Validated({Created.class}) @RequestBody ProjectRobotDTO projectRobotDTO) {
        ProjectRobot projectRobot = new ProjectRobot();
        BeanUtils.copyBean(projectRobot, projectRobotDTO);
        projectRobot.setCreateUser(SessionUtils.getUserId());
        projectRobot.setCreateTime(System.currentTimeMillis());
        projectRobot.setUpdateUser(SessionUtils.getUserId());
        projectRobot.setUpdateTime(System.currentTimeMillis());
        return projectRobotService.add(projectRobot);
    }

    @PostMapping("update")
    @Operation(summary = "项目管理-消息管理-更新机器人")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateRobotLog(#projectRobotDTO)", msClass = MessageTaskLogService.class)
    public void update(@Validated({Updated.class}) @RequestBody ProjectRobotDTO projectRobotDTO) {
        ProjectRobot projectRobot = new ProjectRobot();
        BeanUtils.copyBean(projectRobot, projectRobotDTO);
        projectRobot.setCreateUser(null);
        projectRobot.setCreateTime(null);
        projectRobot.setUpdateUser(SessionUtils.getUserId());
        projectRobot.setUpdateTime(System.currentTimeMillis());
        projectRobotService.update(projectRobot);
    }

    @GetMapping("get/{id}")
    @Operation(summary = "项目管理-消息管理-获取机器人详情")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    public ProjectRobotDTO getDetail(@PathVariable(value = "id") String id) {
        return projectRobotService.getDetail(id);
    }

    @GetMapping("delete/{id}")
    @Operation(summary = "项目管理-消息管理-删除机器人")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.delRobotLog(#id)", msClass = MessageTaskLogService.class)
    public void delete(@PathVariable(value = "id") String id) {
        projectRobotService.delete(id);
    }

    @GetMapping("enable/{id}")
    @Operation(summary = "项目管理-消息管理-启禁用机器人")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_UPDATE)
    public void enable(@PathVariable(value = "id") String id) {
        projectRobotService.enable(id, SessionUtils.getUserId(), System.currentTimeMillis());
    }


}
