package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.TestCaseReviewStatus;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.MathUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.TestCaseReviewPassRule;
import io.metersphere.dto.*;
import io.metersphere.excel.converter.TestReviewCaseStatus;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestCaseReviewReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.request.testreview.*;
import io.metersphere.utils.ListUtil;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private TestCaseReviewFollowMapper testCaseReviewFollowMapper;
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
    BaseProjectMapper baseProjectMapper;
    @Resource
    BaseUserService baseUserService;
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
    @Lazy
    private TestReviewTestCaseService testReviewTestCaseService;
    @Resource
    private TestCaseReviewTestCaseUsersMapper testCaseReviewTestCaseUsersMapper;
    @Resource
    @Lazy
    private TestCaseService testCaseService;

    public TestCaseReview saveTestCaseReview(SaveTestCaseReviewRequest reviewRequest) {
        checkCaseReviewExist(reviewRequest);
        String reviewId = reviewRequest.getId();
        List<String> userIds = reviewRequest.getUserIds();//执行人

        userIds.forEach(userId -> {
            TestCaseReviewUsers testCaseReviewUsers = new TestCaseReviewUsers();
            testCaseReviewUsers.setReviewId(reviewId);
            testCaseReviewUsers.setUserId(userId);
            testCaseReviewUsersMapper.insert(testCaseReviewUsers);
        });

        List<String> follows = reviewRequest.getFollowIds();//关注人

        follows.forEach(followId -> {
            TestCaseReviewFollow testCaseReviewFollow = new TestCaseReviewFollow();
            testCaseReviewFollow.setReviewId(reviewId);
            testCaseReviewFollow.setFollowId(followId);
            testCaseReviewFollowMapper.insert(testCaseReviewFollow);
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

        if (StringUtils.isBlank(reviewRequest.getReviewPassRule())) {
            reviewRequest.setReviewPassRule(TestCaseReviewPassRule.SINGLE.name());
        }

        testCaseReviewMapper.insert(reviewRequest);
        return reviewRequest;
    }

    //评审内容
    private Map getReviewParamMap(TestCaseReview review) {
        Map paramMap = new HashMap<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.putAll(new BeanMap(review));
        return paramMap;
    }

    public List<TestCaseReviewDTO> listCaseReview(QueryCaseReviewRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        //update   reviewerId
        if (StringUtils.equalsIgnoreCase(request.getReviewerId(), "currentUserId")) {
            request.setReviewerId(SessionUtils.getUserId());
        }
        List<TestCaseReviewDTO> list = extTestCaseReviewMapper.list(request);
        list.forEach(testCaseReviewDTO -> {
            testReviewTestCaseService.checkStatus(testCaseReviewDTO);
        });
        calcReviewRate(list);
        setReviews(list);
        return list;
    }

    /**
     * 计算评审的通过率和用例总数
     *
     * @param list
     */
    private void calcReviewRate(List<TestCaseReviewDTO> list) {
        List<String> reviewIds = list.stream()
                .map(TestCaseReviewDTO::getId)
                .collect(Collectors.toList());

        List<TestCaseReviewTestCase> testCaseReviewTestCase = testReviewTestCaseService.getCaseStatusByReviewIds(reviewIds);
        Map<String, List<TestCaseReviewTestCase>> reviewCaseMap = testCaseReviewTestCase.stream().
                collect(Collectors.groupingBy(TestCaseReviewTestCase::getReviewId));
        list.forEach(item -> {
            List<TestCaseReviewTestCase> caseList = reviewCaseMap.get(item.getId());
            if (CollectionUtils.isNotEmpty(caseList)) {
                Map<String, List<TestCaseReviewTestCase>> statusMap = caseList.stream()
                        .collect(Collectors.groupingBy(TestCaseReviewTestCase::getStatus));
                Map<String, Integer> statusSizeMap = new HashMap<>();
                statusMap.forEach((k, v) -> statusSizeMap.put(k, v.size()));
                doCalcReviewRate(item, statusSizeMap);
            } else {
                item.setPassRate(0d);
                item.setCaseCount(0);
            }
        });
    }

    private void doCalcReviewRate(TestCaseReviewDTO review, Map<String, Integer> statusSizeMap) {
        List<TestReviewCaseStatus> statusList = Arrays.stream(TestReviewCaseStatus.values())
                .sorted(Comparator.comparing(TestReviewCaseStatus::getOrder))
                .collect(Collectors.toList());

        List<CountMapDTO> statusCountList = new ArrayList<>();

        int passCount = 0;
        int total = 0;
        for (TestReviewCaseStatus status : statusList) {
            Integer count = statusSizeMap.get(status.name());
            if (count == null) {
                continue;
            }
            CountMapDTO countMapDTO = new CountMapDTO();
            countMapDTO.setKey(status.name());
            countMapDTO.setValue(count);
            statusCountList.add(countMapDTO);
            total += count;
            if (StringUtils.equals(status.name(), TestReviewCaseStatus.Pass.name())) {
                passCount = count;
            }
        }
        review.setStatusCountItems(statusCountList);

        review.setPassRate(MathUtils.getPercentWithDecimal(total == 0 ? 0 : passCount * 1.0 / total));
        review.setCaseCount(total);
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

        List<String> userIds = getReviewUserIds(reviewId);

        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if (!CollectionUtils.isEmpty(userIds)) {
            criteria.andIdIn(userIds);
            return userMapper.selectByExample(userExample);
        }
        return new ArrayList<>();
    }

    public void setReviews(List<TestCaseReviewDTO> reviewList) {
        List<String> reviewIds = reviewList.stream().map(TestCaseReviewDTO::getId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(reviewIds)) {
            return;
        }

        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdIn(reviewIds);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);

        List<String> userIds = testCaseReviewUsers
                .stream()
                .map(TestCaseReviewUsers::getUserId)
                .collect(Collectors.toList());

        Map<String, User> userMap = ServiceUtils.getUserMap(userIds);
        Map<String, List<TestCaseReviewUsers>> reviewUserMap = testCaseReviewUsers.stream()
                .collect(Collectors.groupingBy(TestCaseReviewUsers::getReviewId));

        reviewList.forEach(item -> {
            List<TestCaseReviewUsers> reviewUsers = reviewUserMap.get(item.getId());
            if (CollectionUtils.isNotEmpty(reviewUsers)) {
                item.setReviewers(new ArrayList<>());
                reviewUsers.forEach(reviewUser -> {
                    User user = userMap.get(reviewUser.getUserId());
                    if (user != null) {
                        item.getReviewers().add(user);
                    }
                });
            }
        });
    }

    public List<User> getFollowByReviewId(TestCaseReview request) {
        String reviewId = request.getId();

        TestCaseReviewFollowExample testCaseReviewFollowExample = new TestCaseReviewFollowExample();
        testCaseReviewFollowExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewFollow> testCaseReviewFollows = testCaseReviewFollowMapper.selectByExample(testCaseReviewFollowExample);

        List<String> userIds = testCaseReviewFollows
                .stream()
                .map(TestCaseReviewFollow::getFollowId)
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

    public TestCaseReview editCaseReview(SaveTestCaseReviewRequest testCaseReview) {
        TestCaseReview originReview = testCaseReviewMapper.selectByPrimaryKey(testCaseReview.getId());
        editCaseReviewer(testCaseReview);
        editCaseRevieweFollow(testCaseReview);
        testCaseReview.setUpdateTime(System.currentTimeMillis());
        checkCaseReviewExist(testCaseReview);
        testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);

        if (!StringUtils.equals(testCaseReview.getReviewPassRule(), originReview.getReviewPassRule())) {
            // 如果通过标准发生变化，则重新计算用例的状态
            testReviewTestCaseService.handlePassRuleChange(originReview.getReviewPassRule(), testCaseReview);
        }
        return testCaseReviewMapper.selectByPrimaryKey(testCaseReview.getId());
    }

    private void editCaseReviewer(SaveTestCaseReviewRequest testCaseReview) {
        // 要更新的reviewerIds
        List<String> reviewerIds = testCaseReview.getUserIds();

        String id = testCaseReview.getId();
        List<String> dbReviewIds = getReviewUserIds(id);

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
        // 如果修改了评审人，需要覆盖测试用例评审人
        editCaseReviewUser(testCaseReview, reviewerIds, dbReviewIds, id);
    }

    private List<String> getReviewUserIds(String reviewId) {
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);
        return testCaseReviewUsers.stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());
    }

    private void editCaseReviewUser(SaveTestCaseReviewRequest testCaseReview, List<String> reviewerIds, List<String> dbReviewIds, String id) {
        boolean equalFlag = ListUtil.equalsList(reviewerIds, dbReviewIds);
        if (!equalFlag) {
            TestCaseReviewTestCaseUsersExample testCaseReviewTestCaseUsersExample = new TestCaseReviewTestCaseUsersExample();
            testCaseReviewTestCaseUsersExample.createCriteria().andReviewIdEqualTo(id);
            testCaseReviewTestCaseUsersMapper.deleteByExample(testCaseReviewTestCaseUsersExample);

            List<TestCaseReviewTestCase> testCaseReviewTestCases = testReviewTestCaseService.selectForReviewerChange(id);

            if (CollectionUtils.isNotEmpty(testCaseReviewTestCases)) {
                testCaseReviewTestCases.forEach(review -> {
                    reviewerIds.forEach(userId -> {
                        TestCaseReviewTestCaseUsers insertData = new TestCaseReviewTestCaseUsers();
                        insertData.setReviewId(id);
                        insertData.setCaseId(review.getCaseId());
                        insertData.setUserId(userId);
                        testCaseReviewTestCaseUsersMapper.insert(insertData);
                    });
                });
            }

            for (TestCaseReviewTestCase reviewTestCase : testCaseReviewTestCases) {
                if (StringUtils.equalsAny(reviewTestCase.getStatus(),
                        TestReviewCaseStatus.Pass.name(), TestReviewCaseStatus.UnPass.name(), TestReviewCaseStatus.Underway.name())) {
                    // 重新计算评审状态
                    testReviewTestCaseService.reCalcReviewCaseStatus(testCaseReview.getReviewPassRule(), reviewTestCase);
                }
            }
        }
    }

    public void editCaseRevieweFollow(SaveTestCaseReviewRequest testCaseReview) {
        // 要更新的follows
        List<String> follows = testCaseReview.getFollowIds();
        if (CollectionUtils.isNotEmpty(follows)) {
            String id = testCaseReview.getId();
            TestCaseReviewFollowExample testCaseReviewfollowExample = new TestCaseReviewFollowExample();
            testCaseReviewfollowExample.createCriteria().andReviewIdEqualTo(id);
            List<TestCaseReviewFollow> testCaseReviewFollows = testCaseReviewFollowMapper.selectByExample(testCaseReviewfollowExample);
            List<String> dbReviewIds = testCaseReviewFollows.stream().map(TestCaseReviewFollow::getFollowId).collect(Collectors.toList());
            follows.forEach(followId -> {
                if (!dbReviewIds.contains(followId)) {
                    TestCaseReviewFollow caseReviewFollow = new TestCaseReviewFollow();
                    caseReviewFollow.setFollowId(followId);
                    caseReviewFollow.setReviewId(id);
                    testCaseReviewFollowMapper.insertSelective(caseReviewFollow);
                }
            });
            TestCaseReviewFollowExample example = new TestCaseReviewFollowExample();
            example.createCriteria().andReviewIdEqualTo(id).andFollowIdNotIn(follows);
            testCaseReviewFollowMapper.deleteByExample(example);
        } else {
            TestCaseReviewFollowExample example = new TestCaseReviewFollowExample();
            example.createCriteria().andReviewIdEqualTo(testCaseReview.getId());
            testCaseReviewFollowMapper.deleteByExample(example);
        }

    }

    private void checkCaseReviewExist(TestCaseReview testCaseReview) {
        if (testCaseReview.getName() != null) {
            TestCaseReviewExample example = new TestCaseReviewExample();
            TestCaseReviewExample.Criteria criteria = example
                    .createCriteria()
                    .andNameEqualTo(testCaseReview.getName())
                    .andProjectIdEqualTo(testCaseReview.getProjectId());

            if (StringUtils.isNotBlank(testCaseReview.getId())) {
                criteria.andIdNotEqualTo(testCaseReview.getId());
            }

            if (testCaseReviewMapper.selectByExample(example).size() > 0) {
                MSException.throwException("评审名称已存在");
            }
        }
    }

    public void deleteCaseReview(String reviewId) {
        deleteCaseReviewProject(reviewId);
        deleteCaseReviewUsers(reviewId);
        deleteCaseReviewFollow(reviewId);
        deleteCaseReviewTestCase(reviewId);
        testCaseReviewMapper.deleteByPrimaryKey(reviewId);
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

    private void deleteCaseReviewFollow(String reviewId) {
        TestCaseReviewFollowExample testCaseReviewFollowExample = new TestCaseReviewFollowExample();
        testCaseReviewFollowExample.createCriteria().andReviewIdEqualTo(reviewId);
        testCaseReviewFollowMapper.deleteByExample(testCaseReviewFollowExample);
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

        ServiceUtils.buildCombineTagsToSupportMultiple(request.getRequest());

        // 如果是关联全部指令则根据条件查询未关联的案例
        if (testCaseIds.get(0).equals("all")) {
            List<TestCaseDTO> testCases = extTestCaseMapper.getTestCaseByNotInReview(request.getRequest());
            if (!testCases.isEmpty()) {
                testCaseIds = testCases.stream().map(testCase -> testCase.getId()).collect(Collectors.toList());
            }
        }

        // 尽量保持与用例顺序一致
        Collections.reverse(testCaseIds);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseReviewTestCaseMapper batchMapper = sqlSession.getMapper(TestCaseReviewTestCaseMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getReviewId(), extTestReviewCaseMapper::getLastOrder);
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(request.getReviewId());
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);
        if (!testCaseIds.isEmpty()) {
            for (String caseId : testCaseIds) {
                TestCaseReviewTestCase caseReview = new TestCaseReviewTestCase();
                caseReview.setId(UUID.randomUUID().toString());
                caseReview.setReviewer(SessionUtils.getUser().getId());
                caseReview.setCreateUser(SessionUtils.getUserId());
                caseReview.setCaseId(caseId);
                caseReview.setCreateTime(System.currentTimeMillis());
                caseReview.setUpdateTime(System.currentTimeMillis());
                caseReview.setReviewId(request.getReviewId());
                caseReview.setStatus(TestCaseReviewStatus.Underway.name());
                caseReview.setIsDel(false);
                caseReview.setOrder(nextOrder);
                batchMapper.insert(caseReview);
                nextOrder += ServiceUtils.ORDER_STEP;
                if (CollectionUtils.isNotEmpty(testCaseReviewUsers)) {
                    testCaseReviewUsers.forEach(review -> {
                        TestCaseReviewTestCaseUsers insertData = new TestCaseReviewTestCaseUsers();
                        insertData.setReviewId(request.getReviewId());
                        insertData.setCaseId(caseId);
                        insertData.setUserId(review.getUserId());
                        testCaseReviewTestCaseUsersMapper.insert(insertData);
                    });
                }
                testCaseService.updateReviewStatus(caseReview.getCaseId(), caseReview.getStatus());
            }
        }

        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }

        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        if (StringUtils.equals(testCaseReview.getStatus(), TestCaseReviewStatus.Prepare.name())) {
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

    public TestCaseReviewWithMaintainer getTestReviewWithMaintainer(TestCaseReviewTestCaseEditRequest request) throws Exception {
        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        TestCaseReviewWithMaintainer reviewWithMaintainer = new TestCaseReviewWithMaintainer();
        BeanUtils.copyProperties(reviewWithMaintainer, testCaseReview);
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(request.getCaseId());
        reviewWithMaintainer.setMaintainer(testCaseWithBLOBs.getMaintainer());
        return reviewWithMaintainer;
    }

    public void editTestReviewStatus(String reviewId) {
        String status = extTestCaseReviewMapper.selectStatusById(reviewId);
        if (StringUtils.equalsAnyIgnoreCase(status, TestCaseReviewStatus.Completed.name(), TestCaseReviewStatus.Finished.name())) {
            return;
        }

        List<String> statusList = extTestReviewCaseMapper.getStatusByReviewId(reviewId);
        TestCaseReview testCaseReview = new TestCaseReview();
        testCaseReview.setId(reviewId);

        if (statusList.contains(TestReviewCaseStatus.Underway.name()) || statusList.contains(TestReviewCaseStatus.Again.name())) {
            return;
        }

        testCaseReview.setStatus(TestCaseReviewStatus.Completed.name());
        testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
        SaveTestCaseReviewRequest testCaseReviewRequest = new SaveTestCaseReviewRequest();
        TestCaseReview _testCaseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);

        if (StringUtils.equals(TestCaseReviewStatus.Completed.name(), _testCaseReview.getStatus())) {
            try {
                BeanUtils.copyProperties(testCaseReviewRequest, _testCaseReview);
                String context = getReviewContext(testCaseReviewRequest, NoticeConstants.Event.UPDATE);
                Map<String, Object> paramMap = new HashMap<>(getReviewParamMap(_testCaseReview));
                paramMap.put("operator", SessionUtils.getUser().getName());
                NoticeModel noticeModel = NoticeModel.builder()
                        .operator(SessionUtils.getUserId())
                        .context(context)
                        .subject("测试评审通知")
                        .paramMap(paramMap)
                        .event(NoticeConstants.Event.COMPLETE)
                        .status(TestCaseReviewStatus.Completed.name())
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
        request.setReviewIds(relateRequest.getReviewIds());

        List<TestReviewDTOWithMetric> testReviews = extTestCaseReviewMapper.listRelate(request);

        if (!CollectionUtils.isEmpty(testReviews)) {
            testReviews.forEach(testReview -> {
                List<CountMapDTO> countMapDTOS = extTestReviewCaseMapper.getStatusMapByReviewId(testReview.getId());

                TestCaseReviewTestCaseUsersExample testCaseReviewTestCaseUsersExample = new TestCaseReviewTestCaseUsersExample();
                testCaseReviewTestCaseUsersExample.createCriteria().andReviewIdEqualTo(testReview.getId());
                List<String> userIds = testCaseReviewTestCaseUsersMapper.selectByExample(testCaseReviewTestCaseUsersExample).
                        stream().map(TestCaseReviewTestCaseUsers::getUserId).distinct().collect(Collectors.toList());

                String reviewName = getReviewName(userIds, projectId);
                testReview.setReviewerName(reviewName);

                User u = userMapper.selectByPrimaryKey(testReview.getCreator());
                if (u != null) {
                    testReview.setCreator(u.getName());
                }

                Map<String, Integer> statusSizeMap = new HashMap<>();

                countMapDTOS.forEach(item -> {
                    statusSizeMap.put(item.getKey(), item.getValue());
                });

                doCalcReviewRate(testReview, statusSizeMap);
            });
        }
        return testReviews;
    }

    public void setReviewIds(ReviewRelateRequest request) {
        request.setReviewIds(extTestReviewCaseMapper.findRelateTestReviewId(SessionUtils.getUserId(), request.getWorkspaceId(), request.getProjectId()));
    }

    private String getReviewName(List<String> userIds, String projectId) {
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setProjectId(projectId);
        Map<String, String> userMap = baseUserService.getProjectMemberList(queryMemberRequest)
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        StringBuilder stringBuilder = new StringBuilder();
        String name = StringUtils.EMPTY;

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
        String context = StringUtils.EMPTY;
        if (StringUtils.equals(NoticeConstants.Event.CREATE, type)) {
            context = "测试评审任务通知：" + user.getName() + "发起的" + "'" + reviewRequest.getName() + "'" + "待开始，计划开始时间是" + start + "计划结束时间为" + end + "请跟进";
        } else if (StringUtils.equals(NoticeConstants.Event.UPDATE, type)) {
            String status = StringUtils.EMPTY;
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
            List<String> userIds = getReviewUserIds(reviewId);

            UserExample example = new UserExample();
            example.createCriteria().andIdIn(userIds);
            List<User> users = userMapper.selectByExample(example);
            List<String> userNames = users.stream().map(User::getName).collect(Collectors.toList());

            DetailColumn column = new DetailColumn("评审人", "reviewUser", String.join(",", userNames), null);
            columns.add(column);

            TestCaseReviewFollowExample testCaseReviewFollowExample = new TestCaseReviewFollowExample();
            testCaseReviewFollowExample.createCriteria().andReviewIdEqualTo(reviewId);
            List<TestCaseReviewFollow> testCaseReviewFollows = testCaseReviewFollowMapper.selectByExample(testCaseReviewFollowExample);

            List<String> follows = testCaseReviewFollows.stream().map(TestCaseReviewFollow::getFollowId).collect(Collectors.toList());
            //UserExample example = new UserExample();
            example.createCriteria().andIdIn(follows);
            List<User> follow = userMapper.selectByExample(example);
            List<String> followNames = follow.stream().map(User::getName).collect(Collectors.toList());

            DetailColumn columnFollow = new DetailColumn("关注人", "reviewFollow", String.join(",", followNames), null);
            columns.add(columnFollow);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), review.getProjectId(), review.getName(), review.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ReviewRelevanceRequest request) {
        List<String> testCaseIds = request.getTestCaseIds();
        List<String> names = new ArrayList<>();
        if (testCaseIds.get(0).equals("all")) {
            List<TestCaseDTO> testCases = extTestCaseMapper.getTestCaseByNotInReview(request.getRequest());
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
