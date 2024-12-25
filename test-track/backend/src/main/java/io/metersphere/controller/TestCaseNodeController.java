package io.metersphere.controller;

import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.dto.TestCaseNodeDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.plan.request.function.QueryTestPlanCaseRequest;
import io.metersphere.request.testcase.DragNodeRequest;
import io.metersphere.request.testcase.QueryNodeRequest;
import io.metersphere.request.testcase.QueryTestCaseRequest;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
import io.metersphere.security.CheckOwner;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.TestCaseNodeService;
import io.metersphere.service.wapper.CheckPermissionService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/case/node")
@RestController
public class TestCaseNodeController {

    @Resource
    TestCaseNodeService testCaseNodeService;
    @Resource
    private CheckPermissionService trackCheckPermissionService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private BaseProjectApplicationService projectApplicationService;

    @GetMapping("/list/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getNodeByProjectId(@PathVariable String projectId) {
        baseCheckPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/list/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getNodeByCondition(@PathVariable String projectId, @RequestBody(required = false) QueryTestCaseRequest request) {
        // 高级搜索所属模块搜索时, 切换项目时需替换projectId为参数中切换项目
        if (request != null && request.getProjectId() != null) {
            projectId = request.getProjectId();
        }
        ProjectApplication projectApplication = projectApplicationService.getProjectApplication(projectId, ProjectApplicationType.CASE_CUSTOM_NUM.name());
        if (projectApplication != null && StringUtils.isNotEmpty(projectApplication.getTypeValue()) && request.getCombine() != null) {
            request.getCombine().put("caseCustomNum", projectApplication.getTypeValue());
        }
        baseCheckPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.getNodeTreeByProjectId(projectId,
                Optional.ofNullable(request).orElse(new QueryTestCaseRequest()));
    }

    @PostMapping("/relationship/list/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getRelationshipNodeByCondition(@PathVariable String projectId, @RequestBody(required = false) QueryTestCaseRequest request) {
        // 高级搜索所属模块搜索时, 切换项目时需替换projectId为参数中切换项目
        if (request != null && request.getProjectId() != null) {
            projectId = request.getProjectId();
        }
        baseCheckPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.getRelationshipNodeByCondition(projectId,
                Optional.ofNullable(request).orElse(new QueryTestCaseRequest()));
    }

    @PostMapping("/count/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public Map<String, Integer> getNodeCountMapByProjectId(@PathVariable String projectId, @RequestBody(required = false) QueryTestCaseRequest request) {
        baseCheckPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.getNodeCountMapByProjectId(projectId,
                Optional.ofNullable(request).orElse(new QueryTestCaseRequest()));
    }

    @PostMapping("/minder/extraNode/count")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public Map<String, Integer> getMinderTreeExtraNodeCount(@RequestBody List<String> nodeIds) {
        return testCaseNodeService.getMinderTreeExtraNodeCount(nodeIds);
    }

    @GetMapping("/trashCount/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public long trashCount(@PathVariable String projectId) {
        baseCheckPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.trashCount(projectId);
    }

    @GetMapping("/publicCount/{workSpaceId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public long publicCount(@PathVariable String workSpaceId) {
        return testCaseNodeService.publicCount(workSpaceId);
    }

    /*模块列表列表*/
    @PostMapping("/list/project")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getAllNodeByProjectId(@RequestBody QueryNodeRequest request) {
        return testCaseNodeService.getAllNodeByProjectId(request);
    }

    @GetMapping("/list/plan/{planId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ})
    @CheckOwner(resourceId = "#planId", resourceType = "test_plan")
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId) {
        trackCheckPermissionService.checkTestPlanOwner(planId);
        return testCaseNodeService.getNodeByPlanId(planId);
    }

    @PostMapping("/list/plan/{planId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ})
    @CheckOwner(resourceId = "#planId", resourceType = "test_plan")
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId, @RequestBody(required = false) QueryTestPlanCaseRequest request) {
        trackCheckPermissionService.checkTestPlanOwner(planId);
        return testCaseNodeService.getNodeByPlanId(planId, Optional.ofNullable(request).orElse(new QueryTestPlanCaseRequest()));
    }

    @PostMapping("/list/public/{workspaceId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getPublicCaseNode(@PathVariable String workspaceId, @RequestBody QueryTestCaseRequest request) {
        return testCaseNodeService.getPublicCaseNode(workspaceId, request);
    }

    @PostMapping("/list/trash/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getTrashCaseNode(@PathVariable String projectId, @RequestBody QueryTestCaseRequest request) {
        return testCaseNodeService.getTrashCaseNode(projectId, request);
    }

    @PostMapping("/list/plan/relate")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ})
    public List<TestCaseNodeDTO> getRelatePlanNodes(@RequestBody QueryTestCaseRequest request) {
        trackCheckPermissionService.checkTestPlanOwner(request.getPlanId());
        return testCaseNodeService.getRelatePlanNodes(request);
    }

    @PostMapping("/list/review/relate")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ})
    public List<TestCaseNodeDTO> getRelateReviewNodes(@RequestBody QueryTestCaseRequest request) {
        trackCheckPermissionService.checkTestReviewOwner(request.getReviewId());
        return testCaseNodeService.getRelateReviewNodes(request);
    }

    @GetMapping("/list/plan/{planId}/{runResult}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ})
    @CheckOwner(resourceId = "#planId", resourceType = "test_plan")
    public List<TestCaseNodeDTO> getNodeByPlanIdAndRunResult(@PathVariable String planId, @PathVariable String runResult) {
        trackCheckPermissionService.checkTestPlanOwner(planId);
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        request.setStatus(runResult);
        return testCaseNodeService.getNodeByQueryRequest(request);
    }

    @GetMapping("/list/review/{reviewId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ})
    @CheckOwner(resourceId = "#reviewId", resourceType = "test_case_review")
    public List<TestCaseNodeDTO> getNodeByReviewId(@PathVariable String reviewId) {
        trackCheckPermissionService.checkTestReviewOwner(reviewId);
        return testCaseNodeService.getNodeByReviewId(reviewId);
    }

    @PostMapping("/list/review/{reviewId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ})
    @CheckOwner(resourceId = "#reviewId", resourceType = "test_case_review")
    public List<TestCaseNodeDTO> getNodeByReviewId(@PathVariable String reviewId, @RequestBody(required = false) QueryCaseReviewRequest request) {
        trackCheckPermissionService.checkTestReviewOwner(reviewId);
        return testCaseNodeService.getNodeByReviewId(reviewId, Optional.ofNullable(request).orElse(new QueryCaseReviewRequest()));
    }

    @PostMapping("/add")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT})
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = TestCaseNodeService.class)
    public String addNode(@RequestBody TestCaseNode node) {
        return testCaseNodeService.addNode(node);
    }

    @PostMapping("/edit")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT})
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = TestCaseNodeService.class)
    public int editNode(@RequestBody DragNodeRequest node) {
        return testCaseNodeService.editNode(node);
    }

    @PostMapping("/delete")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT})
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = TestCaseNodeService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return testCaseNodeService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT})
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = TestCaseNodeService.class)
    public void dragNode(@RequestBody DragNodeRequest node) {
        testCaseNodeService.dragNode(node);
    }

    @PostMapping("/pos")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_CASE)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT})
    public void treeSort(@RequestBody List<String> ids) {
        testCaseNodeService.sort(ids);
    }
}
