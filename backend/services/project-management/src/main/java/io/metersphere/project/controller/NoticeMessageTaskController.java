package io.metersphere.project.controller;


import io.metersphere.project.service.NoticeMessageTaskService;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.request.MessageTaskRequest;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@Tag(name = "项目管理-消息设置")
@RestController
@RequestMapping("notice/")
public class NoticeMessageTaskController {
    @Resource
    private NoticeMessageTaskService noticeMessageTaskService;

    @PostMapping("message/task/add")
    @Operation(summary = "项目管理-消息设置-保存消息设置")
    public ResultHolder saveMessage(@Validated({Created.class}) @RequestBody MessageTaskRequest messageTaskRequest) {
       return noticeMessageTaskService.saveMessageTask(messageTaskRequest, SessionUtils.getUserId());
    }

}

