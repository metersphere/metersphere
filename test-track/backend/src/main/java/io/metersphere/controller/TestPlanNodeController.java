package io.metersphere.controller;

import io.metersphere.base.domain.TestPlanNode;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.TestPlanNodeDTO;
import io.metersphere.plan.request.QueryTestPlanRequest;
import io.metersphere.request.testplan.DragPlanNodeRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.TestPlanNodeService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author song-cc-rock
 */
@RestController
@RequestMapping("/plan/node")
public class TestPlanNodeController {

    @Resource
    private TestPlanNodeService testPlanNodeService;

    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;

    @PostMapping("/list/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ})
    public List<TestPlanNodeDTO> getNodeByCondition(@PathVariable String projectId, @RequestBody(required = false) QueryTestPlanRequest request) {
        // 高级搜索所属模块搜索时, 切换项目时需替换projectId为参数中切换项目
        if (request != null && request.getProjectId() != null) {
            projectId = request.getProjectId();
        }
        baseCheckPermissionService.checkProjectOwner(projectId);
        return testPlanNodeService.getNodeTreeByProjectId(projectId,
                Optional.ofNullable(request).orElse(new QueryTestPlanRequest()));
    }

    @PostMapping("/add")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT})
    public String addNode(@RequestBody TestPlanNode node) {
        return testPlanNodeService.addNode(node);
    }

    @PostMapping("/edit")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT})
    public int editNode(@RequestBody TestPlanNode node) {
        return testPlanNodeService.editNode(node);
    }

    @PostMapping("/delete")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ_DELETE})
    public int deleteNode(@RequestBody List<String> nodeIds) {
        return testPlanNodeService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT})
    public void dragNode(@RequestBody DragPlanNodeRequest node) {
        testPlanNodeService.dragNode(node);
    }

    @PostMapping("/pos")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT})
    public void treeSort(@RequestBody List<String> ids) {
        testPlanNodeService.sort(ids);
    }
}
