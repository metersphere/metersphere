package io.metersphere.notice.sender;

import com.fasterxml.jackson.core.type.TypeReference;
import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.TestCaseReview;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.BaseUserMapper;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.HttpHeaderUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.service.BaseCustomFieldService;
import io.metersphere.service.MicroService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.text.StringSubstitutor;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractNoticeSender implements NoticeSender {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private MicroService microService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;

    protected String getContext(MessageDetail messageDetail, NoticeModel noticeModel) {
        // 如果有自定义字段
        handleCustomFields(noticeModel);

        // 处理 userIds 中包含的特殊值
        noticeModel.setReceivers(getRealUserIds(messageDetail, noticeModel, messageDetail.getEvent()));
        //apiReceiver特殊处理
        String apiSpecialType = (String) noticeModel.getParamMap().get("apiSpecialType");
        if (apiSpecialType != null && apiSpecialType.equals("API_SPECIAL")) {
            String specialReceivers = (String) noticeModel.getParamMap().get("specialReceivers");
            List list = JSON.parseArray(specialReceivers);
            if (CollectionUtils.isNotEmpty(list)) {
                for (Object o : list) {
                    noticeModel.getReceivers().add(new Receiver(o.toString(), NotificationConstants.Type.MENTIONED_ME.name()));
                }
            }
        }

        // 如果配置了模版就直接使用模版
        if (StringUtils.isNotBlank(messageDetail.getTemplate())) {
            return getContent(messageDetail.getTemplate(), noticeModel.getParamMap());
        }
        // 处理 WeCom Ding context
        String context = StringUtils.EMPTY;
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

    private void handleCustomFields(NoticeModel noticeModel) {
        if (!noticeModel.getParamMap().containsKey("fields")) {
            return;
        }
        try {
            Object customFields = noticeModel.getParamMap().get("fields");
            List<Object> fields;
            if (customFields instanceof String) {
                fields = JSON.parseArray((String) customFields, Object.class);
            } else {
                fields = (List<Object>) customFields;
            }
            if (CollectionUtils.isNotEmpty(fields)) {
                for (Object o : fields) {
                    Map jsonObject = new BeanMap(o);
                    String id = (String) jsonObject.get("id");
                    CustomField customField = baseCustomFieldService.get(id);
                    if (customField == null) {
                        continue;
                    }
                    Object value = jsonObject.get("value");
                    if (value instanceof String && StringUtils.isNotEmpty((String) value)) {
                        String v = StringUtils.unwrap((String) value, "\"");
                        noticeModel.getParamMap().put(customField.getName(), v); // 处理人
                        if (StringUtils.equals(customField.getName(), "处理人")) {
                            noticeModel.getParamMap().put("processor", v); // 处理人
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    protected String getContent(String template, Map<String, Object> context) {
        // 处理 null
        context.forEach((k, v) -> {
            if (v == null) {
                context.put(k, StringUtils.EMPTY);
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
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return baseUserMapper.queryTypeByIds(userIds);
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
                    if (paramMap.containsKey("executor")) {
                        toUsers.add(new Receiver((String) paramMap.get("executor"), NotificationConstants.Type.SYSTEM_NOTICE.name()));
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

                        if (paramMap.containsKey("maintainer")) {
                            toUsers.add(new Receiver((String) paramMap.get("maintainer"), NotificationConstants.Type.SYSTEM_NOTICE.name()));
                        }
                    }
                    break;
                case NoticeConstants.RelatedUser.FOLLOW_PEOPLE:
                    try {
                        List<Receiver> follows = handleFollows(messageDetail, noticeModel);
                        toUsers.addAll(follows);
                    } catch (Exception e) {
                        LogUtil.error("查询关注人失败: ", e);
                    }
                    break;
                case NoticeConstants.RelatedUser.PROCESSOR:
                    Object value = paramMap.get("processor"); // 处理人
                    if (!Objects.isNull(value)) {
                        toUsers.add(new Receiver(value.toString(), NotificationConstants.Type.SYSTEM_NOTICE.name()));
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
        List<String> follows;
        List<User> followUsers;
        String id = (String) noticeModel.getParamMap().get("id");
        String taskType = messageDetail.getTaskType();
        String operator = noticeModel.getOperator();
        HttpHeaderUtils.runAsUser(userMapper.selectByPrimaryKey(operator));
        switch (taskType) {
            case NoticeConstants.TaskType.TEST_PLAN_TASK:
                followUsers = microService.getForDataArray(MicroServiceName.TEST_TRACK, "/test/plan/follow/" + id, User.class);
                receivers = followUsers
                        .stream()
                        .map(user -> new Receiver(user.getId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.REVIEW_TASK:
                TestCaseReview request = new TestCaseReview();
                request.setId(id);
                followUsers = microService.postForDataArray(MicroServiceName.TEST_TRACK, "/test/case/review/follow", request, User.class);
                receivers = followUsers
                        .stream()
                        .map(user -> new Receiver(user.getId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.API_AUTOMATION_TASK:
                follows = microService.getForDataArray(MicroServiceName.API_TEST, "/api/automation/follow/" + id, String.class);
                receivers = follows
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.API_DEFINITION_TASK:
                follows = microService.getForDataArray(MicroServiceName.API_TEST, "/api/definition/follow/" + id, String.class);
                receivers = follows
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                follows = microService.getForDataArray(MicroServiceName.API_TEST, "/api/testcase/follow/" + id, String.class);
                List<Receiver> caseFollows = follows
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                receivers.addAll(caseFollows);
                break;
            case NoticeConstants.TaskType.PERFORMANCE_TEST_TASK:
                follows = microService.getForDataArray(MicroServiceName.PERFORMANCE_TEST, "/performance/test/follow/" + id, String.class);
                receivers = follows
                        .stream()
                        .map(userId -> new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
                break;
            case NoticeConstants.TaskType.TRACK_TEST_CASE_TASK:
                follows = microService.getForDataArray(MicroServiceName.TEST_TRACK, "/test/case/follow/" + id, String.class);
                receivers = follows
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
