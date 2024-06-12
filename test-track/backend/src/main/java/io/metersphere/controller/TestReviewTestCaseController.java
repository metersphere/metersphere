package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseComment;
import io.metersphere.base.domain.TestCaseReviewTestCase;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.TestReviewCaseDTO;
import io.metersphere.dto.TestReviewTestCaseEditResult;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.request.testplancase.TestReviewCaseBatchRequest;
import io.metersphere.request.testreview.DeleteRelevanceRequest;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
import io.metersphere.request.testreview.TestCaseReviewTestCaseEditRequest;
import io.metersphere.security.CheckOwner;
import io.metersphere.service.TestReviewTestCaseService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/test/review/case")
@RestController
public class TestReviewTestCaseController {

    @Resource
    TestReviewTestCaseService testReviewTestCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "test_case_review")
    public Pager<List<TestReviewCaseDTO>> getTestReviewCases(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCaseReviewRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testReviewTestCaseService.list(request));
    }

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request)", msClass = TestReviewTestCaseService.class)
    public int deleteTestCase(@RequestBody DeleteRelevanceRequest request) {
        return testReviewTestCaseService.deleteTestCase(request);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request)", msClass = TestReviewTestCaseService.class)
    public void deleteTestCaseBatch(@RequestBody TestReviewCaseBatchRequest request) {
        testReviewTestCaseService.deleteTestCaseBatch(request);
    }

    @PostMapping("/batch/edit/status")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_REVIEW)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = TestReviewTestCaseService.class)
    public void editTestCaseBatch(@RequestBody TestReviewCaseBatchRequest request) {
        testReviewTestCaseService.editTestCaseBatchStatus(request);
    }

    @PostMapping("/batch/edit/reviewer")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_REVIEW)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = TestReviewTestCaseService.class)
    public void editTestCaseReviewerBatch(@RequestBody TestReviewCaseBatchRequest request) {
        testReviewTestCaseService.editTestCaseBatchReviewer(request);
    }

    @PostMapping("/minder/edit/{reviewId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#testCases)", msClass = TestReviewTestCaseService.class)
    @CheckOwner(resourceId = "#reviewId", resourceType = "test_case_review")
    public void editTestCaseForMinder(@PathVariable("reviewId") String reviewId, @RequestBody List<TestCaseReviewTestCase> testCases) {
        testReviewTestCaseService.editTestCaseForMinder(reviewId, testCases);
    }

    @PostMapping("/list/minder")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "test_case_review")
    public List<TestReviewCaseDTO> listForMinder(@RequestBody QueryCaseReviewRequest request) {
        return testReviewTestCaseService.listForMinder(request);
    }

    @PostMapping("/list/minder/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "test_case_review")
    public Pager<List<TestReviewCaseDTO>> listForMinder(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCaseReviewRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testReviewTestCaseService.listForMinder(request));
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_REVIEW)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.REVIEW, content = "#msClass.getLogDetails(#testCaseReviewTestCase)", msClass = TestReviewTestCaseService.class)
    @CheckOwner(resourceId = "#testCaseReviewTestCase.getReviewId()", resourceType = "test_case_review")
    public TestReviewTestCaseEditResult editTestCase(@RequestBody TestCaseReviewTestCaseEditRequest testCaseReviewTestCase) {
        return testReviewTestCaseService.editTestCase(testCaseReviewTestCase);
    }

    @GetMapping("/get/{reviewId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    public TestReviewCaseDTO get(@PathVariable String reviewId) {
        return testReviewTestCaseService.get(reviewId, SessionUtils.getUserId());
    }

    @GetMapping("/reviewer/status/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    public List<TestCaseComment> getReviewerStatusComment(@PathVariable String id) {
        return testReviewTestCaseService.getReviewerStatusComment(id);
    }

    @PostMapping("/edit/order")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testReviewTestCaseService.updateOrder(request);
    }


}
