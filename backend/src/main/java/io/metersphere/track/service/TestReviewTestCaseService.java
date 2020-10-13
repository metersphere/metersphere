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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    private String getReviewName(List<String> userIds, Map userMap) {
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

    public int deleteTestCase(DeleteRelevanceRequest request) {
        checkReviewer(request.getReviewId());
        return testCaseReviewTestCaseMapper.deleteByPrimaryKey(request.getId());
    }

    private void checkReviewer(String reviewId) {
        List<String> userIds = testCaseReviewService.getTestCaseReviewerIds(reviewId);
        String currentId = SessionUtils.getUser().getId();
        if (!userIds.contains(currentId)) {
            MSException.throwException("非用例评审人员，不能解除用例关联！");
        }
    }

    public void deleteTestCaseBath(TestReviewCaseBatchRequest request) {
        checkReviewer(request.getReviewId());
        TestCaseReviewTestCaseExample example = new TestCaseReviewTestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        testCaseReviewTestCaseMapper.deleteByExample(example);
    }

    public void editTestCase(TestCaseReviewTestCase testCaseReviewTestCase) {
        String currentUserId = SessionUtils.getUser().getId();
        String reviewId = testCaseReviewTestCase.getReviewId();
        TestCaseReviewUsersExample testCaseReviewUsersExample = new TestCaseReviewUsersExample();
        testCaseReviewUsersExample.createCriteria().andReviewIdEqualTo(reviewId);
        List<TestCaseReviewUsers> testCaseReviewUsers = testCaseReviewUsersMapper.selectByExample(testCaseReviewUsersExample);
        List<String> reviewIds = testCaseReviewUsers.stream().map(TestCaseReviewUsers::getUserId).collect(Collectors.toList());
        if (!reviewIds.contains(currentUserId)) {
            MSException.throwException("非此用例的评审人员！");
        }

        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);
        Long endTime = testCaseReview.getEndTime();
        if (System.currentTimeMillis() > endTime) {
            MSException.throwException("此用例评审已到截止时间！");
        }

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
}
