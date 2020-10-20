package io.metersphere.notice.controller;

import io.metersphere.notice.controller.request.MessageRequest;
import io.metersphere.notice.controller.request.NoticeRequest;
import io.metersphere.notice.domain.MessageSettingDetail;
import io.metersphere.notice.domain.NoticeDetail;
import io.metersphere.notice.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("notice")
public class NoticeController {
    @Resource
    private NoticeService noticeService;

    @PostMapping("/save")
    public void saveNotice(@RequestBody NoticeRequest noticeRequest) {
        noticeService.saveNotice(noticeRequest);
    }

    @GetMapping("/query/{testId}")
    public List<NoticeDetail> queryNotice(@PathVariable String testId) {
        return noticeService.queryNotice(testId);
    }

    @PostMapping("save/message/task")
    public void saveMessage(@RequestBody MessageRequest messageRequest) {
        noticeService.saveMessageTask(messageRequest);
    }

    @GetMapping("/search/message")
    public MessageSettingDetail searchMessage() {
        return noticeService.searchMessage();
    }

    @GetMapping("/delete/message/{identification}")
    public int deleteMessage(@PathVariable String identification) {
        return noticeService.delMessage(identification);
    }
}

