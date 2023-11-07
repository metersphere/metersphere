package io.metersphere.system.notice.sender;


import io.metersphere.api.domain.ApiDefinitionFollower;
import io.metersphere.api.domain.ApiDefinitionFollowerExample;
import io.metersphere.api.domain.ApiScenarioFollower;
import io.metersphere.api.domain.ApiScenarioFollowerExample;
import io.metersphere.api.mapper.ApiDefinitionFollowerMapper;
import io.metersphere.api.mapper.ApiScenarioFollowerMapper;
import io.metersphere.functional.domain.CaseReviewFollower;
import io.metersphere.functional.domain.CaseReviewFollowerExample;
import io.metersphere.functional.domain.FunctionalCaseFollower;
import io.metersphere.functional.domain.FunctionalCaseFollowerExample;
import io.metersphere.functional.mapper.CaseReviewFollowerMapper;
import io.metersphere.functional.mapper.FunctionalCaseFollowerMapper;
import io.metersphere.load.domain.LoadTestFollower;
import io.metersphere.load.domain.LoadTestFollowerExample;
import io.metersphere.load.mapper.LoadTestFollowerMapper;
import io.metersphere.plan.domain.TestPlanFollower;
import io.metersphere.plan.domain.TestPlanFollowerExample;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.constants.NotificationConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
            return MessageTemplateUtils.getContent(messageDetail.getTemplate(), noticeModel.getParamMap());
        }
        String context = StringUtils.EMPTY;
        if (StringUtils.isBlank(context)) {
            context = noticeModel.getContext();
        }
        return MessageTemplateUtils.getContent(context, noticeModel.getParamMap());
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
                    String jsonFields = JSON.toJSONString(o);
                    Map<?, ?> jsonObject = JSON.parseObject(jsonFields, Map.class);
                    String customFieldName = (String) jsonObject.get("id");
                    Object value = jsonObject.get("name");
                    if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                        String v = StringUtils.unwrap((String) value, "\"");
                        noticeModel.getParamMap().put(customFieldName, v); // 处理人
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    private List<Receiver> getRealUserIds(MessageDetail messageDetail, NoticeModel noticeModel, String event) {
        List<Receiver> toUsers = new ArrayList<>();
        Map<String, Object> paramMap = noticeModel.getParamMap();
        for (String userId : messageDetail.getReceiverIds()) {
            switch (userId) {
                case NoticeConstants.RelatedUser.CREATE_USER -> {
                    String createUser = (String) paramMap.get("createUser");
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

        }
        //处理评论人
        if (event.contains(NoticeConstants.Event.AT) || event.contains(NoticeConstants.Event.REPLY)) {
            if (CollectionUtils.isNotEmpty(noticeModel.getRelatedUsers())) {
                for (String relatedUser : noticeModel.getRelatedUsers()) {
                    toUsers.add(new Receiver(relatedUser, NotificationConstants.Type.MENTIONED_ME.name()));
                }
            }
        }

        // 去重复
        List<String> userIds = toUsers.stream().map(Receiver::getUserId).distinct().toList();
        LogUtils.error("userIds: ", JSON.toJSONString(userIds));
        List<User> users = getUsers(userIds);
        List<String> realUserIds = users.stream().map(User::getId).toList();
        return toUsers.stream().filter(t -> realUserIds.contains(t.getUserId())).toList();
    }

    private List<Receiver> handleFollows(MessageDetail messageDetail, NoticeModel noticeModel) {
        List<Receiver> receivers = new ArrayList<>();
        String id = (String) noticeModel.getParamMap().get("id");
        if (StringUtils.isBlank(id)) {
            return receivers;
        }
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
        if (CollectionUtils.isNotEmpty(userIds)) {
            userExample.createCriteria().andIdIn(userIds);
            return userMapper.selectByExample(userExample);
        }
        return new ArrayList<>();
    }
}
