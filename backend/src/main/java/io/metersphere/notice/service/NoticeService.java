package io.metersphere.notice.service;

import io.metersphere.base.domain.Notice;
import io.metersphere.base.domain.NoticeExample;
import io.metersphere.base.mapper.NoticeMapper;
import io.metersphere.notice.controller.request.NoticeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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
                for(String x:notice.getNames()){
                    notice.setEvent(notice.getEvent());
                    notice.setEmail(notice.getEmail());
                    notice.setEnable(notice.getEnable());
                    notice.setTestId(noticeRequest.getTestId());
                    notice.setName(x);
                    noticeMapper.insert(notice);
                }
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
        List<Notice>  notices=noticeMapper.selectByExample(example);
        List<Notice>  notice=new ArrayList<>();
        List<String>  success=new ArrayList<>();
        List<String>  fail=new ArrayList<>();
        String[] successArray=new String[success.size()];
        String[] failArray=new String[fail.size()];
        Notice  notice1=new Notice();
        Notice  notice2=new Notice();
        if(notices.size()>0){
            for(Notice n:notices){
                if(n.getEvent().equals("执行成功")){
                    success.add(n.getName());
                    notice1.setEnable(n.getEnable());
                    notice1.setTestId(id);
                    notice1.setEvent(n.getEvent());
                    notice1.setEmail(n.getEmail());
                }
                if(n.getEvent().equals("执行失败")){
                    fail.add(n.getName());
                    notice2.setEnable(n.getEnable());
                    notice2.setTestId(id);
                    notice2.setEvent(n.getEvent());
                    notice2.setEmail(n.getEmail());
                }
            }
            successArray=success.toArray(new String[success.size()]);
            failArray=fail.toArray(new String[fail.size()]);
            notice1.setNames(successArray);
            notice2.setNames(failArray);
            notice.add(notice1);
            notice.add(notice2);
        }
        return notice;
    }

}
