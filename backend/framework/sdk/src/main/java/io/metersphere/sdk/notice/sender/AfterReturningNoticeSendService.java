package io.metersphere.sdk.notice.sender;


import io.metersphere.sdk.dto.BaseSystemConfigDTO;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.notice.annotation.SendNotice;
import io.metersphere.sdk.notice.constants.NoticeConstants;
import io.metersphere.sdk.notice.NoticeModel;
import io.metersphere.sdk.service.NoticeSendService;
import io.metersphere.sdk.service.SystemParameterService;
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

            String context = getContext(sendNotice, paramMap);

            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(sessionUser.getId())
                    .context(context)
                    .subject(sendNotice.subject())
                    .paramMap(paramMap)
                    .event(sendNotice.event())
                    .status((String) paramMap.get("status"))
                    .excludeSelf(true)
                    .build();
            noticeSendService.send(sendNotice.taskType(), noticeModel);
        }
    }

    /**
     * 有些默认的值，避免通知里出现 ${key}
     */
    private void handleDefaultValues(Map paramMap) {
        paramMap.put("planShareUrl", StringUtils.EMPTY); // 占位符
    }

    private String getContext(SendNotice sendNotice, Map<String, Object> paramMap) {
        String operation = "";
        switch (sendNotice.event()) {
            case NoticeConstants.Event.CREATE:
            case NoticeConstants.Event.CASE_CREATE:
            case NoticeConstants.Event.MOCK_CREATE:
                operation = "创建了";
                break;
            case NoticeConstants.Event.UPDATE:
            case NoticeConstants.Event.CASE_UPDATE:
            case NoticeConstants.Event.MOCK_UPDATE:
                operation = "更新了";
                break;
            case NoticeConstants.Event.DELETE:
            case NoticeConstants.Event.CASE_DELETE:
            case NoticeConstants.Event.MOCK_DELETE:
                operation = "删除了";
                break;
            case NoticeConstants.Event.COMMENT:
                operation = "评论了";
                break;
            case NoticeConstants.Event.COMPLETE:
                operation = "完成了";
                break;
            case NoticeConstants.Event.CLOSE_SCHEDULE:
                operation = "关闭了定时任务";
                break;
            default:
                break;
        }
        String subject = sendNotice.subject();
        String resource = StringUtils.removeEnd(subject, "通知");

        String name = "";
        if (paramMap.containsKey("name")) {
            name = ": ${name}";
        }
        if (paramMap.containsKey("title")) {
            name = ": ${title}";
        }
        return "${operator}" + operation + resource + name;
    }
}
