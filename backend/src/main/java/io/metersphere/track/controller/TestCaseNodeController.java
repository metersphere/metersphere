package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.request.testcase.DragNodeRequest;
import io.metersphere.track.request.testcase.QueryNodeRequest;
import io.metersphere.track.service.TestCaseNodeService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/case/node")
@RestController
@RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
public class TestCaseNodeController {

    @Resource
    TestCaseNodeService testCaseNodeService;

    @GetMapping("/list/{projectId}")
    public List<TestCaseNodeDTO> getNodeByProjectId(@PathVariable String projectId) {
        return testCaseNodeService.getNodeTreeByProjectId(projectId);
    }

    /*模块列表列表*/
    @PostMapping("/list/all/plan")
    public List<TestCaseNodeDTO> getAllNodeByPlanId(@RequestBody QueryNodeRequest request) {
        return testCaseNodeService.getAllNodeByPlanId(request);
    }

    @GetMapping("/list/plan/{planId}")
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId) {
        return testCaseNodeService.getNodeByPlanId(planId);
    }

    @PostMapping("/add")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public String addNode(@RequestBody TestCaseNode node) {
        return testCaseNodeService.addNode(node);
    }

    @PostMapping("/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int editNode(@RequestBody DragNodeRequest node) {
        return testCaseNodeService.editNode(node);
    }

    @PostMapping("/delete")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return testCaseNodeService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void dragNode(@RequestBody DragNodeRequest node) {
        testCaseNodeService.dragNode(node);
    }
}
