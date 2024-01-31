package io.metersphere.system.notice.sender;


import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AfterReturningNoticeSendService {
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private NoticeSendService noticeSendService;

    @Async
    public void sendNotice(String taskType, String event, List<Map> resources, SessionUser sessionUser, String currentProjectId) {

        // 有批量操作发送多次
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        for (Map resource : resources) {
            Map paramMap = new HashMap<>();
            paramMap.put("url", baseSystemConfigDTO.getUrl());
            paramMap.put(NoticeConstants.RelatedUser.OPERATOR, sessionUser.getName());
            paramMap.put("Language",sessionUser.getLanguage());
            paramMap.putAll(resource);
            paramMap.putIfAbsent("projectId", currentProjectId);
            //TODO: 加来源处理

            // 占位符
            handleDefaultValues(paramMap);

            String context = getContext(taskType, event);

            String subject = getSubject(taskType, event);

            List<String> relatedUsers = getRelatedUsers(resource.get("relatedUsers"));

            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(sessionUser.getId())
                    .context(context)
                    .subject(subject)
                    .paramMap(paramMap)
                    .event(event)
                    .status((String) paramMap.get("status"))
                    .excludeSelf(true)
                    .relatedUsers(relatedUsers)
                    .build();
            noticeSendService.send(taskType, noticeModel);
        }
    }

    private List<String> getRelatedUsers(Object relatedUsers) {
        String relatedUser = (String) relatedUsers;
        List<String> relatedUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(relatedUser)) {
            relatedUserList = Arrays.asList(relatedUser.split(";"));
        }
        return relatedUserList;
    }

    private String getSubject(String taskType, String event) {
        Map<String, String> defaultTemplateTitleMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        return defaultTemplateTitleMap.get(taskType + "_" + event);
    }


    /**
     * 有些默认的值，避免通知里出现 ${key}
     */
    private void handleDefaultValues(Map paramMap) {
        paramMap.put("planShareUrl", StringUtils.EMPTY); // 占位符
    }

    private String getContext(String taskType, String event) {
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        return defaultTemplateMap.get(taskType + "_" + event);
    }
}
