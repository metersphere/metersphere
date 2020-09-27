package io.metersphere.notice.service;

import io.metersphere.base.domain.Notice;
import io.metersphere.base.domain.NoticeExample;
import io.metersphere.base.mapper.NoticeMapper;
import io.metersphere.notice.controller.request.NoticeRequest;
import io.metersphere.notice.domain.NoticeDetail;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    public void saveNotice(NoticeRequest noticeRequest) {
        Notice notice = new Notice();
        NoticeExample example = new NoticeExample();
        example.createCriteria().andTestIdEqualTo(noticeRequest.getTestId());
        List<Notice> notices = noticeMapper.selectByExample(example);
        if (notices.size() > 0) {
            noticeMapper.deleteByExample(example);
        }
        saveNotice(noticeRequest, notice);
    }

    private void saveNotice(NoticeRequest noticeRequest, Notice notice) {
        noticeRequest.getNotices().forEach(n -> {
            if (n.getNames().length > 0) {
                for (String x : n.getNames()) {
                    notice.setEvent(n.getEvent());
                    notice.setEmail(n.getEmail());
                    notice.setEnable(n.getEnable());
                    notice.setTestId(noticeRequest.getTestId());
                    notice.setName(x);
                    noticeMapper.insert(notice);
                }
            } else {
                notice.setEvent(n.getEvent());
                notice.setEmail(n.getEmail());
                notice.setEnable(n.getEnable());
                notice.setTestId(noticeRequest.getTestId());
                notice.setName("");
                noticeMapper.insert(notice);
            }
        });
    }

    public List<NoticeDetail> queryNotice(String id) {
        NoticeExample example = new NoticeExample();
        example.createCriteria().andTestIdEqualTo(id);
        List<Notice> notices = noticeMapper.selectByExample(example);
        List<NoticeDetail> notice = new ArrayList<>();
        List<String> success = new ArrayList<>();
        List<String> fail = new ArrayList<>();
        String[] successArray;
        String[] failArray;
        NoticeDetail notice1 = new NoticeDetail();
        NoticeDetail notice2 = new NoticeDetail();
        if (notices.size() > 0) {
            for (Notice n : notices) {
                if (n.getEvent().equals("执行成功")) {
                    success.add(n.getName());
                    notice1.setEnable(n.getEnable());
                    notice1.setTestId(id);
                    notice1.setEvent(n.getEvent());
                    notice1.setEmail(n.getEmail());
                }
                if (n.getEvent().equals("执行失败")) {
                    fail.add(n.getName());
                    notice2.setEnable(n.getEnable());
                    notice2.setTestId(id);
                    notice2.setEvent(n.getEvent());
                    notice2.setEmail(n.getEmail());
                }
            }
            successArray = success.toArray(new String[success.size()]);
            failArray = fail.toArray(new String[fail.size()]);
            notice1.setNames(successArray);
            notice2.setNames(failArray);
            notice.add(notice1);
            notice.add(notice2);
        }
        return notice;
    }

}
