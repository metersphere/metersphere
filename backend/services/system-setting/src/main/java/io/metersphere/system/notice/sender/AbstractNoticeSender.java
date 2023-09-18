package io.metersphere.system.notice.sender;


import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.ApiDefinitionFollowerMapper;
import io.metersphere.api.mapper.ApiScenarioFollowerMapper;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.CaseReviewFollowerMapper;
import io.metersphere.functional.mapper.FunctionalCaseFollowerMapper;
import io.metersphere.load.domain.LoadTestFollower;
import io.metersphere.load.domain.LoadTestFollowerExample;
import io.metersphere.load.mapper.LoadTestFollowerMapper;
import io.metersphere.plan.domain.TestPlanFollower;
import io.metersphere.plan.domain.TestPlanFollowerExample;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.constants.NotificationConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.text.StringSubstitutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractNoticeSender implements NoticeSender {

    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private TestPlanFollowerMapper testPlanFollowerMapper;
    @Resource
    private FunctionalCaseFollowerMapper functionalCaseFollowerMapper;
    @Resource
    private ApiScenarioFollowerMapper apiScenarioFollowerMapper;
    @Resource
    private ApiDefinitionFollowerMapper apiDefinitionFollowerMapper;
    @Resource
    private LoadTestFollowerMapper loadTestFollowerMapper;
    @Resource
    private CaseReviewFollowerMapper caseReviewFollowerMapper;
    @Resource
    private UserMapper userMapper;

    protected String getContext(MessageDetail messageDetail, NoticeModel noticeModel) {
        //处理自定义字段的值
        handleCustomFields(noticeModel);
        // 处理 userIds 中包含的特殊值
        noticeModel.setReceivers(getRealUserIds(messageDetail, noticeModel, messageDetail.getEvent()));
        // 如果配置了模版就直接使用模版
        if (StringUtils.isNotBlank(messageDetail.getTemplate())) {
            return getContent(messageDetail.getTemplate(), noticeModel.getParamMap());
        }
        String context = StringUtils.EMPTY;
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
                    CustomField customField = customFieldMapper.selectByPrimaryKey(id);
                    if (customField == null) {
                        continue;
                    }
                    Object value = jsonObject.get("value");
                    if (value instanceof String && StringUtils.isNotEmpty((String) value)) {
                        String v = StringUtils.unwrap((String) value, "\"");
                        noticeModel.getParamMap().put(customField.getName(), v); // 处理人
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
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


    private List<Receiver> getRealUserIds(MessageDetail messageDetail, NoticeModel noticeModel, String event) {
        List<Receiver> toUsers = new ArrayList<>();
        Map<String, Object> paramMap = noticeModel.getParamMap();
        for (String userId : messageDetail.getReceiverIds()) {
            switch (userId) {
                case NoticeConstants.RelatedUser.CREATE_USER -> {
                    String createUser = (String) paramMap.get(NoticeConstants.RelatedUser.CREATE_USER);
                    if (StringUtils.isNotBlank(createUser)) {
                        toUsers.add(new Receiver(createUser, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                }
                case NoticeConstants.RelatedUser.OPERATOR -> {
                    String operator = (String) paramMap.get(NoticeConstants.RelatedUser.OPERATOR);
                    if (StringUtils.isNotBlank(operator)) {
                        toUsers.add(new Receiver(operator, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                }
                case NoticeConstants.RelatedUser.FOLLOW_PEOPLE -> {
                    try {
                        List<Receiver> follows = handleFollows(messageDetail, noticeModel);
                        toUsers.addAll(follows);
                    } catch (Exception e) {
                        LogUtils.error("查询关注人失败: ", e);
                    }
                }
                default -> toUsers.add(new Receiver(userId, NotificationConstants.Type.MENTIONED_ME.name()));
            }
            //TODO：接口同步时通知的接收人特殊处理（v2接口同步的通知，v3这里待讨论）
            //处理评论人
            if (messageDetail.getTaskType().contains("AT_COMMENT")) {
                if (CollectionUtils.isNotEmpty(noticeModel.getRelatedUsers())) {
                    for (String relatedUser : noticeModel.getRelatedUsers()) {
                        toUsers.add(new Receiver(relatedUser, NotificationConstants.Type.MENTIONED_ME.name()));
                    }
                }
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
            case NoticeConstants.TaskType.TEST_PLAN_TASK -> {
                TestPlanFollowerExample testPlanFollowerExample = new TestPlanFollowerExample();
                testPlanFollowerExample.createCriteria().andTestPlanIdEqualTo(id);
                List<TestPlanFollower> testPlanFollowers = testPlanFollowerMapper.selectByExample(testPlanFollowerExample);
                receivers = testPlanFollowers
                        .stream()
                        .map(t -> new Receiver(t.getUserId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
            }
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> {
                CaseReviewFollowerExample caseReviewFollowerExample = new CaseReviewFollowerExample();
                caseReviewFollowerExample.createCriteria().andReviewIdEqualTo(id);
                List<CaseReviewFollower> caseReviewFollowers = caseReviewFollowerMapper.selectByExample(caseReviewFollowerExample);
                receivers = caseReviewFollowers
                        .stream()
                        .map(t -> new Receiver(t.getUserId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
            }
            case NoticeConstants.TaskType.API_SCENARIO_TASK -> {
                ApiScenarioFollowerExample apiScenarioFollowerExample = new ApiScenarioFollowerExample();
                apiScenarioFollowerExample.createCriteria().andApiScenarioIdEqualTo(id);
                List<ApiScenarioFollower> apiScenarioFollowers = apiScenarioFollowerMapper.selectByExample(apiScenarioFollowerExample);
                receivers = apiScenarioFollowers
                        .stream()
                        .map(t -> new Receiver(t.getUserId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
            }
            case NoticeConstants.TaskType.API_DEFINITION_TASK -> {
                ApiDefinitionFollowerExample apiDefinitionFollowerExample = new ApiDefinitionFollowerExample();
                apiDefinitionFollowerExample.createCriteria().andApiDefinitionIdEqualTo(id);
                List<ApiDefinitionFollower> apiDefinitionFollowers = apiDefinitionFollowerMapper.selectByExample(apiDefinitionFollowerExample);
                receivers = apiDefinitionFollowers
                        .stream()
                        .map(t -> new Receiver(t.getUserId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
            }
            case NoticeConstants.TaskType.LOAD_TEST_TASK -> {
                LoadTestFollowerExample loadTestFollowerExample = new LoadTestFollowerExample();
                loadTestFollowerExample.createCriteria().andTestIdEqualTo(id);
                List<LoadTestFollower> loadTestFollowers = loadTestFollowerMapper.selectByExample(loadTestFollowerExample);
                receivers = loadTestFollowers
                        .stream()
                        .map(t -> new Receiver(t.getUserId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
            }
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> {
                FunctionalCaseFollowerExample functionalCaseFollowerExample = new FunctionalCaseFollowerExample();
                functionalCaseFollowerExample.createCriteria().andCaseIdEqualTo(id);
                List<FunctionalCaseFollower> functionalCaseFollowers = functionalCaseFollowerMapper.selectByExample(functionalCaseFollowerExample);
                receivers = functionalCaseFollowers
                        .stream()
                        .map(t -> new Receiver(t.getUserId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
            }
            default -> {
            }
        }
        LogUtils.info("FOLLOW_PEOPLE: {}", receivers);
        return receivers;
    }

    protected List<User> getUsers(List<String> userIds) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        return userMapper.selectByExample(userExample);
    }
}
