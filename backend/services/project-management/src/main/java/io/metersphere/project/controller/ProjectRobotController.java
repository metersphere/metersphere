package io.metersphere.project.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.dto.ProjectRobotDTO;
import io.metersphere.project.request.ProjectRobotRequest;
import io.metersphere.project.service.ProjectRobotService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目机器人管理")
@RestController
@RequestMapping("/project/robot/")
public class ProjectRobotController {

    @Resource
    private ProjectRobotService projectRobotService;


    @PostMapping("/list/page")
    @Operation(summary = "获取机器人列表")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    public Pager<List<ProjectRobot>> listResourcePools(@Validated @RequestBody ProjectRobotRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, projectRobotService.getList(request));
    }

    @PostMapping("add")
    @Operation(summary = "新增机器人管理")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_ADD)
    public void add(@Validated({Created.class}) @RequestBody ProjectRobotDTO projectRobotDTO) {
        ProjectRobot projectRobot = new ProjectRobot();
        BeanUtils.copyBean(projectRobot, projectRobotDTO);
        projectRobot.setCreateUser(SessionUtils.getUserId());
        projectRobot.setCreateTime(System.currentTimeMillis());
        projectRobot.setUpdateUser(SessionUtils.getUserId());
        projectRobot.setUpdateTime(System.currentTimeMillis());
        projectRobotService.add(projectRobot);
    }

    @PostMapping("update")
    @Operation(summary = "更新机器人")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_UPDATE)
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
    @Operation(summary = "获取机器人详情")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    public ProjectRobotDTO getDetail(@PathVariable(value = "id") String id) {
        return projectRobotService.getDetail(id);
    }

    @GetMapping("delete/{id}")
    @Operation(summary = "删除机器人")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_DELETE)
    public void delete(@PathVariable(value = "id") String id) {
        projectRobotService.delete(id);
    }

    @GetMapping("enable/{id}")
    @Operation(summary = "禁用机器人")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_UPDATE)
    public void enable(@PathVariable(value = "id") String id) {
        projectRobotService.enable(id, SessionUtils.getUserId(), System.currentTimeMillis());
    }


}
