package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewMapper;
import io.metersphere.base.mapper.TestCaseReviewTestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewUsersMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestReviewCaseDTO;
import io.metersphere.track.request.testplancase.TestReviewCaseBatchRequest;
import io.metersphere.track.request.testreview.DeleteRelevanceRequest;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestReviewTestCaseService {

    @Resource
    ExtTestReviewCaseMapper extTestReviewCaseMapper;
    @Resource
    UserService userService;
    @Resource
    TestCaseReviewTestCaseMapper testCaseReviewTestCaseMapper;
    @Resource
    TestCaseReviewUsersMapper testCaseReviewUsersMapper;
    @Resource
    TestCaseReviewMapper testCaseReviewMapper;
    @Resource
    TestCaseReviewService testCaseReviewService;
    @Resource
    TestCaseMapper testCaseMapper;

    public List<TestReviewCaseDTO> list(QueryCaseReviewRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<TestReviewCaseDTO> list = extTestReviewCaseMapper.list(request);
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        Map<String, String> userMap = userService.getMemberList(queryMemberRequest)
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        list.forEach(item -> {
            String reviewId = item.getReviewId();
            List<String> userIds = getReviewUserIds(reviewId);
            item.setReviewerName(getReviewName(userIds, userMap));
        });
        return list;
    }

    private List<String> getReviewUserIds(String reviewId) {
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);
        return testCaseReviewUsers.stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());
    }

    private String getReviewName(List<String> userIds, Map<String, String> userMap) {
        List<String> userNames = new ArrayList<>();
        if (userIds.size() > 0) {
            for (String id : userIds) {
                String n = userMap.get(id);
                if (StringUtils.isNotBlank(n)) {
                    userNames.add(n);
                }
            }
        }
        return StringUtils.join(userNames, "、");
    }

    public int deleteTestCase(DeleteRelevanceRequest request) {
        checkReviewer(request.getReviewId());
        return testCaseReviewTestCaseMapper.deleteByPrimaryKey(request.getId());
    }

    private void checkReviewer(String reviewId) {
        List<String> userIds = testCaseReviewService.getTestCaseReviewerIds(reviewId);
        String currentId = SessionUtils.getUser().getId();
        TestCaseReview caseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);
        String creator = "";
        if (caseReview != null) {
            creator = caseReview.getCreator();
        }
        if (!userIds.contains(currentId) && !StringUtils.equals(creator, currentId)) {
            MSException.throwException("没有权限，不能解除用例关联！");
        }
    }

    public void deleteTestCaseBatch(TestReviewCaseBatchRequest request) {
        checkReviewer(request.getReviewId());
        TestCaseReviewTestCaseExample example = new TestCaseReviewTestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        testCaseReviewTestCaseMapper.deleteByExample(example);
    }

    public void editTestCase(TestCaseReviewTestCase testCaseReviewTestCase) {
        checkReviewCase(testCaseReviewTestCase.getReviewId());

        // 记录测试用例评审状态变更
        testCaseReviewTestCase.setStatus(testCaseReviewTestCase.getStatus());
        testCaseReviewTestCase.setReviewer(SessionUtils.getUser().getId());
        testCaseReviewTestCase.setUpdateTime(System.currentTimeMillis());
        testCaseReviewTestCaseMapper.updateByPrimaryKeySelective(testCaseReviewTestCase);

        // 修改用例评审状态
        String caseId = testCaseReviewTestCase.getCaseId();
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        testCase.setId(caseId);
        testCase.setReviewStatus(testCaseReviewTestCase.getStatus());
        testCaseMapper.updateByPrimaryKeySelective(testCase);
    }

    public List<TestReviewCaseDTO> getTestCaseReviewDTOList(QueryCaseReviewRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extTestReviewCaseMapper.list(request);
    }

    public TestReviewCaseDTO get(String reviewId) {
        return extTestReviewCaseMapper.get(reviewId);
    }

    public void editTestCaseBatchStatus(TestReviewCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        if (StringUtils.isBlank(request.getReviewId())) {
            return;
        } else {
            checkReviewCase(request.getReviewId());
        }

        // 更新状态
        if (StringUtils.isNotBlank(request.getStatus())) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(ids);
            TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
            testCase.setReviewStatus(request.getStatus());
            testCaseMapper.updateByExampleSelective(testCase, example);
        }
    }

    private void checkReviewCase(String reviewId) {
        String currentUserId = SessionUtils.getUser().getId();
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);
        List<String> reviewIds = testCaseReviewUsers.stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());
        if (!reviewIds.contains(currentUserId)) {
            MSException.throwException("非此用例的评审人员！");
        }
    }
}
