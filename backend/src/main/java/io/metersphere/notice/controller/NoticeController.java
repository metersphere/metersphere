package io.metersphere.notice.controller;

import io.metersphere.notice.controller.request.NoticeRequest;
import io.metersphere.notice.domain.NoticeDTO;
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
    public List<NoticeDTO> queryNotice(@PathVariable String testId) {
        return noticeService.queryNotice(testId);
    }

}
