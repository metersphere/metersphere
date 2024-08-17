package io.metersphere.system.service;


import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommonNoticeSendService {
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private NoticeSendService noticeSendService;

    @Async
    public void sendNotice(String taskType, String event, List<Map> resources, User operator, String currentProjectId) {
        setLanguage(operator.getLanguage());
        // 有批量操作发送多次
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        for (Map resource : resources) {
            Map paramMap = new HashMap<>();
            paramMap.put("url", baseSystemConfigDTO.getUrl());
            paramMap.put(NoticeConstants.RelatedUser.OPERATOR, operator.getName());
            paramMap.put("Language", operator.getLanguage());
            paramMap.putAll(resource);
            paramMap.putIfAbsent("projectId", currentProjectId);
            //TODO: 加来源处理

            // 占位符
            handleDefaultValues(paramMap);

            String context = getContext(taskType, event);

            String subject = getSubject(taskType, event);

            List<String> relatedUsers = getRelatedUsers(resource.get("relatedUsers"));

            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(operator.getId())
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

    /**
     * 发送通知
     * @param taskType
     * @param event
     * @param operator
     * @param currentProjectId
     * @param baseSystemConfig
     * @param resource
     * @param extraUsers   除了消息通知配置的用户，需要额外通知的用户
     * @param excludeSelf  是否排除自己
     */
    public void sendNotice(String taskType, String event, Map resource, User operator, String currentProjectId, BaseSystemConfigDTO baseSystemConfig,
                             List<String> extraUsers, boolean excludeSelf) {
        Map paramMap = new HashMap<>();
        paramMap.put("url", baseSystemConfig.getUrl());
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, operator.getName());
        paramMap.put("Language", operator.getLanguage());
        paramMap.putAll(resource);
        paramMap.putIfAbsent("projectId", currentProjectId);

        // 占位符
        handleDefaultValues(paramMap);

        String context = getContext(taskType, event);

        String subject = getSubject(taskType, event);

        List<String> relatedUsers = getRelatedUsers(resource.get("relatedUsers"));

        NoticeModel noticeModel = NoticeModel.builder()
                .operator(operator.getId())
                .context(context)
                .subject(subject)
                .paramMap(paramMap)
                .event(event)
                .status((String) paramMap.get("status"))
                .excludeSelf(true)
                .relatedUsers(relatedUsers)
                .build();
        noticeSendService.sendOther(taskType, noticeModel, extraUsers, excludeSelf);
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

    private static void setLanguage(String language) {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase(language, "US")) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase(language, "TW")) {
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
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
