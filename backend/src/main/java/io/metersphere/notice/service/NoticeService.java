package io.metersphere.notice.service;

import io.metersphere.base.domain.Notice;
import io.metersphere.base.domain.NoticeExample;
import io.metersphere.base.mapper.NoticeMapper;
import io.metersphere.notice.controller.request.NoticeRequest;
import io.metersphere.notice.domain.NoticeDetail;
import org.apache.commons.collections4.CollectionUtils;
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
            if (CollectionUtils.isNotEmpty(n.getUserIds())) {
                for (String x : n.getUserIds()) {
                    Notice notice = new Notice();
                    notice.setId(UUID.randomUUID().toString());
                    notice.setEvent(n.getEvent());
                    notice.setEnable(n.getEnable());
                    notice.setTestId(noticeRequest.getTestId());
                    notice.setUserId(x);
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
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();
        NoticeDetail notice1 = new NoticeDetail();
        NoticeDetail notice2 = new NoticeDetail();
        if (notices.size() > 0) {
            for (Notice n : notices) {
                if (n.getEvent().equals(EXECUTE_SUCCESSFUL)) {
                    successList.add(n.getUserId());
                    notice1.setEnable(n.getEnable());
                    notice1.setTestId(id);
                    notice1.setType(n.getType());
                    notice1.setEvent(n.getEvent());
                }
                if (n.getEvent().equals(EXECUTE_FAILED)) {
                    failList.add(n.getUserId());
                    notice2.setEnable(n.getEnable());
                    notice2.setTestId(id);
                    notice2.setType(n.getType());
                    notice2.setEvent(n.getEvent());
                }
            }
            notice1.setUserIds(successList);
            notice2.setUserIds(failList);
            result.add(notice1);
            result.add(notice2);
        }
        return result;
    }

}
