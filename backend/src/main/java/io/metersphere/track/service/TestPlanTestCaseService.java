package io.metersphere.track.service;

import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.base.domain.TestPlanTestCaseExample;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
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
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;

    public List<TestPlanCaseDTO> list(QueryTestPlanCaseRequest request) {
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

    public void editTestCase(TestPlanTestCase testPlanTestCase) {
        if (StringUtils.equals(TestPlanTestCaseStatus.Prepare.name(), testPlanTestCase.getStatus())) {
            testPlanTestCase.setStatus(TestPlanTestCaseStatus.Underway.name());
        }
        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCaseMapper.updateByPrimaryKeySelective(testPlanTestCase);
    }

    public int deleteTestCase(String id) {
        return testPlanTestCaseMapper.deleteByPrimaryKey(id);
    }

    public void editTestCaseBath(TestPlanCaseBatchRequest request) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andIdIn(request.getIds());

        TestPlanTestCase testPlanTestCase = new TestPlanTestCase();
        BeanUtils.copyBean(testPlanTestCase, request);
        testPlanTestCaseMapper.updateByExampleSelective(
                testPlanTestCase,
                testPlanTestCaseExample);
    }

    public List<TestPlanCaseDTO> getRecentTestCases(QueryTestPlanCaseRequest request, int count) {
        buildQueryRequest(request, count);
        if (request.getPlanIds().isEmpty()) {
            return new ArrayList<>();
        }
        return extTestPlanTestCaseMapper.getRecentTestedTestCase(request);
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
        List<String> relateTestPlanIds = extTestPlanTestCaseMapper.findRelateTestPlanId(user.getId());
        PageHelper.startPage(1, count, true);
        request.setPlanIds(relateTestPlanIds);
        request.setExecutor(user.getId());
    }

}
