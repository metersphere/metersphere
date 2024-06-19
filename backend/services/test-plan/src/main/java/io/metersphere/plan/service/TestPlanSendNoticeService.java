package io.metersphere.plan.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.request.TestPlanUpdateRequest;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ReportStatus;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanSendNoticeService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanFollowerMapper testPlanFollowerMapper;

    public void sendNoticeCase(List<String> relatedUsers, String userId, String caseId, String task, String event, String testPlanId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseId);
        User user = userMapper.selectByPrimaryKey(userId);
        setLanguage(user.getLanguage());
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(task + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(task + "_" + event);
        Map paramMap;
        BeanMap beanMap = new BeanMap(functionalCase);
        paramMap = new HashMap<>(beanMap);
        paramMap.put("testPlanName", testPlan.getName());
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(user.getId())
                .context(template)
                .subject(subject)
                .paramMap(paramMap)
                .event(event)
                .status((String) paramMap.get("status"))
                .excludeSelf(true)
                .relatedUsers(relatedUsers)
                .build();
        noticeSendService.send(task, noticeModel);
    }

    private static void setLanguage(String language) {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase("US", language)) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase("TW", language)) {
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }


    public void batchSendNotice(String projectId, List<String> ids, User user, String event) {
        int amount = 100;//每次读取的条数
        long roundTimes = Double.valueOf(Math.ceil((double) ids.size() / amount)).longValue();//循环的次数
        for (int i = 0; i < (int) roundTimes; i++) {
            int fromIndex = (i * amount);
            int toIndex = ((i + 1) * amount);
            if (i == roundTimes - 1 || toIndex > ids.size()) {//最后一次遍历
                toIndex = ids.size();
            }
            List<String> subList = ids.subList(fromIndex, toIndex);
            List<TestPlanDTO> testPlanDTOS = handleBatchNotice(projectId, subList);
            List<Map> resources = new ArrayList<>();
            resources.addAll(JSON.parseArray(JSON.toJSONString(testPlanDTOS), Map.class));
            commonNoticeSendService.sendNotice(NoticeConstants.TaskType.TEST_PLAN_TASK, event, resources, user, projectId);
        }
    }

    private List<TestPlanDTO> handleBatchNotice(String projectId, List<String> ids) {
        List<TestPlanDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            Map<String, TestPlan> testPlanMap = getTestPlanInfo(projectId, ids);
            ids.forEach(id -> {
                if (testPlanMap.containsKey(id)) {
                    TestPlan testPlan = testPlanMap.get(id);
                    TestPlanDTO testPlanDTO = new TestPlanDTO();
                    BeanUtils.copyBean(testPlanDTO, testPlan);
                    dtoList.add(testPlanDTO);
                }
            });
        }
        return dtoList;
    }

    private Map<String, TestPlan> getTestPlanInfo(String projectId, List<String> ids) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andIdIn(ids);
        List<TestPlan> testPlans = testPlanMapper.selectByExample(example);
        return testPlans.stream().collect(Collectors.toMap(TestPlan::getId, testPlan -> testPlan));
    }


    public TestPlanDTO sendAddNotice(TestPlanCreateRequest request) {
        TestPlanDTO dto = new TestPlanDTO();
        BeanUtils.copyBean(dto, request);
        dto.setStatus(TestPlanConstants.TEST_PLAN_STATUS_PREPARED);
        return dto;
    }

    public TestPlanDTO sendUpdateNotice(TestPlanUpdateRequest request) {
        TestPlanDTO dto = new TestPlanDTO();
        BeanUtils.copyBean(dto, request);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getId());
        dto.setStatus(testPlan.getStatus());
        dto.setType(testPlan.getType());
        dto.setActualEndTime(testPlan.getActualEndTime());
        dto.setActualStartTime(testPlan.getActualStartTime());
        dto.setCreateTime(testPlan.getCreateTime());
        dto.setCreateUser(testPlan.getCreateUser());
        dto.setUpdateTime(testPlan.getUpdateTime());
        dto.setUpdateUser(testPlan.getUpdateUser());
        dto.setNum(testPlan.getNum());
        return dto;
    }

    public TestPlanDTO sendDeleteNotice(String id) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(id);
        TestPlanFollowerExample example = new TestPlanFollowerExample();
        example.createCriteria().andTestPlanIdEqualTo(id);
        List<TestPlanFollower> testPlanFollowers = testPlanFollowerMapper.selectByExample(example);
        List<String> followUsers = testPlanFollowers.stream().map(TestPlanFollower::getUserId).toList();
        TestPlanDTO dto = new TestPlanDTO();
        if (testPlan != null) {
            BeanUtils.copyBean(dto, testPlan);
            BeanUtils.copyBean(dto, testPlanConfig);
            dto.setFollowUsers(followUsers);
            return dto;
        }
        return null;
    }

    /**
     * 报告汇总-计划执行结束通知
     * @param currentUser 当前用户
     * @param planId 计划ID
     * @param projectId 项目ID
     * @param executeResult 执行结果
     */
    @Async
    public void sendExecuteNotice(String currentUser, String planId, String projectId, String executeResult) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        if (testPlan != null) {
            User user = userMapper.selectByPrimaryKey(currentUser);
            setLanguage(user.getLanguage());
            Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
            String template = defaultTemplateMap.get(StringUtils.equals(executeResult, ReportStatus.SUCCESS.name()) ?
                    NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_SUCCESSFUL : NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_FAILED);
            Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
            String subject = defaultSubjectMap.get(StringUtils.equals(executeResult, ReportStatus.SUCCESS.name()) ?
                    NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_SUCCESSFUL : NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_FAILED);
            Map<String, Object> paramMap = new HashMap<>(4);
            paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
            paramMap.put("name", testPlan.getName());
            paramMap.put("projectId", projectId);
            paramMap.put("Language", user.getLanguage());
            NoticeModel noticeModel = NoticeModel.builder().operator(currentUser).excludeSelf(false)
                    .context(template).subject(subject).paramMap(paramMap).event(StringUtils.equals(executeResult, ReportStatus.SUCCESS.name()) ?
                            NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_SUCCESSFUL : NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_FAILED).build();
            noticeSendService.send(NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
        }
    }
}
