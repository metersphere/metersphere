package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.constants.TestCaseReviewStatus;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.TestCaseCommentType;
import io.metersphere.constants.TestCaseReviewCommentStatus;
import io.metersphere.constants.TestCaseReviewPassRule;
import io.metersphere.dto.TestCaseCommentDTO;
import io.metersphere.dto.TestReviewCaseDTO;
import io.metersphere.excel.converter.TestReviewCaseStatus;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.request.testplancase.TestReviewCaseBatchRequest;
import io.metersphere.request.testreview.DeleteRelevanceRequest;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
import io.metersphere.request.testreview.TestCaseReviewTestCaseEditRequest;
import io.metersphere.utils.ListUtil;
import io.metersphere.xpack.version.service.ProjectVersionService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
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
    ExtTestCaseReviewTestCaseMapper extTestCaseReviewTestCaseMapper;
    @Resource
    TestCaseReviewUsersMapper testCaseReviewUsersMapper;
    @Resource
    TestCaseReviewMapper testCaseReviewMapper;
    @Resource
    @Lazy
    TestCaseReviewService testCaseReviewService;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    TestCaseService testCaseService;
    @Resource
    TestCaseCommentService testCaseCommentService;
    @Resource
    TestCaseReviewTestCaseUsersMapper testCaseReviewTestCaseUsersMapper;
    @Resource
    TestCaseReviewTestCaseUsersService testCaseReviewTestCaseUsersService;

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
            String caseId = item.getCaseId();
            List<String> userIds = getReviewUserIds(reviewId, caseId);
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
        TestCaseReviewTestCase testCaseReviewTestCase = testCaseReviewTestCaseMapper.selectByPrimaryKey(request.getId());
        if (testCaseReviewTestCase != null) {
            TestCaseReviewTestCaseUsersExample testCaseReviewTestCaseUsersExample = new TestCaseReviewTestCaseUsersExample();
            testCaseReviewTestCaseUsersExample.createCriteria().andReviewIdEqualTo(request.getReviewId()).andCaseIdEqualTo(testCaseReviewTestCase.getCaseId());
            testCaseReviewTestCaseUsersMapper.deleteByExample(testCaseReviewTestCaseUsersExample);
        }
        testCaseCommentService.deleteByBelongIdAndCaseId(testCaseReviewTestCase.getCaseId(), request.getReviewId());
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
        List<TestCaseReviewTestCase> testCaseReviewTestCases = testCaseReviewTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testCaseReviewTestCases)) {
            List<String> caseIds = testCaseReviewTestCases.stream().map(TestCaseReviewTestCase::getCaseId).collect(Collectors.toList());
            TestCaseReviewTestCaseUsersExample testCaseReviewTestCaseUsersExample = new TestCaseReviewTestCaseUsersExample();
            testCaseReviewTestCaseUsersExample.createCriteria().andReviewIdEqualTo(request.getReviewId()).andCaseIdIn(caseIds);
            testCaseReviewTestCaseUsersMapper.deleteByExample(testCaseReviewTestCaseUsersExample);
        }
        testCaseReviewTestCaseMapper.deleteByExample(example);
    }

    public String editTestCase(TestCaseReviewTestCaseEditRequest testCaseReviewTestCase) {
        List<String> caseIds = new ArrayList<>();
        caseIds.add(testCaseReviewTestCase.getCaseId());
        checkReviewCase(testCaseReviewTestCase.getReviewId(), caseIds);

        String originStatus = testCaseReviewTestCase.getStatus();
        TestCaseReview testReview = testCaseReviewService.getTestReview(testCaseReviewTestCase.getReviewId());
        String status = updateReviewCaseStatusForEdit(testCaseReviewTestCase, testReview.getReviewPassRule());

        // 添加评论，评论的状态不变，按照用户填写的
        testCaseReviewTestCase.setStatus(originStatus);
        if (StringUtils.isNotEmpty(testCaseReviewTestCase.getComment())) {
            // 走Spring的代理对象，防止发送通知操作记录等操作失效
            testCaseCommentService.saveReviewComment(testCaseReviewTestCase);
        } else {
            // 如果只是通过没有加评论内容不通知，添加空评论
            testCaseReviewTestCase.setComment(StringUtils.EMPTY);
            testCaseCommentService.saveReviewCommentWithoutNotification(testCaseReviewTestCase);
        }
        return status;
    }

    public String updateReviewCaseStatusForEdit(TestCaseReviewTestCaseEditRequest testCaseReviewTestCase, String reviewPassRule) {
        List<TestCaseCommentDTO> comments =
                testCaseCommentService.getStatusCaseComments(testCaseReviewTestCase.getCaseId(), TestCaseCommentType.REVIEW.name(), testCaseReviewTestCase.getReviewId());

        comments = filterAgainComments(comments);

        // 添加当前的状态修改，便于后面做统一判断
        TestCaseCommentDTO editComment = new TestCaseCommentDTO();
        editComment.setAuthor(SessionUtils.getUserId());
        editComment.setStatus(testCaseReviewTestCase.getStatus());
        editComment.setDescription(testCaseReviewTestCase.getComment());
        comments.add(0, editComment);

        String status = updateReviewCaseStatus(testCaseReviewTestCase, reviewPassRule, comments, null);
        return status;
    }

    /**
     * 获取每个评审人的评审结果
     *
     * @return
     */
    public List<TestCaseComment> getReviewerStatusComment(String id) {
        TestCaseReviewTestCase testCaseReviewTestCase = testCaseReviewTestCaseMapper.selectByPrimaryKey(id);

        List<TestCaseCommentDTO> comments =
                testCaseCommentService.getStatusCaseComments(testCaseReviewTestCase.getCaseId(), TestCaseCommentType.REVIEW.name(), testCaseReviewTestCase.getReviewId());

        String reviewPassRule = testCaseReviewService.getTestReview(testCaseReviewTestCase.getReviewId())
                .getReviewPassRule();

        List<String> users = testCaseReviewTestCaseUsersService.getUsersByCaseId(testCaseReviewTestCase.getCaseId());
        Set<String> reviewerSet = users.stream().collect(Collectors.toSet());

        comments = filterAgainComments(comments);
        comments = distinctUserComment(comments, reviewerSet);
        Map<String, String> userCommentMap = comments.stream()
                .filter(item -> StringUtils.equalsAny(item.getStatus(), TestCaseReviewCommentStatus.Pass.name(), TestCaseReviewCommentStatus.UnPass.name()))
                .collect(Collectors.toMap(TestCaseComment::getAuthor, TestCaseComment::getStatus));

        users = users.stream()
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> userNameMap = ServiceUtils.getUserNameMap(users);

        List<TestCaseComment> result = new ArrayList<>();
        for (String user : users) {
            TestCaseComment comment = new TestCaseComment();
            if (userCommentMap.containsKey(user)) {
                comment.setStatus(userCommentMap.get(user));
            } else if (StringUtils.equals(reviewPassRule, TestCaseReviewPassRule.ALL.name())) {
                // 多人评审才展示未评审的人
                comment.setStatus(TestCaseReviewStatus.Prepare.name());
            } else {
                continue;
            }
            comment.setAuthor(userNameMap.get(user));
            result.add(comment);
        }

        return result;
    }

    private String updateReviewCaseStatus(TestCaseReviewTestCase testCaseReviewTestCase, String reviewPassRule,
                                          List<TestCaseCommentDTO> comments, Consumer<String> handleStatusChangeFunc) {
        TestCaseReviewTestCase originReviewTestCase = testCaseReviewTestCaseMapper.selectByPrimaryKey(testCaseReviewTestCase.getId());

        // 初始化为原状态，计算完如果有修改才修改
        String originStatus = originReviewTestCase.getStatus();
        String status = originStatus;

        List<String> reviewers = testCaseReviewTestCaseUsersService.getUsersByCaseId(testCaseReviewTestCase.getCaseId());

        Set<String> reviewerSet = reviewers.stream().collect(Collectors.toSet());

        comments = distinctUserComment(comments, reviewerSet);

        List<TestCaseCommentDTO> passComments = comments.stream()
                .filter(comment -> StringUtils.equals(comment.getStatus(), TestCaseReviewCommentStatus.Pass.name()))
                .collect(Collectors.toList());

        List<TestCaseCommentDTO> unPassComments = comments.stream()
                .filter(comment -> StringUtils.equals(comment.getStatus(), TestCaseReviewCommentStatus.UnPass.name()))
                .collect(Collectors.toList());


        if (StringUtils.equals(TestCaseReviewPassRule.ALL.name(), reviewPassRule)) {
            // 全部通过
            Set<String> passUsers = passComments.stream()
                    .map(TestCaseComment::getAuthor)
                    .collect(Collectors.toSet());

            // 评审人是否都通过了
            if (reviewers.stream()
                    .filter(user -> passUsers.contains(user))
                    .collect(Collectors.toList()).size() == reviewers.size()) {
                // 如果所有人都通过了，则通过
                status = TestCaseReviewCommentStatus.Pass.name();
            } else {
                boolean hasUnPassComment = false;
                for (TestCaseCommentDTO comment : comments) {
                    hasUnPassComment = StringUtils.equals(comment.getStatus(), TestCaseReviewCommentStatus.UnPass.name())
                            || StringUtils.equals(comment.getStatus(), TestCaseReviewCommentStatus.ForceUnPass.name());
                    if (hasUnPassComment) {
                        // 如果评论中有不通过，或者是强制变更为不通过，则不通过
                        status = TestCaseReviewCommentStatus.UnPass.name();
                        break;
                    }
                }
                if (!hasUnPassComment && StringUtils.equals(originStatus, TestCaseReviewCommentStatus.Pass.name())) {
                    // 如果没有不通过的，则修改为评审中
                    status = TestCaseReviewStatus.Underway.name();
                }
            }
        } else {
            if (passComments.size() > 0) {
                // 单人通过, 如果有一人通过，则通过
                status = TestCaseReviewCommentStatus.Pass.name();
            } else if (unPassComments.size() > 0) {
                // 如果没有人通过，并且有人不通过，则不通过
                status = TestCaseReviewCommentStatus.UnPass.name();
            }
        }

        if (!StringUtils.equals(originStatus, status)
                && !StringUtils.equalsAny(originStatus, TestReviewCaseStatus.Underway.name(), TestReviewCaseStatus.Again.name(), TestReviewCaseStatus.Prepare.name())) {
            if (handleStatusChangeFunc != null) {
                handleStatusChangeFunc.accept(originStatus);
            }
            // 如果状态有变更，并且原来状态不是进行中或者重新提审，则添加一条状态为 StatusChange , 内容为变更前状态的评论，该评论页面不显示，作为生成流程图创建新节点的标志
            TestCaseReviewTestCaseEditRequest addCommentRequest = new TestCaseReviewTestCaseEditRequest();
            BeanUtils.copyBean(addCommentRequest, testCaseReviewTestCase);
            addCommentRequest.setStatus(TestCaseReviewCommentStatus.StatusChange.name());
            addCommentRequest.setComment(originStatus);
            testCaseCommentService.saveReviewCommentWithoutNotification(addCommentRequest);
        }

        // 如果状态有变化才更新
        if (!StringUtils.equals(originStatus, status)) {
            updateTestReviewTestCaseStatus(testCaseReviewTestCase, status);
        }
        return status;
    }

    /**
     * 只保留每个用户的最后一条有效评论
     *
     * @param comments
     * @param reviewerSet
     * @return
     */
    private List<TestCaseCommentDTO> distinctUserComment(List<TestCaseCommentDTO> comments, Set<String> reviewerSet) {
        // 只保留每个用户的最后一条有效评论
        Set<String> userSet = new HashSet<>();

        comments = comments.stream().filter(item -> {
            if (StringUtils.isBlank(item.getStatus()) || // 过滤没有状态的评论
                    StringUtils.equalsAny(item.getStatus(), TestCaseReviewCommentStatus.RuleChange.name(),
                            TestCaseReviewCommentStatus.StatusChange.name()) || // 过滤不影响结果的状态
                    userSet.contains(item.getAuthor())) { // 保留最新的一条评论
                return false;
            }
            // 必须是评审人
            if (reviewerSet.contains(item.getAuthor())) {
                userSet.add(item.getAuthor());
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return comments;
    }

    private void updateTestReviewTestCaseStatus(TestCaseReviewTestCase testCaseReviewTestCase, String status) {
        // 记录测试用例评审状态变更
        testCaseReviewTestCase.setStatus(status);
        testCaseReviewTestCase.setReviewer(SessionUtils.getUser().getId());
        testCaseReviewTestCase.setUpdateTime(System.currentTimeMillis());
        testCaseReviewTestCaseMapper.updateByPrimaryKeySelective(testCaseReviewTestCase);

        // 修改用例评审状态
        testCaseService.updateReviewStatus(testCaseReviewTestCase.getCaseId(), testCaseReviewTestCase.getStatus());
    }

    /**
     * 过滤掉重新提审之前的评论
     *
     * @param comments
     * @return
     */
    private List<TestCaseCommentDTO> filterAgainComments(List<TestCaseCommentDTO> comments) {
        for (int i = 0; i < comments.size(); i++) {
            TestCaseCommentDTO comment = comments.get(i);
            // 计算状态时，先按照创建时间降序，然后去掉重新提审之前的评论
            if (StringUtils.equals(comment.getStatus(), TestCaseReviewCommentStatus.Again.name())) {
                comments = comments.subList(0, i);
                break;
            }
        }
        return comments;
    }

    public TestReviewCaseDTO get(String reviewId) {
        TestReviewCaseDTO testReviewCaseDTO = extTestReviewCaseMapper.get(reviewId);
        testReviewCaseDTO.setFields(testCaseService.getCustomFieldByCaseId(testReviewCaseDTO.getCaseId()));
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
            checkReviewCase(request.getReviewId(), request.getIds());
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

        ids.forEach(i -> {
            String status = request.getStatus();
            if (StringUtils.equals(status, TestCaseReviewCommentStatus.Pass.name())) {
                status = TestCaseReviewCommentStatus.ForcePass.name();
            } else if (StringUtils.equals(status, TestCaseReviewCommentStatus.UnPass.name())) {
                status = TestCaseReviewCommentStatus.ForceUnPass.name();
            }
            TestCaseReviewTestCaseEditRequest testCaseReviewTestCase = new TestCaseReviewTestCaseEditRequest();
            testCaseReviewTestCase.setStatus(status);
            testCaseReviewTestCase.setReviewer(SessionUtils.getUser().getId());
            testCaseReviewTestCase.setUpdateTime(System.currentTimeMillis());
            testCaseReviewTestCase.setComment(request.getDescription());
            testCaseReviewTestCase.setCaseId(i);
            testCaseReviewTestCase.setReviewId(request.getReviewId());
            testCaseCommentService.saveReviewComment(testCaseReviewTestCase);
        });
    }

    public void editTestCaseBatchReviewer(TestReviewCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestReviewCaseMapper.selectTestCaseIds((QueryCaseReviewRequest) query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (StringUtils.isNotBlank(request.getReviewer())) {
            if (!request.getAppendTag()) {
                TestCaseReviewTestCaseUsersExample example = new TestCaseReviewTestCaseUsersExample();
                example.createCriteria().andCaseIdIn(request.getIds());
                testCaseReviewTestCaseUsersMapper.deleteByExample(example);
            }
            for (int i = 0; i < ids.size(); i++) {
                TestCaseReviewTestCaseUsersExample example = new TestCaseReviewTestCaseUsersExample();
                example.createCriteria().andReviewIdEqualTo(request.getReviewId()).andCaseIdEqualTo(ids.get(i)).andUserIdEqualTo(request.getReviewer());
                List<TestCaseReviewTestCaseUsers> testCaseReviewTestCaseUsers = testCaseReviewTestCaseUsersMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(testCaseReviewTestCaseUsers)) {
                    TestCaseReviewTestCaseUsers insertData = new TestCaseReviewTestCaseUsers();
                    insertData.setCaseId(ids.get(i));
                    insertData.setReviewId(request.getReviewId());
                    insertData.setUserId(request.getReviewer());
                    testCaseReviewTestCaseUsersMapper.insert(insertData);
                }
            }


            // 修改评审人后重新计算用例的评审状态
            TestCaseReview testReview = testCaseReviewService.getTestReview(request.getReviewId());
            List<TestCaseReviewTestCase> testCaseReviewTestCases = selectForReviewChange(request.getReviewId());
            for (TestCaseReviewTestCase reviewTestCase : testCaseReviewTestCases) {
                // 重新计算评审状态
                reCalcReviewCaseStatus(testReview.getReviewPassRule(), reviewTestCase);
            }
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

    private void checkReviewCase(String reviewId, List<String> caseIds) {
        String currentUserId = SessionUtils.getUser().getId();
        TestCaseReviewTestCaseUsersExample testCaseReviewTestCaseUsersExample = new TestCaseReviewTestCaseUsersExample();
        testCaseReviewTestCaseUsersExample.createCriteria().andReviewIdEqualTo(reviewId).andUserIdEqualTo(currentUserId).andCaseIdIn(caseIds);
        List<TestCaseReviewTestCaseUsers> testCaseReviewTestCaseUsers = testCaseReviewTestCaseUsersMapper.selectByExample(testCaseReviewTestCaseUsersExample);
        List<String> caseLists = testCaseReviewTestCaseUsers.stream().map(TestCaseReviewTestCaseUsers::getCaseId).collect(Collectors.toList());
        if (!ListUtil.equalsList(caseIds, caseLists)) {
            MSException.throwException("非此用例的评审人员！");
        }
    }

    public void editTestCaseForMinder(String reviewId, List<TestCaseReviewTestCase> testCaseReviewTestCases) {
        List<String> caseIds = testCaseReviewTestCases.stream().map(TestCaseReviewTestCase::getCaseId).collect(Collectors.toList());
        checkReviewCase(reviewId, caseIds);
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
     *
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

    private List<String> getReviewUserIds(String reviewId, String caseId) {
        TestCaseReviewTestCaseUsersExample testCaseReviewTestCaseUsersExample = new TestCaseReviewTestCaseUsersExample();
        testCaseReviewTestCaseUsersExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo(caseId);
        List<TestCaseReviewTestCaseUsers> testCaseReviewUsers = testCaseReviewTestCaseUsersMapper.selectByExample(testCaseReviewTestCaseUsersExample);
        return testCaseReviewUsers.stream().map(TestCaseReviewTestCaseUsers::getUserId).collect(Collectors.toList());
    }

    public List<TestCaseReviewTestCase> getCaseStatusByReviewIds(List<String> reviewIds) {
        if (CollectionUtils.isEmpty(reviewIds)) {
            return new ArrayList<>(0);
        }
        return extTestCaseReviewTestCaseMapper.getCaseStatusByReviewIds(reviewIds);
    }

    public void handlePassRuleChange(String originPassRule, TestCaseReview review) {
        List<TestCaseReviewTestCase> reviewTestCases = selectForReviewChange(review.getId());
        for (TestCaseReviewTestCase reviewTestCase : reviewTestCases) {
            // 如果是已经评审过的用例，则重新计算
            updateReviewCaseStatusForRuleChange(originPassRule, reviewTestCase, review.getReviewPassRule());
        }
    }

    public List<TestCaseReviewTestCase> selectForReviewChange(String reviewId) {
        return extTestCaseReviewTestCaseMapper.selectForReviewChange(reviewId);
    }

    public void updateReviewCaseStatusForRuleChange(String originPassRule, TestCaseReviewTestCase reviewTestCase, String reviewPassRule) {
        List<TestCaseCommentDTO> comments =
                testCaseCommentService.getStatusCaseComments(reviewTestCase.getCaseId(), TestCaseCommentType.REVIEW.name(), reviewTestCase.getReviewId());

        comments = filterAgainComments(comments);

        if (CollectionUtils.isEmpty(comments)) {
            return;
        }

        Consumer<String> handleStatusChangeFunc = (originStatus) -> {
            // 如果状态发生变化，则添加一条 RuleChange 用于记录，变化前的评审标准
            TestCaseReviewTestCaseEditRequest addCommentRequest = new TestCaseReviewTestCaseEditRequest();
            BeanUtils.copyBean(addCommentRequest, reviewTestCase);
            addCommentRequest.setStatus(TestCaseReviewCommentStatus.RuleChange.name());
            addCommentRequest.setComment(originPassRule);
            testCaseCommentService.saveReviewCommentWithoutNotification(addCommentRequest);
        };

        updateReviewCaseStatus(reviewTestCase, reviewPassRule, comments, handleStatusChangeFunc);
    }

    /**
     * 重新计算用例的评审状态
     *
     * @param reviewPassRule
     * @param reviewTestCase
     */
    public void reCalcReviewCaseStatus(String reviewPassRule, TestCaseReviewTestCase reviewTestCase) {
        List<TestCaseCommentDTO> comments =
                testCaseCommentService.getStatusCaseComments(reviewTestCase.getCaseId(), TestCaseCommentType.REVIEW.name(), reviewTestCase.getReviewId());

        comments = filterAgainComments(comments);

        if (CollectionUtils.isEmpty(comments)) {
            return;
        }

        updateReviewCaseStatus(reviewTestCase, reviewPassRule, comments, null);
    }

    /**
     * 将已经评审过的用例改成重新提审状态
     *
     * @param caseId
     */
    public void reReviewByCaseId(String caseId) {
        List<TestCaseReviewTestCase> reviewTestCases = extTestCaseReviewTestCaseMapper.selectForReReview(caseId);
        for (TestCaseReviewTestCase reviewTestCase : reviewTestCases) {
            List<TestCaseCommentDTO> comments =
                    testCaseCommentService.getStatusCaseComments(reviewTestCase.getCaseId(), TestCaseCommentType.REVIEW.name(), reviewTestCase.getReviewId());

            comments = filterAgainComments(comments);

            if (CollectionUtils.isNotEmpty(comments)) {
                updateTestReviewTestCaseStatus(reviewTestCase, TestCaseReviewCommentStatus.Again.name());
                // 添加一条重新提审的评论
                TestCaseReviewTestCaseEditRequest addCommentRequest = new TestCaseReviewTestCaseEditRequest();
                BeanUtils.copyBean(addCommentRequest, reviewTestCase);
                addCommentRequest.setStatus(TestCaseReviewCommentStatus.Again.name());
                addCommentRequest.setComment(StringUtils.EMPTY);
                testCaseCommentService.saveReviewCommentWithoutNotification(addCommentRequest);
            }
        }
    }

    public List<TestCaseReviewTestCase> selectForReviewerChange(String reviewId) {
        return extTestCaseReviewTestCaseMapper.selectForReviewerChange(reviewId);
    }

    /**
     * 检查执行结果，自动更新计划状态
     * @param caseId
     */
    public void checkStatus(String caseId) {
        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(caseId);
        if (testCaseReview.getEndTime() != null && testCaseReview.getEndTime() < System.currentTimeMillis()) {
            TestCaseReviewExample example = new TestCaseReviewExample();
            example.createCriteria().andIdEqualTo(caseId);
            TestCaseReview review = new TestCaseReview();
            review.setStatus(TestPlanStatus.Finished.name());
            testCaseReviewMapper.updateByExampleSelective(review,example);
        }
    }
}
