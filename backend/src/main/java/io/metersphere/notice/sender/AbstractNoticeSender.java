package io.metersphere.notice.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractNoticeSender implements NoticeSender {
    @Resource
    private UserService userService;

    protected String getContext(MessageDetail messageDetail, NoticeModel noticeModel) {

        // 处理 userIds 中包含的特殊值
        noticeModel.setReceivers(getRealUserIds(messageDetail.getUserIds(), noticeModel, messageDetail.getEvent()));

        // 如果配置了模版就直接使用模版
        if (StringUtils.isNotBlank(messageDetail.getTemplate())) {
            return getContent(messageDetail.getTemplate(), noticeModel.getParamMap());
        }
        // 处理 WeCom Ding context
        String context = "";
        switch (messageDetail.getEvent()) {
            case NoticeConstants.Event.EXECUTE_FAILED:
                context = noticeModel.getFailedContext();
                break;
            case NoticeConstants.Event.EXECUTE_SUCCESSFUL:
                context = noticeModel.getSuccessContext();
                break;
            default:
                context = noticeModel.getContext();
                break;
        }
        // 兼容
        if (StringUtils.isBlank(context)) {
            context = noticeModel.getContext();
        }
        return getContent(context, noticeModel.getParamMap());
    }

    protected String getHtmlContext(MessageDetail messageDetail, NoticeModel noticeModel) {
        // 处理 userIds 中包含的特殊值
        noticeModel.setReceivers(getRealUserIds(messageDetail.getUserIds(), noticeModel, messageDetail.getEvent()));

        // 如果配置了模版就直接使用模版
        if (StringUtils.isNotBlank(messageDetail.getTemplate())) {
            return getContent(messageDetail.getTemplate(), noticeModel.getParamMap());
        }

        // 处理 mail context
        String context = "";
        try {
            switch (messageDetail.getEvent()) {
                case NoticeConstants.Event.EXECUTE_FAILED:
                    URL resource1 = this.getClass().getResource("/mail/" + noticeModel.getFailedMailTemplate() + ".html");
                    context = IOUtils.toString(resource1, StandardCharsets.UTF_8);
                    break;
                case NoticeConstants.Event.EXECUTE_SUCCESSFUL:
                    URL resource2 = this.getClass().getResource("/mail/" + noticeModel.getSuccessMailTemplate() + ".html");
                    context = IOUtils.toString(resource2, StandardCharsets.UTF_8);
                    break;
                default:
                    URL resource3 = this.getClass().getResource("/mail/" + noticeModel.getMailTemplate() + ".html");
                    context = IOUtils.toString(resource3, StandardCharsets.UTF_8);
                    break;
            }
        } catch (IOException e) {
            LogUtil.error(e);
            // 兼容
            try {
                URL resource3 = this.getClass().getResource("/mail/" + noticeModel.getMailTemplate() + ".html");
                context = IOUtils.toString(resource3, StandardCharsets.UTF_8);
            } catch (IOException ex) {
            }
        }
        return getContent(context, noticeModel.getParamMap());
    }

    protected String getContent(String template, Map<String, Object> context) {
        if (MapUtils.isNotEmpty(context)) {
            for (String k : context.keySet()) {
                if (context.get(k) != null) {
                    String value = handleTime(k, context);
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", value);
                } else {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", "");
                }
            }
        }
        return template;
    }

    private String handleTime(String k, Map<String, Object> context) {
        String value = context.get(k).toString();
        if (StringUtils.endsWithIgnoreCase(k, "Time")) {
            try {
                long time = Long.parseLong(value);
                value = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
            } catch (Exception ignore) {
            }
        }
        return value;
    }

    protected List<UserDetail> getUserDetails(List<String> userIds) {
        return userService.queryTypeByIds(userIds);
    }

    private List<Receiver> getRealUserIds(List<String> userIds, NoticeModel noticeModel, String event) {
        List<Receiver> toUsers = new ArrayList<>();
        Map<String, Object> paramMap = noticeModel.getParamMap();
        for (String userId : userIds) {
            switch (userId) {
                case NoticeConstants.RelatedUser.EXECUTOR:
                    if (StringUtils.equals(NoticeConstants.Event.CREATE, event)) {
                        List<String> relatedUsers = (List<String>) paramMap.get("userIds");
                        if (CollectionUtils.isNotEmpty(relatedUsers)) {
                            List<Receiver> receivers = relatedUsers.stream()
                                    .map(u -> new Receiver(u, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                                    .collect(Collectors.toList());
                            toUsers.addAll(receivers);
                        }
                    }
                    break;
                case NoticeConstants.RelatedUser.CREATOR:
                    String creator = (String) paramMap.get("creator");
                    String createUser = (String) paramMap.get("createUser");
                    String createUserId = (String) paramMap.get("createUserId");
                    String userId1 = (String) paramMap.get("userId");

                    if (StringUtils.isNotBlank(userId1)) {
                        toUsers.add(new Receiver(userId1, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    } else if (StringUtils.isNotBlank(creator)) {
                        toUsers.add(new Receiver(creator, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    } else if (StringUtils.isNotBlank(createUser)) {
                        toUsers.add(new Receiver(createUser, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    } else if (StringUtils.isNotBlank(createUserId)) {
                        toUsers.add(new Receiver(createUserId, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                    break;
                case NoticeConstants.RelatedUser.MAINTAINER:
                    if (StringUtils.equals(NoticeConstants.Event.COMMENT, event)) {
                        List<String> relatedUsers = (List<String>) paramMap.get("userIds");
                        if (CollectionUtils.isNotEmpty(relatedUsers)) {
                            List<Receiver> receivers = relatedUsers.stream()
                                    .map(u -> new Receiver(u, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                                    .collect(Collectors.toList());
                            toUsers.addAll(receivers);
                        }
                    }
                    break;
                case NoticeConstants.RelatedUser.FOLLOW_PEOPLE:
                    String followPeople = (String) paramMap.get("followPeople");
                    if (StringUtils.isNotBlank(followPeople)) {
                        toUsers.add(new Receiver(followPeople, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                    break;
                case NoticeConstants.RelatedUser.PROCESSOR:
                    String customFields = (String) paramMap.get("customFields");
                    JSONArray array = JSON.parseArray(customFields);
                    for (Object o : array) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        if (StringUtils.equals(jsonObject.getString("name"), "处理人")) {
                            String processor = jsonObject.getString("value");
                            toUsers.add(new Receiver(processor, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                            break;
                        }
                    }
                    break;
                default:
                    toUsers.add(new Receiver(userId, NotificationConstants.Type.MENTIONED_ME.name()));
                    break;
            }
        }
        // 去重复
        return toUsers.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
