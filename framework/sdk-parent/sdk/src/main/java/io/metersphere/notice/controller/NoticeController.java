package io.metersphere.notice.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.service.NoticeService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("notice")
public class NoticeController {
    @Resource
    private NoticeService noticeService;

    @PostMapping("save/message/task")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MESSAGE_SETTINGS, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#messageDetail.id)", msClass = NoticeService.class)
    public void saveMessage(@RequestBody MessageDetail messageDetail) {
        noticeService.saveMessageTask(messageDetail);
    }

    @PostMapping("update/message/task")
    @RequiresPermissions("PROJECT_MESSAGE:READ+EDIT")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MESSAGE_SETTINGS, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#messageDetail.id)", content = "#msClass.getLogDetails(#messageDetail.id)", msClass = NoticeService.class)
    public void updateMessage(@RequestBody MessageDetail messageDetail) {
        noticeService.saveMessageTask(messageDetail);
    }

    @GetMapping("/search/message/type/{type}")
    public List<MessageDetail> searchMessage(@PathVariable String type) {
        String projectId = SessionUtils.getCurrentProjectId();
        return noticeService.searchMessageByTypeAndProjectId(type, projectId);
    }

    @GetMapping("/search/message/{testId}")
    public List<MessageDetail> searchMessageSchedule(@PathVariable String testId) {
        return noticeService.searchMessageByTestId(testId);
    }

    @GetMapping("/delete/message/{identification}")
    @RequiresPermissions("PROJECT_MESSAGE:READ+DELETE")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MESSAGE_SETTINGS, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getDelLogDetails(#identification)", msClass = NoticeService.class)
    public int deleteMessage(@PathVariable String identification) {
        return noticeService.delMessage(identification);
    }
}

