package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestCaseTestDTO;
import io.metersphere.track.dto.TestReviewCaseDTO;
import io.metersphere.track.request.testplancase.TestReviewCaseBatchRequest;
import io.metersphere.track.request.testreview.DeleteRelevanceRequest;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestReviewTestCaseService {
    @Resource
    private TestCaseTestMapper testCaseTestMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
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
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;

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

    public List<String> selectIds(QueryCaseReviewRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<String> list = extTestReviewCaseMapper.selectIds(request);
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
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }
        TestCaseReviewTestCaseExample example = new TestCaseReviewTestCaseExample();
        example.createCriteria().andIdIn(ids);
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
        TestReviewCaseDTO testReviewCaseDTO = extTestReviewCaseMapper.get(reviewId);
        List<TestCaseTestDTO> testCaseTestDTOS = extTestPlanTestCaseMapper.listTestCaseTest(testReviewCaseDTO.getCaseId());
        testCaseTestDTOS.forEach(dto -> {
            setTestName(dto);
        });
        testReviewCaseDTO.setList(testCaseTestDTOS);
        return testReviewCaseDTO;
    }

    private void setTestName(TestCaseTestDTO dto) {
        String type = dto.getTestType();
        String id = dto.getTestId();
        switch (type) {
            case "performance":
                LoadTest loadTest = loadTestMapper.selectByPrimaryKey(id);
                if (loadTest != null) {
                    dto.setTestName(loadTest.getName());
                }
                break;
            case "testcase":
                ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(id);
                if (apiTestCaseWithBLOBs != null) {
                    dto.setTestName(apiTestCaseWithBLOBs.getName());
                }
                break;
            case "automation":
                ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(id);
                if (apiScenarioWithBLOBs != null) {
                    dto.setTestName(apiScenarioWithBLOBs.getName());
                }
                break;
            default:
                break;
        }
    }

    public void editTestCaseBatchStatus(TestReviewCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = extTestReviewCaseMapper.selectTestCaseIds(request.getCondition());
            if (request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }
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

    public void editTestCaseForMinder(List<TestCaseReviewTestCase> testCaseReviewTestCases) {
        if (!CollectionUtils.isEmpty(testCaseReviewTestCases)) {
            List<TestCaseWithBLOBs> testCaseList = new ArrayList<>();
            testCaseReviewTestCases.forEach((item) -> {
                TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
                testCase.setId(item.getCaseId());
                testCase.setReviewStatus(item.getStatus());
                testCaseList.add(testCase);
                testCase.setUpdateTime(System.currentTimeMillis());
                item.setUpdateTime(System.currentTimeMillis());
                testCaseReviewTestCaseMapper.updateByPrimaryKeySelective(item);
            });
            testCaseList.forEach(testCaseMapper::updateByPrimaryKeySelective);
        }
    }

    public String getLogDetails(DeleteRelevanceRequest request) {
        TestCaseReview caseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        TestCaseReviewTestCase testCaseReviewTestCase = testCaseReviewTestCaseMapper.selectByPrimaryKey(request.getId());
        StringBuilder titleBuilder = new StringBuilder();
        if (testCaseReviewTestCase != null) {
            TestCaseWithBLOBs bloBs = testCaseMapper.selectByPrimaryKey(testCaseReviewTestCase.getCaseId());
            if (bloBs != null) {
                titleBuilder.append(bloBs.getName()).append(" 从 ");
            }
        }
        if (caseReview != null) {
            titleBuilder.append(caseReview.getName());
        }
        if (StringUtils.isNotEmpty(titleBuilder.toString())) {
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getId()), caseReview.getProjectId(), titleBuilder.toString(), caseReview.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(TestReviewCaseBatchRequest request) {
        TestCaseReview caseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        TestCaseReviewTestCaseExample example = new TestCaseReviewTestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        List<TestCaseReviewTestCase> testCaseReviewTestCases = testCaseReviewTestCaseMapper.selectByExample(example);
        StringBuilder titleBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(testCaseReviewTestCases)) {
            testCaseReviewTestCases.forEach(item -> {
                TestCaseWithBLOBs bloBs = testCaseMapper.selectByPrimaryKey(item.getCaseId());
                if (bloBs != null) {
                    titleBuilder.append(bloBs.getName()).append(",");
                }
            });
            titleBuilder.append(" 从 ");
        }
        if (caseReview != null) {
            titleBuilder.append(caseReview.getName());
        }
        if (StringUtils.isNotEmpty(titleBuilder.toString())) {
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getId()), caseReview.getProjectId(), titleBuilder.toString(), caseReview.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String batchLogDetails(TestReviewCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = extTestReviewCaseMapper.selectTestCaseIds(request.getCondition());
            if (request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        // 更新状态
        if (StringUtils.isNotBlank(request.getStatus())) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(ids);
            List<TestCase> cases = testCaseMapper.selectByExample(example);

            List<DetailColumn> columns = new LinkedList<>();
            List<String> names = cases.stream().map(TestCase::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), cases.get(0).getProjectId(), String.join(",", names) + "评审结果更改为：" + "【" + StatusReference.statusMap.get(request.getStatus()) + "】", cases.get(0).getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<TestCaseReviewTestCase> testCases) {
        // 更新状态
        if (CollectionUtils.isNotEmpty(testCases)) {
            List<DetailColumn> columns = new LinkedList<>();
            TestCaseExample example = new TestCaseExample();
            List<String> ids = testCases.stream().map(TestCaseReviewTestCase::getCaseId).collect(Collectors.toList());
            example.createCriteria().andIdIn(ids);
            List<TestCase> cases = testCaseMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(cases)) {
                List<String> names = cases.stream().map(TestCase::getName).collect(Collectors.toList());
                List<TestCase> collect = cases.stream().filter(u -> StringUtils.isNotEmpty(u.getProjectId())).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), CollectionUtils.isNotEmpty(collect) ? collect.get(0).getProjectId() : null, String.join(",", names), cases.get(0).getCreateUser(), columns);
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getLogDetails(TestCaseReviewTestCase request) {
        TestCaseReview caseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        TestCaseWithBLOBs caseWithBLOBs = testCaseMapper.selectByPrimaryKey(request.getCaseId());
        if (caseWithBLOBs != null) {
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getId()), caseReview.getProjectId(), caseWithBLOBs.getName(), caseReview.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<TestReviewCaseDTO> listForMinder(QueryCaseReviewRequest request) {
        return extTestReviewCaseMapper.listForMinder(request);
    }
}
