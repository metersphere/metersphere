package io.metersphere.bug.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.bug.dto.response.BugRelateCaseDTO;
import io.metersphere.bug.service.BugRelateCaseService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "缺陷管理-关联用例")
@RestController
@RequestMapping("/bug/relate/case")
public class BugRelateCaseController {

    @Resource
    private BugRelateCaseService bugRelateCaseService;

    @PostMapping("/page")
    @Operation(description = "缺陷管理-关联用例-列表分页查询")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public Pager<List<BugRelateCaseDTO>> page(@Validated @RequestBody BugRelatedCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, bugRelateCaseService.page(request));
    }

    @GetMapping("/un-relate/{id}")
    @Operation(description = "缺陷管理-关联用例-取消关联用例")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public void unRelate(@PathVariable String id) {
        bugRelateCaseService.unRelate(id);
    }

    @GetMapping("/check-permission/{projectId}/{caseType}")
    @Operation(description = "缺陷管理-关联用例-查看用例权限校验")
    public void checkPermission(@PathVariable String projectId, @PathVariable String caseType) {
        bugRelateCaseService.checkPermission(projectId, SessionUtils.getUserId(), caseType);
    }
}
