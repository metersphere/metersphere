package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewMapper;
import io.metersphere.commons.constants.TestCaseReviewStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.dto.TestCaseReviewDTO;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import io.metersphere.track.request.testreview.SaveTestCaseReviewRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
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
        userExample.createCriteria().andIdIn(userIds);
        return userMapper.selectByExample(userExample);
    }

    public List<TestCaseReviewDTO> recent(String currentWorkspaceId) {
        return extTestCaseReviewMapper.listByWorkspaceId(currentWorkspaceId);
    }

    public void editCaseReview(TestCaseReview testCaseReview) {
        testCaseReview.setUpdateTime(System.currentTimeMillis());
        checkCaseReviewExist(testCaseReview);
        testCaseReviewMapper.updateByPrimaryKeySelective(testCaseReview);
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
}
