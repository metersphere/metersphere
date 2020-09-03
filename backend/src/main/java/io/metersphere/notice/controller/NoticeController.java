package io.metersphere.notice.controller;

import io.metersphere.notice.controller.request.NoticeRequest;
import io.metersphere.notice.service.NoticeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("notice")
public class NoticeController {
   @Resource
   private NoticeService noticeService;
   @PostMapping("/save")
   public void saveNotice(NoticeRequest noticeRequest){
       noticeService.saveNotice(noticeRequest);
   }
}
