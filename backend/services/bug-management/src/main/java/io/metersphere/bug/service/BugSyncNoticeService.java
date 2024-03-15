package io.metersphere.bug.service;

import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugSyncNoticeService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private NoticeSendService noticeSendService;

    public void sendNotice(int total, String currentUser, String projectId) {
        User user = userMapper.selectByPrimaryKey(currentUser);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(NoticeConstants.TemplateText.BUG_SYNC_TASK_EXECUTE_COMPLETED);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TemplateText.BUG_SYNC_TASK_EXECUTE_COMPLETED);
        // ${OPERATOR}同步了${total}条缺陷
        Map paramMap = new HashMap<>();
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        paramMap.put("total", total);
        paramMap.put("projectId", projectId);
        NoticeModel noticeModel = NoticeModel.builder().operator(currentUser)
                .context(template).subject(subject).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_COMPLETED).build();
        noticeSendService.send(NoticeConstants.TaskType.BUG_SYNC_TASK, noticeModel);
    }
}
