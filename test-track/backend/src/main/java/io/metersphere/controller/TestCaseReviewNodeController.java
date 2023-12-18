package io.metersphere.controller;

import io.metersphere.base.domain.TestCaseReviewNode;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.TestCaseReviewNodeDTO;
import io.metersphere.request.testreview.DragReviewNodeRequest;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.TestCaseReviewNodeService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author song-cc-rock
 */
@RestController
@RequestMapping("/case/review/node")
public class TestCaseReviewNodeController {

    @Resource
    private TestCaseReviewNodeService testCaseReviewNodeService;

    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;

    @PostMapping("/list/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ})
    public List<TestCaseReviewNodeDTO> getNodeByCondition(@PathVariable String projectId, @RequestBody(required = false) QueryCaseReviewRequest request) {
        // 高级搜索所属模块搜索时, 切换项目时需替换projectId为参数中切换项目
        if (request != null && request.getProjectId() != null) {
            projectId = request.getProjectId();
        }
        baseCheckPermissionService.checkProjectOwner(projectId);
        return testCaseReviewNodeService.getNodeTreeByProjectId(projectId,
                Optional.ofNullable(request).orElse(new QueryCaseReviewRequest()));
    }

    @PostMapping("/add")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ_EDIT})
    public String addNode(@RequestBody TestCaseReviewNode node) {
        return testCaseReviewNodeService.addNode(node);
    }

    @PostMapping("/edit")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ_EDIT})
    public int editNode(@RequestBody TestCaseReviewNode node) {
        return testCaseReviewNodeService.editNode(node);
    }

    @PostMapping("/delete")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ_DELETE})
    public int deleteNode(@RequestBody List<String> nodeIds) {
        return testCaseReviewNodeService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ_EDIT})
    public void dragNode(@RequestBody DragReviewNodeRequest node) {
        testCaseReviewNodeService.dragNode(node);
    }

    @PostMapping("/pos")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_REVIEW_READ_EDIT})
    public void treeSort(@RequestBody List<String> ids) {
        testCaseReviewNodeService.sort(ids);
    }
}
