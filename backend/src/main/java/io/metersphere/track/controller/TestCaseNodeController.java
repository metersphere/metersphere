package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.request.testcase.DragNodeRequest;
import io.metersphere.track.request.testcase.QueryNodeRequest;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import io.metersphere.track.service.TestCaseNodeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/case/node")
@RestController
public class TestCaseNodeController {

    @Resource
    TestCaseNodeService testCaseNodeService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("/list/{projectId}")
    public List<TestCaseNodeDTO> getNodeByProjectId(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/list/{projectId}")
    public List<TestCaseNodeDTO> getNodeByCondition(@PathVariable String projectId, @RequestBody(required = false) QueryTestCaseRequest request) {
        checkPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.getNodeTreeByProjectId(projectId, Optional.ofNullable(request).orElse(new QueryTestCaseRequest()));
    }

    @PostMapping("/minder/extraNode/count")
    public Map<String, Integer> getMinderTreeExtraNodeCount(@RequestBody List<String> nodeIds) {
        return testCaseNodeService.getMinderTreeExtraNodeCount(nodeIds);
    }

    @GetMapping("/trashCount/{projectId}")
    public long trashCount(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.trashCount(projectId);
    }

    @GetMapping("/publicCount/{workSpaceId}")
    public long publicCount(@PathVariable String workSpaceId) {
        return testCaseNodeService.publicCount(workSpaceId);
    }

    /*模块列表列表*/
    @PostMapping("/list/project")
    public List<TestCaseNodeDTO> getAllNodeByProjectId(@RequestBody QueryNodeRequest request) {
        return testCaseNodeService.getAllNodeByProjectId(request);
    }

    @GetMapping("/list/plan/{planId}")
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        return testCaseNodeService.getNodeByPlanId(planId);
    }

    @PostMapping("/list/plan/{planId}")
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId, @RequestBody(required = false) QueryTestPlanCaseRequest request) {
        checkPermissionService.checkTestPlanOwner(planId);
        return testCaseNodeService.getNodeByPlanId(planId, Optional.ofNullable(request).orElse(new QueryTestPlanCaseRequest()));
    }

    @PostMapping("/list/public/{workspaceId}")
    public List<TestCaseNodeDTO> getPublicCaseNode(@PathVariable String workspaceId, @RequestBody QueryTestCaseRequest request) {
        return testCaseNodeService.getPublicCaseNode(workspaceId, request);
    }

    @PostMapping("/list/trash/{projectId}")
    public List<TestCaseNodeDTO> getTrashCaseNode(@PathVariable String projectId, @RequestBody QueryTestCaseRequest request) {
        return testCaseNodeService.getTrashCaseNode(projectId, request);
    }

    @PostMapping("/list/plan/relate")
    public List<TestCaseNodeDTO> getRelatePlanNodes(@RequestBody QueryTestCaseRequest request) {
        checkPermissionService.checkTestPlanOwner(request.getPlanId());
        return testCaseNodeService.getRelatePlanNodes(request);
    }

    @PostMapping("/list/review/relate")
    public List<TestCaseNodeDTO> getRelateReviewNodes(@RequestBody QueryTestCaseRequest request) {
        checkPermissionService.checkTestReviewOwner(request.getReviewId());
        return testCaseNodeService.getRelateReviewNodes(request);
    }

    @GetMapping("/list/plan/{planId}/{runResult}")
    public List<TestCaseNodeDTO> getNodeByPlanIdAndRunResult(@PathVariable String planId, @PathVariable String runResult) {
        checkPermissionService.checkTestPlanOwner(planId);
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        request.setStatus(runResult);
        return testCaseNodeService.getNodeByQueryRequest(request);
    }

    @GetMapping("/list/review/{reviewId}")
    public List<TestCaseNodeDTO> getNodeByReviewId(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        return testCaseNodeService.getNodeByReviewId(reviewId);
    }

    @PostMapping("/list/review/{reviewId}")
    public List<TestCaseNodeDTO> getNodeByReviewId(@PathVariable String reviewId, @RequestBody(required = false) QueryCaseReviewRequest request) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        return testCaseNodeService.getNodeByReviewId(reviewId, Optional.ofNullable(request).orElse(new QueryCaseReviewRequest()));
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = TestCaseNodeService.class)
    public String addNode(@RequestBody TestCaseNode node) {
        return testCaseNodeService.addNode(node);
    }

    @PostMapping("/edit")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = TestCaseNodeService.class)
    public int editNode(@RequestBody DragNodeRequest node) {
        return testCaseNodeService.editNode(node);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = TestCaseNodeService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return testCaseNodeService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = TestCaseNodeService.class)
    public void dragNode(@RequestBody DragNodeRequest node) {
        testCaseNodeService.dragNode(node);
    }

    @PostMapping("/pos")
    public void treeSort(@RequestBody List<String> ids) {
        testCaseNodeService.sort(ids);
    }
}
