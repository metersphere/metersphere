package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewMapper;
import io.metersphere.base.mapper.TestCaseReviewTestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewUsersMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.dto.TestReviewCaseDTO;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.request.testplancase.TestReviewCaseBatchRequest;
import io.metersphere.request.testreview.DeleteRelevanceRequest;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
import io.metersphere.request.testreview.TestCaseReviewTestCaseEditRequest;
import io.metersphere.xpack.version.service.ProjectVersionService;
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
    ExtTestReviewCaseMapper extTestReviewCaseMapper;
    @Resource
    BaseUserService baseUserService;
    @Resource
    TestCaseReviewTestCaseMapper testCaseReviewTestCaseMapper;
    @Resource
    TestCaseReviewUsersMapper testCaseReviewUsersMapper;
    @Resource
    TestCaseReviewMapper testCaseReviewMapper;
    @Resource
    io.metersphere.service.TestCaseReviewService testCaseReviewService;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    io.metersphere.service.TestCaseService testCaseService;
//    @Resource
//    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    io.metersphere.service.TestCaseCommentService testCaseCommentService;

    public List<TestReviewCaseDTO> list(QueryCaseReviewRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<TestReviewCaseDTO> list = extTestReviewCaseMapper.list(request);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }

        ServiceUtils.buildVersionInfo(list);
        ServiceUtils.buildProjectInfo(list);
        ServiceUtils.buildCustomNumInfo(list);

        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setWorkspaceId(SessionUtils.getCurrentProjectId());
        Map<String, String> userMap = baseUserService.getMemberList(queryMemberRequest)
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        List<String> versionIds = list.stream().map(TestReviewCaseDTO::getVersionId).collect(Collectors.toList());
        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
        if (projectVersionService != null) {
            Map<String, String> projectVersionMap = projectVersionService.getProjectVersionByIds(versionIds).stream()
                    .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
            list.forEach(item -> {
                item.setVersionName(projectVersionMap.get(item.getVersionId()));
            });
        }

        list.forEach(item -> {
            String reviewId = item.getReviewId();
            List<String> userIds = getReviewUserIds(reviewId);
            item.setReviewerName(getReviewName(userIds, userMap));
            item.setMaintainerName(userMap.get(item.getMaintainer()));
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

    public int deleteToGc(List<String> caseIds) {
        return updateIsDel(caseIds, true);
    }

    private int updateIsDel(List<String> caseIds, Boolean isDel) {
        if (CollectionUtils.isEmpty(caseIds)) {
            return 0;
        }
        TestCaseReviewTestCaseExample example = new TestCaseReviewTestCaseExample();
        example.createCriteria().andCaseIdIn(caseIds);
        TestCaseReviewTestCase record = new TestCaseReviewTestCase();
        record.setIsDel(isDel);
        return testCaseReviewTestCaseMapper.updateByExampleSelective(record, example);
    }

    private void checkReviewer(String reviewId) {
        List<String> userIds = testCaseReviewService.getTestCaseReviewerIds(reviewId);
        String currentId = SessionUtils.getUser().getId();
        TestCaseReview caseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);
        String creator = StringUtils.EMPTY;
        if (caseReview != null) {
            creator = caseReview.getCreator();
        }
        if (!userIds.contains(currentId) && !StringUtils.equals(creator, currentId)) {
            MSException.throwException("没有权限，不能解除用例关联！");
        }
    }

    public void deleteTestCaseBatch(TestReviewCaseBatchRequest request) {
        checkReviewer(request.getReviewId());
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestReviewCaseMapper.selectIds((QueryCaseReviewRequest) query));

        List<String> ids = request.getIds();
        TestCaseReviewTestCaseExample example = new TestCaseReviewTestCaseExample();
        example.createCriteria().andIdIn(ids);
        testCaseReviewTestCaseMapper.deleteByExample(example);
    }

    public void editTestCase(TestCaseReviewTestCaseEditRequest testCaseReviewTestCase) {
        checkReviewCase(testCaseReviewTestCase.getReviewId());

        // 记录测试用例评审状态变更
        testCaseReviewTestCase.setStatus(testCaseReviewTestCase.getStatus());
        testCaseReviewTestCase.setReviewer(SessionUtils.getUser().getId());
        testCaseReviewTestCase.setUpdateTime(System.currentTimeMillis());
        testCaseReviewTestCaseMapper.updateByPrimaryKeySelective(testCaseReviewTestCase);

        // 修改用例评审状态
        testCaseService.updateReviewStatus(testCaseReviewTestCase.getCaseId(), testCaseReviewTestCase.getStatus());

        if (StringUtils.isNotEmpty(testCaseReviewTestCase.getComment())) {
            // 走Spring的代理对象，防止发送通知操作记录等操作失效
            testCaseCommentService.saveComment(testCaseReviewTestCase);
        }
    }

    public TestReviewCaseDTO get(String reviewId) {
        TestReviewCaseDTO testReviewCaseDTO = extTestReviewCaseMapper.get(reviewId);
        return testReviewCaseDTO;
    }

    public void editTestCaseBatchStatus(TestReviewCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestReviewCaseMapper.selectTestCaseIds((QueryCaseReviewRequest) query));

        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        if (StringUtils.isBlank(request.getReviewId())) {
            return;
        } else {
            checkReviewCase(request.getReviewId());
        }

        // 更新状态{TestCase, TestCaseReviewTestCase}
        if (StringUtils.isNotBlank(request.getStatus())) {
            testCaseService.updateReviewStatus(ids, request.getStatus());

            TestCaseReviewTestCaseExample caseReviewTestCaseExample = new TestCaseReviewTestCaseExample();
            caseReviewTestCaseExample.createCriteria().andReviewIdEqualTo(request.getReviewId()).andCaseIdIn(ids);
            TestCaseReviewTestCase testCaseReviewTestCase = new TestCaseReviewTestCase();
            testCaseReviewTestCase.setStatus(request.getStatus());
            testCaseReviewTestCaseMapper.updateByExampleSelective(testCaseReviewTestCase, caseReviewTestCaseExample);
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

    public void editTestCaseForMinder(String reviewId, List<TestCaseReviewTestCase> testCaseReviewTestCases) {
        checkReviewCase(reviewId);
        if (!CollectionUtils.isEmpty(testCaseReviewTestCases)) {
            testCaseReviewTestCases.forEach((item) -> {
                item.setUpdateTime(System.currentTimeMillis());
                testCaseReviewTestCaseMapper.updateByPrimaryKeySelective(item);
                testCaseService.updateReviewStatus(item.getCaseId(), item.getStatus());
            });
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
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestReviewCaseMapper.selectTestCaseIds((QueryCaseReviewRequest) query));
        List<String> ids = request.getIds();

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
        List<OrderRequest> orders = ServiceUtils.getDefaultSortOrder("tcrtc", request.getOrders());
        orders.forEach(order -> {
            if (order.getName().equals("update_time")) {
                order.setPrefix("tcrtc");
            }
        });
        request.setOrders(orders);
        return extTestReviewCaseMapper.listForMinder(request);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestCaseReviewTestCase.class, TestCaseReviewTestCaseMapper.class,
                extTestReviewCaseMapper::selectReviewIds,
                extTestReviewCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestCaseReviewTestCase.class,
                testCaseReviewTestCaseMapper::selectByPrimaryKey,
                extTestReviewCaseMapper::getPreOrder,
                extTestReviewCaseMapper::getLastOrder,
                testCaseReviewTestCaseMapper::updateByPrimaryKeySelective);
    }

    public int reduction(List<String> ids) {
        return updateIsDel(ids, false);
    }
}
