package io.metersphere.system.notice.sender;


import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.*;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugFollower;
import io.metersphere.bug.domain.BugFollowerExample;
import io.metersphere.bug.mapper.BugFollowerMapper;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.CaseReviewFollowerMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.FunctionalCaseFollowerMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanFollower;
import io.metersphere.plan.domain.TestPlanFollowerExample;
import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.mapper.TestPlanReportMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.ScheduleMapper;
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
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractNoticeSender implements NoticeSender {

    @Resource
    private BugFollowerMapper bugFollowerMapper;
    @Resource
    private BugMapper bugMapper;
    @Resource
    private TestPlanFollowerMapper testPlanFollowerMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private FunctionalCaseFollowerMapper functionalCaseFollowerMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private ApiScenarioFollowerMapper apiScenarioFollowerMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionFollowerMapper apiDefinitionFollowerMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private CaseReviewFollowerMapper caseReviewFollowerMapper;
    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;


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

    protected String getSubjectText(MessageDetail messageDetail, NoticeModel noticeModel) {
        // 如果配置了模版就直接使用模版
        if (StringUtils.isNotBlank(messageDetail.getSubject())) {
            return MessageTemplateUtils.getContent(messageDetail.getSubject(), noticeModel.getParamMap());
        }
        String context = StringUtils.EMPTY;
        if (StringUtils.isBlank(context)) {
            context = noticeModel.getSubject();
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
                    } else {
                        Receiver receiver = handleCreateUser(messageDetail, noticeModel);
                        toUsers.add(Objects.requireNonNullElseGet(receiver, () -> new Receiver((String) paramMap.get(NoticeConstants.RelatedUser.OPERATOR), NotificationConstants.Type.SYSTEM_NOTICE.name())));
                    }
                }
                case NoticeConstants.RelatedUser.OPERATOR -> {
                    String operator = (String) paramMap.get(NoticeConstants.RelatedUser.OPERATOR);
                    if (StringUtils.isNotBlank(operator)) {
                        toUsers.add(new Receiver(operator, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    }
                }
                // 处理人(缺陷)
                case NoticeConstants.RelatedUser.HANDLE_USER -> {
                    String handleUser = (String) paramMap.get(NoticeConstants.RelatedUser.HANDLE_USER);
                    if (StringUtils.isNotBlank(handleUser)) {
                        toUsers.add(new Receiver(handleUser, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                    } else {
                        Receiver receiver = handleHandler(messageDetail, noticeModel);
                        toUsers.add(receiver);
                    }
                }
                case NoticeConstants.RelatedUser.FOLLOW_PEOPLE -> {
                    try {
                        List<String> followUser = (List) paramMap.get("followUsers");
                        if (CollectionUtils.isNotEmpty(followUser)) {
                            followUser.forEach(item ->{
                                toUsers.add(new Receiver(item, NotificationConstants.Type.SYSTEM_NOTICE.name()));
                            });
                        } else {
                            List<Receiver> follows = handleFollows(messageDetail, noticeModel);
                            toUsers.addAll(follows);
                        }
                    } catch (Exception e) {
                        LogUtils.error("查询关注人失败：{}", e);
                    }
                }
                default -> toUsers.add(new Receiver(userId, NotificationConstants.Type.SYSTEM_NOTICE.name()));
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
        List<String> userIds = toUsers.stream().map(Receiver::getUserId).toList();
        LogUtils.info("userIds: ", JSON.toJSONString(userIds));
        List<User> users = getUsers(userIds, messageDetail.getProjectId(), false);
        List<String> realUserIds = users.stream().map(User::getId).distinct().toList();
        return toUsers.stream().filter(t -> realUserIds.contains(t.getUserId())).distinct().toList();
    }

    private Receiver handleCreateUser(MessageDetail messageDetail, NoticeModel noticeModel) {
        String id = (String) noticeModel.getParamMap().get("id");
        if (StringUtils.isBlank(id)) {
            return null;
        }
        String taskType = messageDetail.getTaskType();

        Receiver receiver = null;
        switch (taskType) {
            case NoticeConstants.TaskType.TEST_PLAN_TASK -> {
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
                if (testPlan != null) {
                    receiver = new Receiver(testPlan.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> {
                CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(id);
                if (caseReview != null) {
                    receiver = new Receiver(caseReview.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.API_SCENARIO_TASK -> {
                ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
                if (apiScenario != null) {
                    receiver = new Receiver(apiScenario.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.API_DEFINITION_TASK -> {
                ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
                if (apiDefinition != null) {
                    receiver = new Receiver(apiDefinition.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> {
                FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
                if (functionalCase != null) {
                    receiver = new Receiver(functionalCase.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.BUG_TASK -> {
                Bug bug = bugMapper.selectByPrimaryKey(id);
                if (bug != null) {
                    receiver = new Receiver(bug.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.API_REPORT_TASK -> {
                ApiReport apiReport = apiReportMapper.selectByPrimaryKey(id);
                if (apiReport != null) {
                    receiver = new Receiver(apiReport.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
                ApiScenarioReport scenarioReport = apiScenarioReportMapper.selectByPrimaryKey(id);
                if (scenarioReport != null) {
                    receiver = new Receiver(scenarioReport.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.SCHEDULE_TASK -> {
                Schedule schedule = scheduleMapper.selectByPrimaryKey(id);
                if (schedule != null) {
                    receiver = new Receiver(schedule.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            case NoticeConstants.TaskType.TEST_PLAN_REPORT_TASK -> {
                TestPlanReport report = testPlanReportMapper.selectByPrimaryKey(id);
                if (report != null) {
                    receiver = new Receiver(report.getCreateUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
                }
            }
            default -> {
            }
        }
        return receiver;
    }

    /**
     * 处理人字段
     *
     * @param messageDetail
     * @param noticeModel
     * @return 通知接收人
     */
    private Receiver handleHandler(MessageDetail messageDetail, NoticeModel noticeModel) {
        String id = (String) noticeModel.getParamMap().get("id");
        if (StringUtils.isBlank(id)) {
            return null;
        }
        Receiver receiver = null;
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug != null && StringUtils.equals(bug.getPlatform(), "Local")) {
            // 本地缺陷的处理人才需要通知
            receiver = new Receiver(bug.getHandleUser(), NotificationConstants.Type.SYSTEM_NOTICE.name());
        }
        return receiver;
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
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> {
                FunctionalCaseFollowerExample functionalCaseFollowerExample = new FunctionalCaseFollowerExample();
                functionalCaseFollowerExample.createCriteria().andCaseIdEqualTo(id);
                List<FunctionalCaseFollower> functionalCaseFollowers = functionalCaseFollowerMapper.selectByExample(functionalCaseFollowerExample);
                receivers = functionalCaseFollowers
                        .stream()
                        .map(t -> new Receiver(t.getUserId(), NotificationConstants.Type.SYSTEM_NOTICE.name()))
                        .collect(Collectors.toList());
            }
            case NoticeConstants.TaskType.BUG_TASK -> {
                BugFollowerExample bugFollowerExample = new BugFollowerExample();
                bugFollowerExample.createCriteria().andBugIdEqualTo(id);
                List<BugFollower> bugFollowers = bugFollowerMapper.selectByExample(bugFollowerExample);
                receivers = bugFollowers
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

    protected List<User> getUsers(List<String> userIds, String projectId, boolean enable) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            if (enable) {
                return extSystemProjectMapper.getEnableProjectMemberByUserId(projectId, userIds);
            } else {
                return extSystemProjectMapper.getProjectMemberByUserId(projectId, userIds);
            }
        } else {
            return new ArrayList<>();
        }
    }

    protected List<Receiver> getReceivers(List<Receiver> receivers, Boolean excludeSelf, String operator) {
        // 排除自己
        List<Receiver> realReceivers = new ArrayList<>();
        if (excludeSelf) {
            for (Receiver receiver : receivers) {
                if (!StringUtils.equals(receiver.getUserId(), operator)) {
                    LogUtils.info("发送人是自己不发");
                    realReceivers.add(receiver);
                }
            }
        } else {
            realReceivers = receivers;
        }
        return realReceivers;
    }
}
