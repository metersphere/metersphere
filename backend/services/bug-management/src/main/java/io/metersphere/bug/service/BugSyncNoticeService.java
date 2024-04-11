package io.metersphere.bug.service;

import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugSyncNoticeService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private NoticeSendService noticeSendService;

    @Resource
    private ProjectApplicationService projectApplicationService;

    public void sendNotice(int total, String currentUser, String language, String triggerMode, String projectId) {
        String platformName = projectApplicationService.getPlatformName(projectId);
        User user = userMapper.selectByPrimaryKey(currentUser);
        setLanguage(user.getLanguage());
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(NoticeConstants.TemplateText.BUG_SYNC_TASK_EXECUTE_COMPLETED);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TemplateText.BUG_SYNC_TASK_EXECUTE_COMPLETED);
        // ${OPERATOR}同步了${total}条缺陷
        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        paramMap.put("total", total);
        paramMap.put("projectId", projectId);
        paramMap.put("Language", language);
        paramMap.put("platform", platformName);
        paramMap.put("triggerMode", triggerMode);
        NoticeModel noticeModel = NoticeModel.builder().operator(currentUser).excludeSelf(false)
                .context(template).subject(subject).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_COMPLETED).build();
        noticeSendService.send(NoticeConstants.TaskType.BUG_SYNC_TASK, noticeModel);
    }

    private static void setLanguage(String language) {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase("US",language)) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase("TW",language)){
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }
}
