package io.metersphere.track.service;

import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.service.ApiDefinitionExecResultService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import io.metersphere.track.request.testcase.TestPlanCaseBatchRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
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
public class TestPlanApiCaseService {

    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    ApiTestCaseService apiTestCaseService;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    UserService userService;
    @Resource
    TestPlanService testPlanService;

    public List<TestPlanApiCaseDTO> list(ApiTestCaseRequest request) {
        request.setProjectId(null);
//        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
//        example.createCriteria().andTestPlanIdEqualTo(request.getPlanId());
//        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(example);
//        if (CollectionUtils.isEmpty(testPlanApiCases)) {
//            return new ArrayList<>();
//        }
//        List<String> caseIds = testPlanApiCases.stream().collect(Collectors.toMap((TestPlanApiCase::getApiCaseId + TestPlanApiCase::getTestPlanId)->{
//
//        }));
//        request.setIds(caseIds);
//        return apiTestCaseService.listSimple(request);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<TestPlanApiCaseDTO> apiTestCases = extTestPlanApiCaseMapper.list(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        apiTestCaseService.buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public List<ApiTestCaseDTO> relevanceList(ApiTestCaseRequest request) {
        List<String> ids = apiTestCaseService.selectIdsNotExistsInPlan(request.getProjectId(), request.getPlanId());
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        request.setIds(ids);
        return apiTestCaseService.listSimple(request);
    }

    public int delete(String planId, String id) {
        apiDefinitionExecResultService.deleteByResourceId(id);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andIdEqualTo(id);

        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
        apiDefinitionExecResultService.deleteByResourceIds(request.getIds());
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andIdIn(request.getIds())
                .andTestPlanIdEqualTo(request.getPlanId());
        testPlanApiCaseMapper.deleteByExample(example);
    }
//
//    public List<TestPlanCaseDTO> listByNode(QueryTestPlanCaseRequest request) {
//        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.listByNode(request);
//        return list;
//    }
//
//    public List<TestPlanCaseDTO> listByNodes(QueryTestPlanCaseRequest request) {
//        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.listByNodes(request);
//        return list;
//    }
//
//    public void editTestCase(TestPlanTestCaseWithBLOBs testPlanTestCase) {
//        if (StringUtils.equals(TestPlanTestCaseStatus.Prepare.name(), testPlanTestCase.getStatus())) {
//            testPlanTestCase.setStatus(TestPlanTestCaseStatus.Underway.name());
//        }
//        testPlanTestCase.setExecutor(SessionUtils.getUser().getId());
//        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
//        testPlanTestCaseMapper.updateByPrimaryKeySelective(testPlanTestCase);
//    }
//
//    public int deleteTestCase(String id) {
//        return testPlanTestCaseMapper.deleteByPrimaryKey(id);
//    }
//
//    public void editTestCaseBath(TestPlanCaseBatchRequest request) {
//        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
//        testPlanTestCaseExample.createCriteria().andIdIn(request.getIds());
//
//        TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
//        BeanUtils.copyBean(testPlanTestCase, request);
//        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
//        testPlanTestCaseMapper.updateByExampleSelective(
//                testPlanTestCase,
//                testPlanTestCaseExample);
//    }
//
//    public List<TestPlanCaseDTO> getRecentTestCases(QueryTestPlanCaseRequest request, int count) {
//        buildQueryRequest(request, count);
//        if (request.getPlanIds().isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        List<TestPlanCaseDTO> recentTestedTestCase = extTestPlanTestCaseMapper.getRecentTestedTestCase(request);
//        List<String> planIds = recentTestedTestCase.stream().map(TestPlanCaseDTO::getPlanId).collect(Collectors.toList());
//
//        if (planIds.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        Map<String, String> testPlanMap = testPlanService.getTestPlanByIds(planIds).stream()
//                .collect(Collectors.toMap(TestPlan::getId, TestPlan::getName));
//
//        recentTestedTestCase.forEach(testCase -> {
//            testCase.setPlanName(testPlanMap.get(testCase.getPlanId()));
//        });
//        return recentTestedTestCase;
//    }
//
//    public List<TestPlanCaseDTO> getPendingTestCases(QueryTestPlanCaseRequest request, int count) {
//        buildQueryRequest(request, count);
//        if (request.getPlanIds().isEmpty()) {
//            return new ArrayList<>();
//        }
//        return extTestPlanTestCaseMapper.getPendingTestCases(request);
//    }
//
//    public void buildQueryRequest(QueryTestPlanCaseRequest request, int count) {
//        SessionUser user = SessionUtils.getUser();
//        List<String> relateTestPlanIds = extTestPlanTestCaseMapper.findRelateTestPlanId(user.getId(), SessionUtils.getCurrentWorkspaceId(), SessionUtils.getCurrentProjectId());
//        PageHelper.startPage(1, count, true);
//        request.setPlanIds(relateTestPlanIds);
//        request.setExecutor(user.getId());
//    }
//
//    public TestPlanCaseDTO get(String testplanTestCaseId) {
//        return extTestPlanTestCaseMapper.get(testplanTestCaseId);
//    }
//
//    public void deleteTestCaseBath(TestPlanCaseBatchRequest request) {
//        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
//        example.createCriteria().andIdIn(request.getIds());
//        testPlanTestCaseMapper.deleteByExample(example);
//    }
//
//    public List<String> getTestPlanTestCaseIds(String testId) {
//        return extTestPlanTestCaseMapper.getTestPlanTestCaseIds(testId);
//    }
//
//    public int updateTestCaseStates(List<String> ids, String reportStatus) {
//        return extTestPlanTestCaseMapper.updateTestCaseStates(ids, reportStatus);
//    }
}
