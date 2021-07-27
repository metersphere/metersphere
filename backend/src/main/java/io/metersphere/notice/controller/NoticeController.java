package io.metersphere.notice.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("notice")
public class NoticeController {
    @Resource
    private NoticeService noticeService;

    @PostMapping("save/message/task")
    @MsAuditLog(module = "organization_message_settings", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#messageDetail.id)", content = "#msClass.getLogDetails(#messageDetail.id)", msClass = NoticeService.class)
    public void saveMessage(@RequestBody MessageDetail messageDetail) {
        noticeService.saveMessageTask(messageDetail);
    }

    @GetMapping("/search/message/type/{type}")
    public List<MessageDetail> searchMessage(@PathVariable String type) {
        return noticeService.searchMessageByType(type);
    }

    @GetMapping("/search/message/{testId}")
    public List<MessageDetail> searchMessageSchedule(@PathVariable String testId) {
        return noticeService.searchMessageByTestId(testId);
    }

    @GetMapping("/delete/message/{identification}")
    @MsAuditLog(module = "organization_message_settings", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#identification)", msClass = NoticeService.class)
    public int deleteMessage(@PathVariable String identification) {
        return noticeService.delMessage(identification);
    }
}

