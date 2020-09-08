package io.metersphere.notice.service;

import io.metersphere.base.domain.Notice;
import io.metersphere.base.domain.NoticeExample;
import io.metersphere.base.mapper.NoticeMapper;
import io.metersphere.notice.controller.request.NoticeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class NoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    public void saveNotice(NoticeRequest noticeRequest) {
        NoticeExample example = new NoticeExample();
        example.createCriteria().andTestIdEqualTo(noticeRequest.getTestId());
        List<Notice> notices = noticeMapper.selectByExample(example);
        if (notices != null) {
            noticeMapper.deleteByExample(example);
            noticeRequest.getNotices().forEach(notice -> {
                notice.setEvent(noticeRequest.getEvent());
                notice.setTestId(noticeRequest.getTestId());
                noticeMapper.insert(notice);
            });
        } else {
            noticeRequest.getNotices().forEach(notice -> {
                notice.setEvent(noticeRequest.getEvent());
                notice.setTestId(noticeRequest.getTestId());
                noticeMapper.insert(notice);
            });
        }

    }

    public List<Notice> queryNotice(String id) {
        NoticeExample example = new NoticeExample();
        example.createCriteria().andTestIdEqualTo(id);
        return noticeMapper.selectByExample(example);
    }

}
