package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCaseComment;
import io.metersphere.base.domain.TestCaseReview;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.track.dto.TestCaseReviewDTO;
import io.metersphere.track.dto.TestReviewDTOWithMetric;
import io.metersphere.track.request.testreview.*;
import io.metersphere.track.service.TestCaseCommentService;
import io.metersphere.track.service.TestCaseReviewService;
import io.metersphere.track.service.TestReviewProjectService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/test/case/review")
@RestController

public class TestCaseReviewController {

    @Resource
    TestCaseReviewService testCaseReviewService;
    @Resource
    TestReviewProjectService testReviewProjectService;
    @Resource
    CheckPermissionService checkPermissionService;
    @Resource
    private TestCaseCommentService testCaseCommentService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    public Pager<List<TestCaseReviewDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCaseReviewRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseReviewService.listCaseReview(request));
    }

    @PostMapping("/save")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_CREATE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.CREATE, title = "#reviewRequest.name", content = "#msClass.getLogDetails(#reviewRequest.id)", msClass = TestCaseReviewService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.REVIEW_TASK, event = NoticeConstants.Event.CREATE, mailTemplate = "track/ReviewInitiate", subject = "测试评审通知")
    public TestCaseReview saveCaseReview(@RequestBody SaveTestCaseReviewRequest reviewRequest) {
        reviewRequest.setId(UUID.randomUUID().toString());
        return testCaseReviewService.saveTestCaseReview(reviewRequest);
    }

    @PostMapping("/project")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    public List<Project> getProjectByReviewId(@RequestBody TestCaseReview request) {
        return testCaseReviewService.getProjectByReviewId(request);
    }

    @PostMapping("/reviewer")
    public List<User> getUserByReviewId(@RequestBody TestCaseReview request) {
        return testCaseReviewService.getUserByReviewId(request);
    }

    @PostMapping("/follow")
    public List<User> getFollowByReviewId(@RequestBody TestCaseReview request) {
        return testCaseReviewService.getFollowByReviewId(request);
    }

    @GetMapping("/recent/{count}")
    public List<TestCaseReviewDTO> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        PageHelper.startPage(1, count, true);
        return testCaseReviewService.recent(currentWorkspaceId);
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#testCaseReview.id)", title = "#testCaseReview.name", content = "#msClass.getLogDetails(#testCaseReview.id)", msClass = TestCaseReviewService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.REVIEW_TASK, event = NoticeConstants.Event.UPDATE, mailTemplate = "track/ReviewUpdate", subject = "测试评审通知")
    public TestCaseReview editCaseReview(@RequestBody SaveTestCaseReviewRequest testCaseReview) {
        return testCaseReviewService.editCaseReview(testCaseReview);
    }

    @GetMapping("/delete/{reviewId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#reviewId)", msClass = TestCaseReviewService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.REVIEW_TASK, target = "#targetClass.getTestReview(#reviewId)", targetClass = TestCaseReviewService.class,
            event = NoticeConstants.Event.DELETE, mailTemplate = "track/ReviewDelete", subject = "测试评审通知")
    public void deleteCaseReview(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        testCaseReviewService.deleteCaseReview(reviewId);
    }

    @PostMapping("/list/all")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    public List<TestCaseReview> listAll() {
        return testCaseReviewService.listCaseReviewAll();
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = TestCaseReviewService.class)
    public void testReviewRelevance(@RequestBody ReviewRelevanceRequest request) {
        testCaseReviewService.testReviewRelevance(request);
    }

    @PostMapping("/projects")
    public List<Project> getProjectByReviewId(@RequestBody TestReviewRelevanceRequest request) {
        List<String> projectIds = testReviewProjectService.getProjectIdsByReviewId();
        request.setProjectIds(projectIds);
        return testReviewProjectService.getProject(request);
    }

    @PostMapping("/project/{goPage}/{pageSize}")
    public Pager<List<Project>> getProjectByReviewId(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestReviewRelevanceRequest request) {
        List<String> projectIds = testReviewProjectService.getProjectIdsByReviewId();
        request.setProjectIds(projectIds);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testReviewProjectService.getProject(request));
    }


    @GetMapping("/get/{reviewId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    public TestCaseReview getTestReview(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        return testCaseReviewService.getTestReview(reviewId);
    }

    @PostMapping("/edit/status/{reviewId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_EDIT)
    public void editTestPlanStatus(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        testCaseReviewService.editTestReviewStatus(reviewId);
    }

    @PostMapping("/list/all/relate")
    public List<TestReviewDTOWithMetric> listRelateAll(@RequestBody ReviewRelateRequest request) {
        return testCaseReviewService.listRelateAll(request);
    }

    @PostMapping("/edit/follows")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    public void editTestFollows(@RequestBody SaveTestCaseReviewRequest testCaseReview) {
        testCaseReviewService.editCaseRevieweFollow(testCaseReview);
    }

    @PostMapping("/comment/save")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_COMMENT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseCommentService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.REVIEW_TASK, target = "#targetClass.getTestReview(#request.reviewId)", targetClass = TestCaseReviewService.class,
            event = NoticeConstants.Event.COMMENT, mailTemplate = "track/TestCaseComment", subject = "测试评审通知")
    public TestCaseComment saveComment(@RequestBody SaveCommentRequest request) {
        request.setId(UUID.randomUUID().toString());
        return testCaseCommentService.saveComment(request);
    }

}
