package io.metersphere.track.service;

import io.metersphere.base.domain.*;
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

    public int deleteTestCase(String id) {
        return testCaseReviewTestCaseMapper.deleteByPrimaryKey(id);
    }

    public void deleteTestCaseBath(TestReviewCaseBatchRequest request) {
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
        testCaseReviewTestCase.setStatus(testCaseReviewTestCase.getStatus());
        testCaseReviewTestCase.setReviewer(SessionUtils.getUser().getId());
        testCaseReviewTestCase.setUpdateTime(System.currentTimeMillis());
        testCaseReviewTestCaseMapper.updateByPrimaryKeySelective(testCaseReviewTestCase);
    }
}
