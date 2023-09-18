package io.metersphere.project.controller;


import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.service.MessageTaskLogService;
import io.metersphere.project.service.NoticeMessageTaskService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.request.MessageTaskRequest;

import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "项目管理-项目与权限-消息管理-消息设置")
@RestController
@RequestMapping("notice/")
public class NoticeMessageTaskController {
    @Resource
    private NoticeMessageTaskService noticeMessageTaskService;

    @PostMapping("message/task/save")
    @Operation(summary = "项目管理-项目与权限-消息管理-消息设置-保存消息设置")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_MESSAGE_READ_ADD, PermissionConstants.PROJECT_MESSAGE_READ_UPDATE}, logical = Logical.OR)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addLog(#messageTaskRequest)", msClass = MessageTaskLogService.class)
    public ResultHolder saveMessage(@Validated({Created.class, Updated.class}) @RequestBody MessageTaskRequest messageTaskRequest) {
        return noticeMessageTaskService.saveMessageTask(messageTaskRequest, SessionUtils.getUserId());
    }

    @GetMapping("message/task/get/{projectId}")
    @Operation(summary = "项目管理-项目与权限-消息管理-消息设置-获取消息设置")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ_ADD)
    public List<MessageTaskDTO> getMessageList(@PathVariable String projectId) {
        return noticeMessageTaskService.getMessageList(projectId);
    }

}

