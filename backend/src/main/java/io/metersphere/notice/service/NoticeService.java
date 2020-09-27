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
import java.util.UUID;

import static io.metersphere.commons.constants.NoticeConstants.EXECUTE_FAILED;
import static io.metersphere.commons.constants.NoticeConstants.EXECUTE_SUCCESSFUL;

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
            if (n.getNames().length > 0) {
                for (String x : n.getNames()) {
                    Notice notice = new Notice();
                    notice.setId(UUID.randomUUID().toString());
                    notice.setEvent(n.getEvent());
                    notice.setEnable(n.getEnable());
                    notice.setTestId(noticeRequest.getTestId());
                    notice.setName(x);
                    notice.setType(n.getType());
                    noticeMapper.insert(notice);
                }
            }
        });
    }

    public List<NoticeDetail> queryNotice(String id) {
        NoticeExample example = new NoticeExample();
        example.createCriteria().andTestIdEqualTo(id);
        List<Notice> notices = noticeMapper.selectByExample(example);
        List<NoticeDetail> result = new ArrayList<>();
        List<String> success = new ArrayList<>();
        List<String> fail = new ArrayList<>();
        String[] successArray;
        String[] failArray;
        NoticeDetail notice1 = new NoticeDetail();
        NoticeDetail notice2 = new NoticeDetail();
        if (notices.size() > 0) {
            for (Notice n : notices) {
                if (n.getEvent().equals(EXECUTE_SUCCESSFUL)) {
                    success.add(n.getName());
                    notice1.setEnable(n.getEnable());
                    notice1.setTestId(id);
                    notice1.setType(n.getType());
                    notice1.setEvent(n.getEvent());
                }
                if (n.getEvent().equals(EXECUTE_FAILED)) {
                    fail.add(n.getName());
                    notice2.setEnable(n.getEnable());
                    notice2.setTestId(id);
                    notice2.setType(n.getType());
                    notice2.setEvent(n.getEvent());
                }
            }
            successArray = success.toArray(new String[0]);
            failArray = fail.toArray(new String[0]);
            notice1.setNames(successArray);
            notice2.setNames(failArray);
            result.add(notice1);
            result.add(notice2);
        }
        return result;
    }

}
