package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseReviewTestCase;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.dto.TestReviewCaseDTO;
import io.metersphere.track.request.testplancase.TestReviewCaseBatchRequest;
import io.metersphere.track.request.testreview.DeleteRelevanceRequest;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import io.metersphere.track.service.TestReviewTestCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/review/case")
@RestController
public class TestReviewTestCaseController {

    @Resource
    TestReviewTestCaseService testReviewTestCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestReviewCaseDTO>> getTestPlanCases(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCaseReviewRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testReviewTestCaseService.list(request));
    }

    @PostMapping("/delete")
    @MsAuditLog(module = "track_test_case_review", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request)", msClass = TestReviewTestCaseService.class)
    public int deleteTestCase(@RequestBody DeleteRelevanceRequest request) {
        return testReviewTestCaseService.deleteTestCase(request);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = "track_test_case_review", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request)", msClass = TestReviewTestCaseService.class)
    public void deleteTestCaseBatch(@RequestBody TestReviewCaseBatchRequest request) {
        testReviewTestCaseService.deleteTestCaseBatch(request);
    }

    @PostMapping("/batch/edit/status")
    @MsAuditLog(module = "track_test_case_review", type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = TestReviewTestCaseService.class)
    public void editTestCaseBatch(@RequestBody TestReviewCaseBatchRequest request) {
        testReviewTestCaseService.editTestCaseBatchStatus(request);
    }

    @PostMapping("/minder/edit")
    @MsAuditLog(module = "track_test_case_review", type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#testCases)", msClass = TestReviewTestCaseService.class)
    public void editTestCaseForMinder(@RequestBody List<TestCaseReviewTestCase> testCases) {
        testReviewTestCaseService.editTestCaseForMinder(testCases);
    }

    @PostMapping("/list/minder")
    public List<TestReviewCaseDTO> listForMinder(@RequestBody QueryCaseReviewRequest request) {
        return testReviewTestCaseService.listForMinder(request);
    }

    @PostMapping("/edit")
    @MsAuditLog(module = "track_test_case_review", type = OperLogConstants.REVIEW, content = "#msClass.getLogDetails(#testCaseReviewTestCase)", msClass = TestReviewTestCaseService.class)
    public void editTestCase(@RequestBody TestCaseReviewTestCase testCaseReviewTestCase) {
        testReviewTestCaseService.editTestCase(testCaseReviewTestCase);
    }

    @GetMapping("/get/{reviewId}")
    public TestReviewCaseDTO get(@PathVariable String reviewId) {
        return testReviewTestCaseService.get(reviewId);
    }

    @PostMapping("/list/ids")
    public List<TestReviewCaseDTO> getTestReviewCaseList(@RequestBody QueryCaseReviewRequest request) {
        return testReviewTestCaseService.getTestCaseReviewDTOList(request);
    }

}
