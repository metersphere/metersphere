package io.metersphere.project.controller;


import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.dto.MessageTemplateConfigDTO;
import io.metersphere.project.service.MessageTaskLogService;
import io.metersphere.project.service.NoticeMessageTaskService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.MessageTaskRequest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@Tag(name = "项目管理-消息管理-消息设置")
@RestController
@RequestMapping("notice/")
public class NoticeMessageTaskController {
    @Resource
    private NoticeMessageTaskService noticeMessageTaskService;

    @PostMapping("message/task/save")
    @Operation(summary = "项目管理-消息管理-消息设置-保存消息设置")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_MESSAGE_READ_ADD, PermissionConstants.PROJECT_MESSAGE_READ_UPDATE}, logical = Logical.OR)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addLog(#messageTaskRequest)", msClass = MessageTaskLogService.class)
    @CheckOwner(resourceId = "#messageTaskRequest.projectId", resourceType = "project")
    public ResultHolder saveMessage(@Validated({Created.class, Updated.class}) @RequestBody MessageTaskRequest messageTaskRequest) {
        return noticeMessageTaskService.saveMessageTask(messageTaskRequest, SessionUtils.getUserId());
    }

    @GetMapping("message/task/get/{projectId}")
    @Operation(summary = "项目管理-消息管理-消息设置-获取消息设置")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<MessageTaskDTO> getMessageList(@PathVariable String projectId) throws IOException {
        return noticeMessageTaskService.getMessageList(projectId);
    }

    @GetMapping("message/task/get/user/{projectId}")
    @Operation(summary = "项目管理-消息管理-消息设置-获取用户列表")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    public List<OptionDTO> getUserList(@PathVariable String projectId, @Schema(description = "查询关键字，根据用户名查询")
    @RequestParam(value = "keyword", required = false) String keyword) {
        return noticeMessageTaskService.getUserList(projectId, keyword);
    }

    @GetMapping("message/template/detail/{projectId}")
    @Operation(summary = "项目管理-消息管理-消息设置-查看消息模版详情")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    public MessageTemplateConfigDTO getTemplateDetail(@PathVariable String projectId, @Schema(description = "消息配置功能类型")
    @RequestParam(value = "taskType") String taskType, @Schema(description = "消息配置场景")
    @RequestParam(value = "event") String event, @Schema(description = "消息配置机器人id")
    @RequestParam(value = "robotId") String robotId) {
        return noticeMessageTaskService.getTemplateDetail(projectId, taskType, event, robotId);
    }

}

