package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.TestCaseReviewStatus;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.constants.TestReviewCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestCaseReviewReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestCaseReviewDTO;
import io.metersphere.track.dto.TestReviewCaseDTO;
import io.metersphere.track.dto.TestReviewDTOWithMetric;
import io.metersphere.track.request.testreview.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseReviewService {

    @Resource
    private TestCaseReviewProjectMapper testCaseReviewProjectMapper;
    @Resource
    private TestCaseReviewUsersMapper testCaseReviewUsersMapper;
    @Resource
    private TestCaseReviewMapper testCaseReviewMapper;
    @Resource
    private ExtTestCaseReviewMapper extTestCaseReviewMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    ExtTestReviewCaseMapper extTestReviewCaseMapper;
    @Resource
    ExtProjectMapper extProjectMapper;
    @Resource
    UserService userService;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    TestCaseReviewTestCaseMapper testCaseReviewTestCaseMapper;
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private TestCaseReviewLoadMapper testCaseReviewLoadMapper;
    @Resource
    private TestCaseReviewApiCaseMapper testCaseReviewApiCaseMapper;
    @Resource
    private TestCaseReviewScenarioMapper testCaseReviewScenarioMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;


    public String saveTestCaseReview(SaveTestCaseReviewRequest reviewRequest) {
        checkCaseReviewExist(reviewRequest);
        String reviewId = reviewRequest.getId();
        List<String> userIds = reviewRequest.getUserIds();//执行人

        userIds.forEach(userId -> {
            TestCaseReviewUsers testCaseReviewUsers = new TestCaseReviewUsers();
            testCaseReviewUsers.setReviewId(reviewId);
            testCaseReviewUsers.setUserId(userId);
            testCaseReviewUsersMapper.insert(testCaseReviewUsers);
        });

        reviewRequest.setId(reviewId);
        reviewRequest.setCreateUser(SessionUtils.getUserId());
        reviewRequest.setCreateTime(System.currentTimeMillis());
        reviewRequest.setUpdateTime(System.currentTimeMillis());
        reviewRequest.setCreator(SessionUtils.getUser().getId());//创建人
        reviewRequest.setStatus(TestCaseReviewStatus.Prepare.name());
        if (StringUtils.isBlank(reviewRequest.getProjectId())) {
            reviewRequest.setProjectId(SessionUtils.getCurrentProjectId());
        }
        testCaseReviewMapper.insert(reviewRequest);
        // 发送通知
        String context = getReviewContext(reviewRequest, NoticeConstants.Event.CREATE);
        Map<String, Object> paramMap = new HashMap<>(getReviewParamMap(reviewRequest));
        NoticeModel noticeModel = NoticeModel.builder()
                .context(context)
                .relatedUsers(userIds)
                .subject(Translator.get("test_review_task_notice"))
                .mailTemplate("ReviewInitiate")
                .paramMap(paramMap)
                .event(NoticeConstants.Event.CREATE)
                .build();
        noticeSendService.send(NoticeConstants.TaskType.REVIEW_TASK, noticeModel);
        return reviewRequest.getId();
    }

    //评审内容
    private Map<String, String> getReviewParamMap(SaveTestCaseReviewRequest reviewRequest) {
        Long startTime = reviewRequest.getCreateTime();
        Long endTime = reviewRequest.getEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        String eTime = String.valueOf(endTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        }
        String end = null;
        if (!eTime.equals("null")) {
            end = sdf.format(new Date(Long.parseLong(eTime)));
        }

        Map<String, String> paramMap = new HashMap<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        User user = userMapper.selectByPrimaryKey(reviewRequest.getCreator());
        paramMap.put("creator", user.getName());
        paramMap.put("reviewName", reviewRequest.getName());
        paramMap.put("start", start);
        paramMap.put("end", end);
        paramMap.put("id", reviewRequest.getId());
        String status = "";
        if (StringUtils.equals(TestPlanStatus.Underway.name(), reviewRequest.getStatus())) {
            status = "进行中";
        } else if (StringUtils.equals(TestPlanStatus.Prepare.name(), reviewRequest.getStatus())) {
            status = "未开始";
        } else if (StringUtils.equals(TestPlanStatus.Completed.name(), reviewRequest.getStatus())) {
            status = "已完成";
        }
        paramMap.put("status", status);
        return paramMap;
    }

    public List<TestCaseReviewDTO> listCaseReview(QueryCaseReviewRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        String projectId = request.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            return new ArrayList<>();
        }
        return extTestCaseReviewMapper.list(request);
    }

    public List<Project> getProjectByReviewId(TestCaseReview request) {
        String reviewId = request.getId();

        TestCaseReviewProjectExample testCaseReviewProjectExample = new TestCaseReviewProjectExample();
        testCaseReviewProjectExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewProject> testCaseReviewProject = testCaseReviewProjectMapper.selectByExample(testCaseReviewProjectExample);

        List<String> projectIds = testCaseReviewProject
                .stream()
                .map(TestCaseReviewProject::getProjectId)
                .collect(Collectors.toList());

        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);
        String projectId = testCaseReview.getProjectId();
        projectIds.add(projectId);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        return projectMapper.selectByExample(projectExample);
    }

    public List<User> getUserByReviewId(TestCaseReview request) {
        String reviewId = request.getId();

        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);

        List<String> userIds = testCaseReviewUsers
                .stream()
                .map(TestCaseReviewUsers::getUserId)
                .collect(Collectors.toList());

        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if (!CollectionUtils.isEmpty(userIds)) {
            criteria.andIdIn(userIds);
            return userMapper.selectByExample(userExample);
        }
        return new ArrayList<>();
    }

    public List<TestCaseReviewDTO> recent(String currentWorkspaceId) {
        return extTestCaseReviewMapper.listByWorkspaceId(currentWorkspaceId, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

    public String editCaseReview(SaveTestCaseReviewRequest testCaseReview) {
        editCaseReviewer(testCaseReview);
        testCaseReview.setUpdateTime(System.currentTimeMillis());
        checkCaseReviewExist(testCaseReview);
        testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
        //  发送通知
        List<String> userIds = new ArrayList<>(testCaseReview.getUserIds());
        String context = getReviewContext(testCaseReview, NoticeConstants.Event.UPDATE);
        Map<String, Object> paramMap = new HashMap<>(getReviewParamMap(testCaseReview));
        NoticeModel noticeModel = NoticeModel.builder()
                .context(context)
                .relatedUsers(userIds)
                .subject(Translator.get("test_review_task_notice"))
                .mailTemplate("ReviewEnd")
                .paramMap(paramMap)
                .event(NoticeConstants.Event.UPDATE)
                .build();
        noticeSendService.send(NoticeConstants.TaskType.REVIEW_TASK, noticeModel);
        return testCaseReview.getId();
    }

    private void editCaseReviewer(SaveTestCaseReviewRequest testCaseReview) {
        // 要更新的reviewerIds
        List<String> reviewerIds = testCaseReview.getUserIds();

        String id = testCaseReview.getId();
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(id);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);
        List<String> dbReviewIds = testCaseReviewUsers.stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());

        reviewerIds.forEach(reviewerId -> {
            if (!dbReviewIds.contains(reviewerId)) {
                TestCaseReviewUsers caseReviewUser = new TestCaseReviewUsers();
                caseReviewUser.setUserId(reviewerId);
                caseReviewUser.setReviewId(id);
                testCaseReviewUsersMapper.insertSelective(caseReviewUser);
            }
        });

        TestCaseReviewUsersExample example = new TestCaseReviewUsersExample();
        example.createCriteria().andReviewIdEqualTo(id).andUserIdNotIn(reviewerIds);
        testCaseReviewUsersMapper.deleteByExample(example);
    }

    private void checkCaseReviewExist(TestCaseReview testCaseReview) {
        if (testCaseReview.getName() != null) {
            TestCaseReviewExample example = new TestCaseReviewExample();
            TestCaseReviewExample.Criteria criteria = example
                    .createCriteria()
                    .andNameEqualTo(testCaseReview.getName());

            if (StringUtils.isNotBlank(testCaseReview.getId())) {
                criteria.andIdNotEqualTo(testCaseReview.getId());
            }

            if (testCaseReviewMapper.selectByExample(example).size() > 0) {
                MSException.throwException("评审名称已存在");
            }
        }
    }

    public void deleteCaseReview(String reviewId) {
        TestCaseReview testCaseReview = getTestReview(reviewId);
        deleteCaseReviewProject(reviewId);
        deleteCaseReviewUsers(reviewId);
        deleteCaseReviewTestCase(reviewId);
        testCaseReviewMapper.deleteByPrimaryKey(reviewId);
        // 发送通知
        try {
            List<String> userIds = new ArrayList<>();
            userIds.add(testCaseReview.getCreator());
            SaveTestCaseReviewRequest testCaseReviewRequest = new SaveTestCaseReviewRequest();
            BeanUtils.copyProperties(testCaseReviewRequest, testCaseReview);
            String context = getReviewContext(testCaseReviewRequest, NoticeConstants.Event.DELETE);
            Map<String, Object> paramMap = new HashMap<>(getReviewParamMap(testCaseReviewRequest));
            NoticeModel noticeModel = NoticeModel.builder()
                    .context(context)
                    .relatedUsers(userIds)
                    .subject(Translator.get("test_review_task_notice"))
                    .mailTemplate("ReviewDelete")
                    .paramMap(paramMap)
                    .event(NoticeConstants.Event.DELETE)
                    .build();
            noticeSendService.send(NoticeConstants.TaskType.REVIEW_TASK, noticeModel);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void deleteCaseReviewProject(String reviewId) {
        TestCaseReviewProjectExample testCaseReviewProjectExample = new TestCaseReviewProjectExample();
        testCaseReviewProjectExample.createCriteria().andReviewIdEqualTo(reviewId);
        testCaseReviewProjectMapper.deleteByExample(testCaseReviewProjectExample);
    }

    private void deleteCaseReviewUsers(String reviewId) {
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
        testCaseReviewUsersMapper.deleteByExample(testCaseReviewUsersExample);
    }

    private void deleteCaseReviewTestCase(String reviewId) {
        TestCaseReviewTestCaseExample testCaseReviewTestCaseExample = new TestCaseReviewTestCaseExample();
        testCaseReviewTestCaseExample.createCriteria().andReviewIdEqualTo(reviewId);
        testCaseReviewTestCaseMapper.deleteByExample(testCaseReviewTestCaseExample);
    }

    public List<TestCaseReview> listCaseReviewAll() {
        if (StringUtils.isNotBlank(SessionUtils.getCurrentProjectId())) {
            TestCaseReviewExample testCaseReviewExample = new TestCaseReviewExample();
            testCaseReviewExample.createCriteria().andProjectIdEqualTo(SessionUtils.getCurrentProjectId());
            return testCaseReviewMapper.selectByExample(testCaseReviewExample);
        }
        return new ArrayList<>();
    }

    public void testReviewRelevance(ReviewRelevanceRequest request) {
        List<String> testCaseIds = request.getTestCaseIds();

        if (testCaseIds.isEmpty()) {
            return;
        }
        // 如果是关联全部指令则根据条件查询未关联的案例
        if (testCaseIds.get(0).equals("all")) {
            List<TestCase> testCases = extTestCaseMapper.getTestCaseByNotInReview(request.getRequest());
            if (!testCases.isEmpty()) {
                testCaseIds = testCases.stream().map(testCase -> testCase.getId()).collect(Collectors.toList());
            }
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseReviewTestCaseMapper batchMapper = sqlSession.getMapper(TestCaseReviewTestCaseMapper.class);

        if (!testCaseIds.isEmpty()) {
            testCaseIds.forEach(caseId -> {
                TestCaseReviewTestCase caseReview = new TestCaseReviewTestCase();
                caseReview.setId(UUID.randomUUID().toString());
                caseReview.setReviewer(SessionUtils.getUser().getId());
                caseReview.setCreateUser(SessionUtils.getUserId());
                caseReview.setCaseId(caseId);
                caseReview.setCreateTime(System.currentTimeMillis());
                caseReview.setUpdateTime(System.currentTimeMillis());
                caseReview.setReviewId(request.getReviewId());
                caseReview.setStatus(TestCaseReviewStatus.Prepare.name());
                batchMapper.insert(caseReview);
            });
        }

        sqlSession.flushStatements();
        //同步添加关联的接口和测试用例
     /*   if(request.getChecked()){
            if (!testCaseIds.isEmpty()) {
                testCaseIds.forEach(caseId -> {
                    TestCaseWithBLOBs testDtail=testCaseMapper.selectByPrimaryKey(caseId);
                    if(StringUtils.equals(testDtail.getType(), TestCaseStatus.performance.name())){
                        TestCaseReviewLoad t=new TestCaseReviewLoad();
                        t.setId(UUID.randomUUID().toString());
                        t.setTestCaseReviewId(request.getReviewId());
                        t.setLoadCaseId(testDtail.getTestId());
                        t.setCreateTime(System.currentTimeMillis());
                        t.setUpdateTime(System.currentTimeMillis());
                        TestCaseReviewLoadExample example=new TestCaseReviewLoadExample();
                        example.createCriteria().andTestCaseReviewIdEqualTo(request.getReviewId()).andLoadCaseIdEqualTo(t.getLoadCaseId());
                        if (testCaseReviewLoadMapper.countByExample(example) <=0) {
                            testCaseReviewLoadMapper.insert(t);
                        }

                    }
                    if(StringUtils.equals(testDtail.getType(),TestCaseStatus.testcase.name())){
                        TestCaseReviewApiCase t=new TestCaseReviewApiCase();
                        ApiTestCaseWithBLOBs apitest=apiTestCaseMapper.selectByPrimaryKey(testDtail.getTestId());
                        ApiDefinitionWithBLOBs apidefinition=apiDefinitionMapper.selectByPrimaryKey(apitest.getApiDefinitionId());
                        t.setId(UUID.randomUUID().toString());
                        t.setTestCaseReviewId(request.getReviewId());
                        t.setApiCaseId(testDtail.getTestId());
                        t.setEnvironmentId(apidefinition.getEnvironmentId());
                        t.setCreateTime(System.currentTimeMillis());
                        t.setUpdateTime(System.currentTimeMillis());
                        TestCaseReviewApiCaseExample example=new TestCaseReviewApiCaseExample();
                        example.createCriteria().andTestCaseReviewIdEqualTo(request.getReviewId()).andApiCaseIdEqualTo(t.getApiCaseId());
                        if(testCaseReviewApiCaseMapper.countByExample(example)<=0){
                            testCaseReviewApiCaseMapper.insert(t);
                        }

                    }
                    if(StringUtils.equals(testDtail.getType(),TestCaseStatus.automation.name())){
                        TestCaseReviewScenario t=new TestCaseReviewScenario();
                        ApiScenarioWithBLOBs testPlanApiScenario=apiScenarioMapper.selectByPrimaryKey(testDtail.getTestId());
                        t.setId(UUID.randomUUID().toString());
                        t.setTestCaseReviewId(request.getReviewId());
                        t.setApiScenarioId(testDtail.getTestId());
                        t.setLastResult(testPlanApiScenario.getLastResult());
                        t.setPassRate(testPlanApiScenario.getPassRate());
                        t.setReportId(testPlanApiScenario.getReportId());
                        t.setStatus(testPlanApiScenario.getStatus());
                        t.setCreateTime(System.currentTimeMillis());
                        t.setUpdateTime(System.currentTimeMillis());
                        TestCaseReviewScenarioExample example=new TestCaseReviewScenarioExample();
                        example.createCriteria().andTestCaseReviewIdEqualTo(request.getReviewId()).andApiScenarioIdEqualTo(t.getApiScenarioId());
                        if(testCaseReviewScenarioMapper.countByExample(example)<=0){
                            testCaseReviewScenarioMapper.insert(t);
                        }

                    }

                });
            }
        }*/
        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        if (StringUtils.equals(testCaseReview.getStatus(), TestCaseReviewStatus.Prepare.name())
                || StringUtils.equals(testCaseReview.getStatus(), TestCaseReviewStatus.Completed.name())) {
            testCaseReview.setStatus(TestCaseReviewStatus.Underway.name());
            testCaseReviewMapper.updateByPrimaryKey(testCaseReview);
        }
    }

    public List<String> getTestCaseReviewerIds(String reviewId) {
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);
        return testCaseReviewUsers.stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());
    }

    public TestCaseReview getTestReview(String reviewId) {
        return Optional.ofNullable(testCaseReviewMapper.selectByPrimaryKey(reviewId)).orElse(new TestCaseReview());
    }

    public void editTestReviewStatus(String reviewId) {
        List<String> statusList = extTestReviewCaseMapper.getStatusByReviewId(reviewId);
        TestCaseReview testCaseReview = new TestCaseReview();
        testCaseReview.setId(reviewId);

        for (String status : statusList) {
            if (StringUtils.equals(status, TestReviewCaseStatus.Prepare.name())) {
                testCaseReview.setStatus(TestCaseReviewStatus.Underway.name());
                testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
                return;
            } else if (StringUtils.equals(status, TestReviewCaseStatus.UnPass.name())) {
                testCaseReview.setStatus(TestCaseReviewStatus.Finished.name());
                testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
                return;
            }
        }
        testCaseReview.setStatus(TestCaseReviewStatus.Completed.name());
        testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
        SaveTestCaseReviewRequest testCaseReviewRequest = new SaveTestCaseReviewRequest();
        TestCaseReview _testCaseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);
        List<String> userIds = new ArrayList<>();
        userIds.add(_testCaseReview.getCreator());
        if (StringUtils.equals(TestCaseReviewStatus.Completed.name(), _testCaseReview.getStatus())) {
            try {
                BeanUtils.copyProperties(testCaseReviewRequest, _testCaseReview);
                String context = getReviewContext(testCaseReviewRequest, NoticeConstants.Event.UPDATE);
                Map<String, Object> paramMap = new HashMap<>(getReviewParamMap(testCaseReviewRequest));
                NoticeModel noticeModel = NoticeModel.builder()
                        .context(context)
                        .relatedUsers(userIds)
                        .subject(Translator.get("test_review_task_notice"))
                        .mailTemplate("ReviewEnd")
                        .paramMap(paramMap)
                        .event(NoticeConstants.Event.UPDATE)
                        .build();
                noticeSendService.send(NoticeConstants.TaskType.REVIEW_TASK, noticeModel);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
    }

    public List<TestReviewDTOWithMetric> listRelateAll(ReviewRelateRequest relateRequest) {
        String type = relateRequest.getType();
        String projectId = relateRequest.getProjectId();
        String workspaceId = relateRequest.getWorkspaceId();
        SessionUser user = SessionUtils.getUser();
        QueryTestReviewRequest request = new QueryTestReviewRequest();
        if (StringUtils.equals("creator", type)) {
            request.setCreator(user.getId());
        } else {
            request.setReviewerId(user.getId());
        }

        request.setWorkspaceId(workspaceId);
        request.setProjectId(projectId);
        request.setReviewIds(extTestReviewCaseMapper.findRelateTestReviewId(user.getId(), workspaceId, projectId));

        List<String> projectIds = extProjectMapper.getProjectIdByWorkspaceId(workspaceId);

        List<TestReviewDTOWithMetric> testReviews = extTestCaseReviewMapper.listRelate(request);

        Map<String, List<TestReviewCaseDTO>> testCaseMap = new HashMap<>();
        listTestCaseByProjectIds(projectIds).forEach(testCase -> {
            List<TestReviewCaseDTO> list = testCaseMap.get(testCase.getReviewId());
            if (list == null) {
                list = new ArrayList<>();
                list.add(testCase);
                testCaseMap.put(testCase.getReviewId(), list);
            } else {
                list.add(testCase);
            }
        });

        if (!CollectionUtils.isEmpty(testReviews)) {
            testReviews.forEach(testReview -> {
                List<TestReviewCaseDTO> testCases = testCaseMap.get(testReview.getId());

                TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
                testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(testReview.getId());
                List<String> userIds = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample)
                        .stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());
                String reviewName = getReviewName(userIds, projectId);
                testReview.setReviewerName(reviewName);

                User u = userMapper.selectByPrimaryKey(testReview.getCreator());
                if (u != null) {
                    testReview.setCreator(u.getName());
                }

                testReview.setReviewed(0);
                testReview.setTotal(0);
                testReview.setPass(0);
                if (testCases != null) {
                    testReview.setTotal(testCases.size());
                    testCases.forEach(testCase -> {
                        if (!StringUtils.equals(testCase.getReviewStatus(), TestReviewCaseStatus.Prepare.name())) {
                            testReview.setReviewed(testReview.getReviewed() + 1);
                        }
                        if (StringUtils.equals(testCase.getReviewStatus(), TestReviewCaseStatus.Pass.name())) {
                            testReview.setPass(testReview.getPass() + 1);
                        }
                    });
                }

            });
        }
        return testReviews;
    }

    private String getReviewName(List<String> userIds, String projectId) {
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setProjectId(projectId);
        Map<String, String> userMap = userService.getProjectMember(queryMemberRequest)
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        StringBuilder stringBuilder = new StringBuilder();
        String name = "";

        if (userIds.size() > 0) {
            for (String id : userIds) {
                if (StringUtils.isNotBlank(userMap.get(id))) {
                    stringBuilder.append(userMap.get(id)).append("、");
                }
            }
            if (StringUtils.isNotBlank(stringBuilder)) {
                name = stringBuilder.substring(0, stringBuilder.length() - 1);
            }
        }
        return name;
    }

    private List<TestReviewCaseDTO> listTestCaseByProjectIds(List<String> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return new ArrayList<>();
        }
        return extTestReviewCaseMapper.listTestCaseByProjectIds(projectIds);
    }

    /*编辑，新建，完成,删除通知内容*/
    private String getReviewContext(SaveTestCaseReviewRequest reviewRequest, String type) {

        User user = userMapper.selectByPrimaryKey(reviewRequest.getCreator());
        Long startTime = reviewRequest.getCreateTime();
        Long endTime = reviewRequest.getEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        String eTime = String.valueOf(endTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        } else {
            start = "未设置";
        }
        String end = null;
        if (!eTime.equals("null")) {
            end = sdf.format(new Date(Long.parseLong(eTime)));
        } else {
            start = "未设置";
        }
        String context = "";
        if (StringUtils.equals(NoticeConstants.Event.CREATE, type)) {
            context = "测试评审任务通知：" + user.getName() + "发起的" + "'" + reviewRequest.getName() + "'" + "待开始，计划开始时间是" + start + "计划结束时间为" + end + "请跟进";
        } else if (StringUtils.equals(NoticeConstants.Event.UPDATE, type)) {
            String status = "";
            if (StringUtils.equals(TestPlanStatus.Underway.name(), reviewRequest.getStatus())) {
                status = "进行中";
            } else if (StringUtils.equals(TestPlanStatus.Prepare.name(), reviewRequest.getStatus())) {
                status = "未开始";
            } else if (StringUtils.equals(TestPlanStatus.Completed.name(), reviewRequest.getStatus())) {
                status = "已完成";
            }
            context = "测试评审任务通知：" + user.getName() + "发起的" + "'" + reviewRequest.getName() + "'" + "计划开始时间是" + start + "计划结束时间为" + end + status;
        } else if (StringUtils.equals(NoticeConstants.Event.DELETE, type)) {
            context = "测试评审任务通知：" + user.getName() + "发起的" + "'" + reviewRequest.getName() + "'" + "计划开始时间是" + start + "计划结束时间为" + end + "已删除";
        }

        return context;
    }

    public String getLogDetails(String id) {
        TestCaseReview review = testCaseReviewMapper.selectByPrimaryKey(id);
        if (review != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(review, TestCaseReviewReference.testCaseReviewColumns);

            String reviewId = review.getId();
            TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
            testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
            List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);

            List<String> userIds = testCaseReviewUsers.stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());
            UserExample example = new UserExample();
            example.createCriteria().andIdIn(userIds);
            List<User> users = userMapper.selectByExample(example);
            List<String> userNames = users.stream().map(User::getName).collect(Collectors.toList());

            DetailColumn column = new DetailColumn("评审人", "reviewUser", String.join(",", userNames), null);
            columns.add(column);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), review.getProjectId(), review.getName(), review.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ReviewRelevanceRequest request) {
        List<String> testCaseIds = request.getTestCaseIds();
        List<String> names = new ArrayList<>();
        if (testCaseIds.get(0).equals("all")) {
            List<TestCase> testCases = extTestCaseMapper.getTestCaseByNotInReview(request.getRequest());
            if (!testCases.isEmpty()) {
                names = testCases.stream().map(TestCase::getName).collect(Collectors.toList());
                testCaseIds = testCases.stream().map(testCase -> testCase.getId()).collect(Collectors.toList());
            }
        } else {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(testCaseIds);
            List<TestCase> cases = testCaseMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(cases)) {
                names = cases.stream().map(TestCase::getName).collect(Collectors.toList());
            }
        }
        TestCaseReview caseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        if (caseReview != null) {
            List<DetailColumn> columns = new LinkedList<>();
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(testCaseIds), caseReview.getProjectId(), String.join(",", names) + " 关联到 " + "【" + caseReview.getName() + "】", caseReview.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

}
