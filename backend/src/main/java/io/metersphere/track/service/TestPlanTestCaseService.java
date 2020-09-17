package io.metersphere.track.service;

import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.domain.TestPlanTestCaseExample;
import io.metersphere.base.domain.TestPlanTestCaseWithBLOBs;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.request.testcase.TestPlanCaseBatchRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanTestCaseService {

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;

    @Resource
    UserService userService;

    @Resource
    TestPlanService testPlanService;

    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;

    public List<TestPlanCaseDTO> list(QueryTestPlanCaseRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.list(request);
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        Map<String, String> userMap = userService.getMemberList(queryMemberRequest)
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        list.forEach(item -> {
            item.setExecutorName(userMap.get(item.getExecutor()));
        });
        return list;
    }

    public List<TestPlanCaseDTO> listByNode(QueryTestPlanCaseRequest request) {
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.listByNode(request);
        return list;
    }

    public List<TestPlanCaseDTO> listByNodes(QueryTestPlanCaseRequest request) {
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.listByNodes(request);
        return list;
    }

    public void editTestCase(TestPlanTestCaseWithBLOBs testPlanTestCase) {
        if (StringUtils.equals(TestPlanTestCaseStatus.Prepare.name(), testPlanTestCase.getStatus())) {
            testPlanTestCase.setStatus(TestPlanTestCaseStatus.Underway.name());
        }
        testPlanTestCase.setExecutor(SessionUtils.getUser().getId());
        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCaseMapper.updateByPrimaryKeySelective(testPlanTestCase);
    }

    public int deleteTestCase(String id) {
        return testPlanTestCaseMapper.deleteByPrimaryKey(id);
    }

    public void editTestCaseBath(TestPlanCaseBatchRequest request) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andIdIn(request.getIds());

        TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
        BeanUtils.copyBean(testPlanTestCase, request);
        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCaseMapper.updateByExampleSelective(
                testPlanTestCase,
                testPlanTestCaseExample);
    }

    public List<TestPlanCaseDTO> getRecentTestCases(QueryTestPlanCaseRequest request, int count) {
        buildQueryRequest(request, count);
        if (request.getPlanIds().isEmpty()) {
            return new ArrayList<>();
        }

        List<TestPlanCaseDTO> recentTestedTestCase = extTestPlanTestCaseMapper.getRecentTestedTestCase(request);
        List<String> planIds = recentTestedTestCase.stream().map(TestPlanCaseDTO::getPlanId).collect(Collectors.toList());

        if (planIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> testPlanMap = testPlanService.getTestPlanByIds(planIds).stream()
                .collect(Collectors.toMap(TestPlan::getId, TestPlan::getName));

        recentTestedTestCase.forEach(testCase -> {
            testCase.setPlanName(testPlanMap.get(testCase.getPlanId()));
        });
        return recentTestedTestCase;
    }

    public List<TestPlanCaseDTO> getPendingTestCases(QueryTestPlanCaseRequest request, int count) {
        buildQueryRequest(request, count);
        if (request.getPlanIds().isEmpty()) {
            return new ArrayList<>();
        }
        return extTestPlanTestCaseMapper.getPendingTestCases(request);
    }

    public void buildQueryRequest(QueryTestPlanCaseRequest request, int count) {
        SessionUser user = SessionUtils.getUser();
        List<String> relateTestPlanIds = extTestPlanTestCaseMapper.findRelateTestPlanId(user.getId(), SessionUtils.getCurrentWorkspaceId());
        PageHelper.startPage(1, count, true);
        request.setPlanIds(relateTestPlanIds);
        request.setExecutor(user.getId());
    }

    public TestPlanCaseDTO get(String caseId) {
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setId(caseId);
        return extTestPlanTestCaseMapper.list(request).get(0);
    }

    public void deleteTestCaseBath(TestPlanCaseBatchRequest request) {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        testPlanTestCaseMapper.deleteByExample(example);
    }
}
