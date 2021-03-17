package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCaseReview;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.track.dto.TestCaseReviewDTO;
import io.metersphere.track.dto.TestReviewDTOWithMetric;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import io.metersphere.track.request.testreview.ReviewRelevanceRequest;
import io.metersphere.track.request.testreview.SaveTestCaseReviewRequest;
import io.metersphere.track.request.testreview.TestReviewRelevanceRequest;
import io.metersphere.track.service.TestCaseReviewService;
import io.metersphere.track.service.TestReviewProjectService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/case/review")
@RestController
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class TestCaseReviewController {

    @Resource
    TestCaseReviewService testCaseReviewService;
    @Resource
    TestReviewProjectService testReviewProjectService;
    @Resource
    CheckPermissionService checkPermissionService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestCaseReviewDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCaseReviewRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseReviewService.listCaseReview(request));
    }

    @PostMapping("/save")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public String saveCaseReview(@RequestBody SaveTestCaseReviewRequest reviewRequest) {
        return testCaseReviewService.saveTestCaseReview(reviewRequest);
    }

    @PostMapping("/project")
    public List<Project> getProjectByReviewId(@RequestBody TestCaseReview request) {
        return testCaseReviewService.getProjectByReviewId(request);
    }

    @PostMapping("/reviewer")
    public List<User> getUserByReviewId(@RequestBody TestCaseReview request) {
        return testCaseReviewService.getUserByReviewId(request);
    }

    @GetMapping("/recent/{count}")
    public List<TestCaseReviewDTO> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        PageHelper.startPage(1, count, true);
        return testCaseReviewService.recent(currentWorkspaceId);
    }

    @PostMapping("/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public String editCaseReview(@RequestBody SaveTestCaseReviewRequest testCaseReview) {
        return testCaseReviewService.editCaseReview(testCaseReview);
    }

    @GetMapping("/delete/{reviewId}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void deleteCaseReview(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        testCaseReviewService.deleteCaseReview(reviewId);
    }

    @PostMapping("/list/all")
    public List<TestCaseReview> listAll() {
        return testCaseReviewService.listCaseReviewAll();
    }

    @PostMapping("/relevance")
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
    public TestCaseReview getTestReview(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        return testCaseReviewService.getTestReview(reviewId);
    }

    @PostMapping("/edit/status/{reviewId}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void editTestPlanStatus(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        testCaseReviewService.editTestReviewStatus(reviewId);
    }

    @GetMapping("/list/all/relate/{type}")
    public List<TestReviewDTOWithMetric> listRelateAll(@PathVariable String type) {
        return testCaseReviewService.listRelateAll(type);
    }
}
