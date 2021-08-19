package io.metersphere.notice.sender;

import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.service.UserService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

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
        }
        return getContent(context, noticeModel.getParamMap());
    }

    protected String getContent(String template, Map<String, Object> context) {
        if (MapUtils.isNotEmpty(context)) {
            for (String k : context.keySet()) {
                if (context.get(k) != null) {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", context.get(k).toString());
                } else {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", "");
                }
            }
        }
        return template;
    }

    protected List<String> getUserPhones(NoticeModel noticeModel, List<String> userIds) {
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        List<String> phoneList = new ArrayList<>();
        list.forEach(u -> phoneList.add(u.getPhone()));
        LogUtil.info("收件人地址: " + phoneList);
        return phoneList.stream().distinct().collect(Collectors.toList());
    }

    protected List<String> getUserEmails(NoticeModel noticeModel, List<String> userIds) {
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        List<String> phoneList = new ArrayList<>();
        list.forEach(u -> phoneList.add(u.getEmail()));
        LogUtil.info("收件人地址: " + phoneList);
        return phoneList.stream().distinct().collect(Collectors.toList());
    }

    private List<Receiver> getRealUserIds(List<String> userIds, NoticeModel noticeModel, String event) {
        List<Receiver> toUsers = new ArrayList<>();
        Map<String, Object> paramMap = noticeModel.getParamMap();
        for (String userId : userIds) {
            switch (userId) {
                case NoticeConstants.RelatedUser.EXECUTOR:
                    if (StringUtils.equals(NoticeConstants.Event.CREATE, event)) {
                        List<String> relatedUsers = noticeModel.getRelatedUsers();
                        List<Receiver> receivers = relatedUsers.stream()
                                .map(u -> new Receiver(u, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                                .collect(Collectors.toList());
                        toUsers.addAll(receivers);
                    }
                    break;
                case NoticeConstants.RelatedUser.CREATOR:
                    Object creator = paramMap.get("creator");
                    if (creator != null) {
                        toUsers.add(new Receiver(creator.toString(), NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                    Object createUser = paramMap.get("createUser");
                    if (createUser != null) {
                        toUsers.add(new Receiver(createUser.toString(), NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                    break;
                case NoticeConstants.RelatedUser.MAINTAINER:
                    if (StringUtils.equals(NoticeConstants.Event.COMMENT, event)) {
                        List<String> relatedUsers = noticeModel.getRelatedUsers();
                        List<Receiver> receivers = relatedUsers.stream()
                                .map(u -> new Receiver(u, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                                .collect(Collectors.toList());
                        toUsers.addAll(receivers);
                    }
                    break;
                case NoticeConstants.RelatedUser.FOLLOW_PEOPLE:
                    Object followPeople = paramMap.get("followPeople");
                    if (followPeople != null) {
                        toUsers.add(new Receiver(followPeople.toString(), NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                    break;
                default:
                    toUsers.add(new Receiver(userId, NotificationConstants.Type.MENTIONED_ME.name()));
                    break;
            }
        }
        // 排除自己
        toUsers.removeIf(u -> StringUtils.equals(u.getUserId(), noticeModel.getOperator()));
        return toUsers;
    }
}
