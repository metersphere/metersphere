package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.constants.TestCaseReviewStatus;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.constants.TestReviewCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.MathUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.notice.service.MailService;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestCaseReviewDTO;
import io.metersphere.track.dto.TestReviewCaseDTO;
import io.metersphere.track.dto.TestReviewDTOWithMetric;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import io.metersphere.track.request.testreview.QueryTestReviewRequest;
import io.metersphere.track.request.testreview.ReviewRelevanceRequest;
import io.metersphere.track.request.testreview.SaveTestCaseReviewRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
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
    MailService mailService;


    public void saveTestCaseReview(SaveTestCaseReviewRequest reviewRequest) {
        checkCaseReviewExist(reviewRequest);

        String reviewId = UUID.randomUUID().toString();
        List<String> projectIds = reviewRequest.getProjectIds();
        List<String> userIds = reviewRequest.getUserIds();
        projectIds.forEach(projectId -> {
            TestCaseReviewProject testCaseReviewProject = new TestCaseReviewProject();
            testCaseReviewProject.setProjectId(projectId);
            testCaseReviewProject.setReviewId(reviewId);
            testCaseReviewProjectMapper.insertSelective(testCaseReviewProject);
        });

        userIds.forEach(userId -> {
            TestCaseReviewUsers testCaseReviewUsers = new TestCaseReviewUsers();
            testCaseReviewUsers.setReviewId(reviewId);
            testCaseReviewUsers.setUserId(userId);
            testCaseReviewUsersMapper.insert(testCaseReviewUsers);
        });

        reviewRequest.setId(reviewId);
        reviewRequest.setCreateTime(System.currentTimeMillis());
        reviewRequest.setUpdateTime(System.currentTimeMillis());
        reviewRequest.setCreator(SessionUtils.getUser().getId());
        reviewRequest.setStatus(TestCaseReviewStatus.Prepare.name());
        testCaseReviewMapper.insert(reviewRequest);
        try {
            mailService.sendReviewerNotice(userIds, reviewRequest);
        } catch (Exception e) {
            LogUtil.error(e);
        }

    }

    public List<TestCaseReviewDTO> listCaseReview(QueryCaseReviewRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
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
        return extTestCaseReviewMapper.listByWorkspaceId(currentWorkspaceId);
    }

    public void editCaseReview(SaveTestCaseReviewRequest testCaseReview) {
        editCaseReviewer(testCaseReview);
        editCaseReviewProject(testCaseReview);
        testCaseReview.setUpdateTime(System.currentTimeMillis());
        checkCaseReviewExist(testCaseReview);
        testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
        try {
            mailService.sendReviewerNotice(testCaseReview.getUserIds(), testCaseReview);
        } catch (Exception e) {
            LogUtil.error(e);
        }
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

    private void editCaseReviewProject(SaveTestCaseReviewRequest testCaseReview) {
        List<String> projectIds = testCaseReview.getProjectIds();
        String id = testCaseReview.getId();
        if (!CollectionUtils.isEmpty(projectIds)) {
            TestCaseReviewProjectExample testCaseReviewProjectExample = new TestCaseReviewProjectExample();
            testCaseReviewProjectExample.createCriteria().andReviewIdEqualTo(id);
            List<TestCaseReviewProject> testCaseReviewProjects = testCaseReviewProjectMapper.selectByExample(testCaseReviewProjectExample);
            List<String> dbProjectIds = testCaseReviewProjects.stream().map(TestCaseReviewProject::getProjectId).collect(Collectors.toList());
            projectIds.forEach(projectId -> {
                if (!dbProjectIds.contains(projectId)) {
                    TestCaseReviewProject testCaseReviewProject = new TestCaseReviewProject();
                    testCaseReviewProject.setReviewId(id);
                    testCaseReviewProject.setProjectId(projectId);
                    testCaseReviewProjectMapper.insert(testCaseReviewProject);
                }
            });

            TestCaseReviewProjectExample example = new TestCaseReviewProjectExample();
            example.createCriteria().andReviewIdEqualTo(id).andProjectIdNotIn(projectIds);
            testCaseReviewProjectMapper.deleteByExample(example);


            // 关联的项目下的用例idList
            TestCaseExample testCaseExample = new TestCaseExample();
            testCaseExample.createCriteria().andProjectIdIn(projectIds);
            List<TestCase> caseList = testCaseMapper.selectByExample(testCaseExample);
            List<String> caseIds = caseList.stream().map(TestCase::getId).collect(Collectors.toList());

            TestCaseReviewTestCaseExample testCaseReviewTestCaseExample = new TestCaseReviewTestCaseExample();
            TestCaseReviewTestCaseExample.Criteria criteria = testCaseReviewTestCaseExample.createCriteria().andReviewIdEqualTo(id);
            if (!CollectionUtils.isEmpty(caseIds)) {
                criteria.andCaseIdNotIn(caseIds);
            }
            testCaseReviewTestCaseMapper.deleteByExample(testCaseReviewTestCaseExample);
        }
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
        deleteCaseReviewProject(reviewId);
        deleteCaseReviewUsers(reviewId);
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

    private void deleteCaseReviewTestCase(String reviewId) {
        TestCaseReviewTestCaseExample testCaseReviewTestCaseExample = new TestCaseReviewTestCaseExample();
        testCaseReviewTestCaseExample.createCriteria().andReviewIdEqualTo(reviewId);
        testCaseReviewTestCaseMapper.deleteByExample(testCaseReviewTestCaseExample);
    }

    public List<TestCaseReview> listCaseReviewAll(String currentWorkspaceId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(currentWorkspaceId);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(projectIds)) {
            TestCaseReviewProjectExample testCaseReviewProjectExample = new TestCaseReviewProjectExample();
            testCaseReviewProjectExample.createCriteria().andProjectIdIn(projectIds);
            List<TestCaseReviewProject> testCaseReviewProjects = testCaseReviewProjectMapper.selectByExample(testCaseReviewProjectExample);
            List<String> reviewIds = testCaseReviewProjects.stream().map(TestCaseReviewProject::getReviewId).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(reviewIds)) {
                TestCaseReviewExample testCaseReviewExample = new TestCaseReviewExample();
                testCaseReviewExample.createCriteria().andIdIn(reviewIds);
                return testCaseReviewMapper.selectByExample(testCaseReviewExample);
            }
        }

        return new ArrayList<>();
    }

    public void testReviewRelevance(ReviewRelevanceRequest request) {
        String reviewId = request.getReviewId();
        List<String> userIds = getTestCaseReviewerIds(reviewId);
        String currentId = SessionUtils.getUser().getId();
        if (!userIds.contains(currentId)) {
            MSException.throwException("非用例评审人员，不能关联用例！");
        }

        List<String> testCaseIds = request.getTestCaseIds();

        if (testCaseIds.isEmpty()) {
            return;
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseReviewTestCaseMapper batchMapper = sqlSession.getMapper(TestCaseReviewTestCaseMapper.class);

        if (!testCaseIds.isEmpty()) {
            testCaseIds.forEach(caseId -> {
                TestCaseReviewTestCase caseReview = new TestCaseReviewTestCase();
                caseReview.setId(UUID.randomUUID().toString());
                caseReview.setReviewer(SessionUtils.getUser().getId());
                caseReview.setCaseId(caseId);
                caseReview.setCreateTime(System.currentTimeMillis());
                caseReview.setUpdateTime(System.currentTimeMillis());
                caseReview.setReviewId(request.getReviewId());
                caseReview.setStatus(TestCaseReviewStatus.Prepare.name());
                batchMapper.insert(caseReview);
            });
        }

        sqlSession.flushStatements();

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
            }
        }
        testCaseReview.setStatus(TestCaseReviewStatus.Completed.name());
        SaveTestCaseReviewRequest testCaseReviewRequest = new SaveTestCaseReviewRequest();
        TestCaseReview _testCaseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);
        List<String> userIds = new ArrayList<>();
        userIds.add(_testCaseReview.getCreator());
        testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
        try {
            BeanUtils.copyProperties(testCaseReviewRequest, _testCaseReview);
            mailService.sendEndNotice(userIds, testCaseReviewRequest);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public List<TestReviewDTOWithMetric> listRelateAll(String type) {
        SessionUser user = SessionUtils.getUser();
        QueryTestReviewRequest request = new QueryTestReviewRequest();
        if (StringUtils.equals("creator", type)) {
            request.setCreator(user.getId());
        } else {
            request.setReviewerId(user.getId());
        }
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setReviewIds(extTestReviewCaseMapper.findRelateTestReviewId(user.getId(), SessionUtils.getCurrentWorkspaceId()));

        List<String> projectIds = extProjectMapper.getProjectIdByWorkspaceId(SessionUtils.getCurrentOrganizationId());

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
                String reviewName = getReviewName(userIds);
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

    private String getReviewName(List<String> userIds) {
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        Map<String, String> userMap = userService.getMemberList(queryMemberRequest)
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        StringBuilder stringBuilder = new StringBuilder();
        String name = "";

        if (userIds.size() > 0) {
            for (String id : userIds) {
                stringBuilder.append(userMap.get(id)).append("、");
            }
            name = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
        }
        return name;
    }

    public List<TestReviewCaseDTO> listTestCaseByProjectIds(List<String> projectIds) {
        QueryCaseReviewRequest request = new QueryCaseReviewRequest();
        request.setProjectIds(projectIds);
        return extTestReviewCaseMapper.list(request);
    }
}
