package io.metersphere.notice.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.TestCaseReview;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.UserService;
import io.metersphere.track.service.TestCaseReviewService;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.context.annotation.Lazy;

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
    @Resource
    @Lazy
    private PerformanceTestService performanceTestService;
    @Resource
    @Lazy
    private ApiAutomationService apiAutomationService;
    @Resource
    @Lazy
    private ApiDefinitionService apiDefinitionService;
    @Resource
    @Lazy
    private ApiTestCaseService apiTestCaseService;
    @Resource
    @Lazy
    private TestCaseService testCaseService;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    @Lazy
    private TestCaseReviewService testCaseReviewService;

    protected String getContext(MessageDetail messageDetail, NoticeModel noticeModel) {

        // 处理 userIds 中包含的特殊值
        noticeModel.setReceivers(getRealUserIds(messageDetail, noticeModel, messageDetail.getEvent()));

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
        noticeModel.setReceivers(getRealUserIds(messageDetail, noticeModel, messageDetail.getEvent()));

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
        // 处理 null
        context.forEach((k, v) -> {
            if (v == null) {
                context.put(k, "");
            }
        });
        // 处理时间格式的数据
        handleTime(context);
        StringSubstitutor sub = new StringSubstitutor(context);
        return sub.replace(template);
    }

    private void handleTime(Map<String, Object> context) {
        context.forEach((k, v) -> {
            if (StringUtils.endsWithIgnoreCase(k, "Time")) {
                try {
                    String value = v.toString();
                    long time = Long.parseLong(value);
                    v = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
                    context.put(k, v);
                } catch (Exception ignore) {
                }
            }
        });

    }

    protected List<UserDetail> getUserDetails(List<String> userIds) {
        return userService.queryTypeByIds(userIds);
    }

    private List<Receiver> getRealUserIds(MessageDetail messageDetail, NoticeModel noticeModel, String event) {
        List<Receiver> toUsers = new ArrayList<>();
        Map<String, Object> paramMap = noticeModel.getParamMap();
        for (String userId : messageDetail.getUserIds()) {
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
                    toUsers.addAll(handleFollows(messageDetail, noticeModel));
                    break;
                case NoticeConstants.RelatedUser.PROCESSOR:
                    String customFields = (String) paramMap.get("customFields");
                    JSONArray array = JSON.parseArray(customFields);
                    for (Object o : array) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String name = jsonObject.getString("name");
                        Object value = jsonObject.getObject("value", Object.class);
                        paramMap.put(name, value); // 处理人
                        if (StringUtils.equals(jsonObject.getString("name"), "处理人")) {
                            paramMap.put("processor", value); // 处理人
                            toUsers.add(new Receiver(value.toString(), NotificationConstants.Type.SYSTEM_NOTICE.name()));
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

    private List<Receiver> handleFollows(MessageDetail messageDetail, NoticeModel noticeModel) {
        List<Receiver> receivers = new ArrayList<>();
        String id = (String) noticeModel.getParamMap().get("id");
        String taskType = messageDetail.getTaskType();
        switch (taskType) {
            case NoticeConstants.TaskType.TEST_PLAN_TASK:
                receivers = testPlanService.getPlanFollow(id)
                        .stream()
                        .map(user -> new Receiver(user.getId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.REVIEW_TASK:
                TestCaseReview request = new TestCaseReview();
                request.setId(id);
                receivers = testCaseReviewService.getFollowByReviewId(request).stream()
                        .map(user -> new Receiver(user.getId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.API_AUTOMATION_TASK:
                receivers = apiAutomationService.getFollows(id)
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.API_DEFINITION_TASK:
                receivers = apiDefinitionService.getFollows(id)
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                List<Receiver> caseFollows = apiTestCaseService.getFollows(id)
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                receivers.addAll(caseFollows);
                break;
            case NoticeConstants.TaskType.PERFORMANCE_TEST_TASK:
                receivers = performanceTestService.getFollows(id)
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.TRACK_TEST_CASE_TASK:
                receivers = testCaseService.getFollows(id)
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            default:
                break;
        }
        LogUtil.info("FOLLOW_PEOPLE: {}", receivers);
        return receivers;
    }
}
