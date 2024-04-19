package io.metersphere.system.schedule;

import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiScheduleNoticeService {
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private UserMapper userMapper;

    public void sendScheduleNotice(Schedule schedule, String userId) {
        if (ObjectUtils.isNotEmpty(schedule)) {
            Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
            String event = NoticeConstants.Event.OPEN;
            if (BooleanUtils.isFalse(schedule.getEnable())) {
                event = NoticeConstants.Event.CLOSE;
            }
            BeanMap beanMap = new BeanMap(schedule);
            Map paramMap = new HashMap<>(beanMap);
            User user = userMapper.selectByPrimaryKey(userId);
            noticeSendService.setLanguage(user.getLanguage());
            paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user != null ? user.getName() : "");
            String template = defaultTemplateMap.get(NoticeConstants.TaskType.SCHEDULE_TASK + "_" + event);
            Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
            String subject = defaultSubjectMap.get(NoticeConstants.TaskType.SCHEDULE_TASK + "_" + event);
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(userId)
                    .context(template)
                    .subject(subject)
                    .paramMap(paramMap)
                    .event(event)
                    .excludeSelf(true)
                    .build();
            noticeSendService.send(NoticeConstants.TaskType.SCHEDULE_TASK, noticeModel);
        }
    }

    public void batchSendNotice(String projectId, List<Schedule> scheduleList, User user, String event) {
        SubListUtils.dealForSubList(scheduleList, 100, list -> {
            List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(list), Map.class));
            commonNoticeSendService.sendNotice(NoticeConstants.TaskType.SCHEDULE_TASK, event, resources, user, projectId);
        });
    }


}
