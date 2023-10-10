package io.metersphere.system.notice.sender;


import io.metersphere.sdk.dto.BaseSystemConfigDTO;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AfterReturningNoticeSendService {
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private NoticeSendService noticeSendService;

    @Async
    public void sendNotice(SendNotice sendNotice, List<Map> resources, SessionUser sessionUser, String currentProjectId) {

        // 有批量操作发送多次
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        for (Map resource : resources) {
            Map paramMap = new HashMap<>();
            paramMap.put("url", baseSystemConfigDTO.getUrl());
            paramMap.put("operator", sessionUser.getName());
            paramMap.putAll(resource);
            paramMap.putIfAbsent("projectId", currentProjectId);
            // 占位符
            handleDefaultValues(paramMap);

            String context = getContext(sendNotice);

            String subject = getSubject(sendNotice);

            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(sessionUser.getId())
                    .context(context)
                    .subject(subject)
                    .paramMap(paramMap)
                    .event(sendNotice.event())
                    .status((String) paramMap.get("status"))
                    .excludeSelf(true)
                    .build();
            noticeSendService.send(sendNotice.taskType(), noticeModel);
        }
    }

    private String getSubject(SendNotice sendNotice) {
        Map<String, String> defaultTemplateTitleMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        return defaultTemplateTitleMap.get(sendNotice.taskType() + "_" + sendNotice.event());
    }

    /**
     * 有些默认的值，避免通知里出现 ${key}
     */
    private void handleDefaultValues(Map paramMap) {
        paramMap.put("planShareUrl", StringUtils.EMPTY); // 占位符
    }

    private String getContext(SendNotice sendNotice) {
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        return defaultTemplateMap.get(sendNotice.taskType() + "_" + sendNotice.event());
    }
}
