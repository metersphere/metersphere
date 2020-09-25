package io.metersphere.notice.service;

import io.metersphere.base.domain.Notice;
import io.metersphere.base.domain.NoticeExample;
import io.metersphere.base.mapper.NoticeMapper;
import io.metersphere.notice.controller.request.NoticeRequest;
import io.metersphere.notice.domain.NoticeDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    public void saveNotice(NoticeRequest noticeRequest) {
        NoticeExample example = new NoticeExample();
        example.createCriteria().andTestIdEqualTo(noticeRequest.getTestId());
        List<Notice> notices = noticeMapper.selectByExample(example);
        if (notices.size() > 0) {
            noticeMapper.deleteByExample(example);
        }
        noticeRequest.getNotices().forEach(n -> {
            Notice notice = new Notice();
            notice.setEvent(n.getEvent());
            notice.setEmail(n.getEmail());
            notice.setEnable(n.getEnable());
            notice.setTestId(noticeRequest.getTestId());
            notice.setName("");
            noticeMapper.insert(notice);
        });
    }

    public List<NoticeDTO> queryNotice(String id) {
        NoticeExample example = new NoticeExample();
        example.createCriteria().andTestIdEqualTo(id);
        List<Notice> notices = noticeMapper.selectByExample(example);
        List<NoticeDTO> noticeDTOS = new ArrayList<>();
        List<String> success = new ArrayList<>();
        List<String> fail = new ArrayList<>();
        String[] successArray;
        String[] failArray;
        NoticeDTO notice1 = new NoticeDTO();
        NoticeDTO notice2 = new NoticeDTO();
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
        successArray = success.toArray(new String[0]);
        failArray = fail.toArray(new String[0]);
        notice1.setNames(successArray);
        notice2.setNames(failArray);
        noticeDTOS.add(notice1);
        noticeDTOS.add(notice2);
        return noticeDTOS;
    }

}
