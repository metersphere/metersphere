package io.metersphere.notice.service;

import io.metersphere.base.domain.Notice;
import io.metersphere.base.mapper.NoticeMapper;
import io.metersphere.notice.controller.request.NoticeRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class NoticeService {
   @Resource
    private NoticeMapper noticeMapper;
    public void saveNotice(NoticeRequest noticeRequest){
        Notice notice=new Notice();
        noticeMapper.insert(notice);
    }
}
