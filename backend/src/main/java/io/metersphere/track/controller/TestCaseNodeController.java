package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.request.testcase.DragNodeRequest;
import io.metersphere.track.request.testcase.QueryNodeRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.service.TestCaseNodeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RequestMapping("/case/node")
@RestController
public class TestCaseNodeController {

    @Resource
    TestCaseNodeService testCaseNodeService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("/list/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getNodeByProjectId(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/minder/extraNode/count")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public Map<String, Integer> getMinderTreeExtraNodeCount(@RequestBody List<String> nodeIds) {
        return testCaseNodeService.getMinderTreeExtraNodeCount(nodeIds);
    }

    @GetMapping("/trashCount/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public long trashCount(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return testCaseNodeService.trashCount(projectId);
    }

    @GetMapping("/publicCount/{workSpaceId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public long publicCount(@PathVariable String workSpaceId) {
        return testCaseNodeService.publicCount(workSpaceId);
    }

    /*模块列表列表*/
    @PostMapping("/list/all/plan")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getAllNodeByPlanId(@RequestBody QueryNodeRequest request) {
        return testCaseNodeService.getAllNodeByPlanId(request);
    }

    /*模块列表列表*/
    @PostMapping("/list/project")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getAllNodeByProjectId(@RequestBody QueryNodeRequest request) {
        return testCaseNodeService.getAllNodeByProjectId(request);
    }

    @PostMapping("/list/all/review")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getAllNodeByReviewId(@RequestBody QueryNodeRequest request) {
        return testCaseNodeService.getAllNodeByReviewId(request);
    }

    @GetMapping("/list/plan/{planId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        return testCaseNodeService.getNodeByPlanId(planId);
    }

    @GetMapping("/list/plan/{planId}/{runResult}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getNodeByPlanIdAndRunResult(@PathVariable String planId,@PathVariable String runResult) {
        checkPermissionService.checkTestPlanOwner(planId);
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        request.setStatus(runResult);
        return testCaseNodeService.getNodeByQueryRequest(request);
    }

    @GetMapping("/list/review/{reviewId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestCaseNodeDTO> getNodeByReviewId(@PathVariable String reviewId) {
        checkPermissionService.checkTestReviewOwner(reviewId);
        return testCaseNodeService.getNodeByReviewId(reviewId);
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
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT})
    public void treeSort(@RequestBody List<String> ids) {
        testCaseNodeService.sort(ids);
    }
}
