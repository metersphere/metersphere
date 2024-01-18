package io.metersphere.bug.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.bug.dto.response.BugRelateCaseDTO;
import io.metersphere.bug.service.BugRelateCaseCommonService;
import io.metersphere.bug.service.BugRelateCaseLogService;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.request.AssociateCaseModuleRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
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
import java.util.Map;

@Tag(name = "缺陷管理-关联用例")
@RestController
@RequestMapping("/bug/case")
public class BugRelateCaseController {

    @Resource
    private BugRelateCaseCommonService bugRelateCaseCommonService;
    @Resource
    private BaseAssociateCaseProvider functionalCaseProvider;

    @PostMapping("/un-relate/page")
    @Operation(description = "缺陷管理-关联用例-未关联用例-列表分页")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public Pager<List<TestCaseProviderDTO>> unRelatedPage(@Validated @RequestBody TestCasePageProviderRequest request) {
        // 目前只保留功能用例的Provider接口, 后续其他用例根据RelateCaseType扩展
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalCaseProvider.listUnRelatedTestCaseList(request));
    }

    @PostMapping("/un-relate/module/count")
    @Operation(summary = "缺陷管理-关联用例-未关联用例-模块树数量")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> countTree(@RequestBody @Validated TestCasePageProviderRequest request) {
        return bugRelateCaseCommonService.countTree(request);
    }

    @PostMapping("/un-relate/module/tree")
    @Operation(summary = "缺陷管理-关联用例-未关联用例-模块树")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@RequestBody @Validated AssociateCaseModuleRequest request) {
        return bugRelateCaseCommonService.getRelateCaseTree(request);
    }

    @PostMapping("/relate")
    @Operation(summary = "缺陷管理-关联用例-关联")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.sourceId", resourceType = "functional_case")
    public void relate(@Validated @RequestBody AssociateOtherCaseRequest request) {
        bugRelateCaseCommonService.relateCase(request, false, SessionUtils.getUserId());
    }

    @PostMapping("/page")
    @Operation(description = "缺陷管理-关联用例-列表分页查询")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public Pager<List<BugRelateCaseDTO>> page(@Validated @RequestBody BugRelatedCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, bugRelateCaseCommonService.page(request));
    }

    @GetMapping("/un-relate/{id}")
    @Operation(description = "缺陷管理-关联用例-取消关联用例")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.getRelateLog(#id)", msClass = BugRelateCaseLogService.class)
    public void unRelate(@PathVariable String id) {
        bugRelateCaseCommonService.unRelate(id);
    }

    @GetMapping("/check-permission/{projectId}/{caseType}")
    @Operation(description = "缺陷管理-关联用例-查看用例权限校验")
    public void checkPermission(@PathVariable String projectId, @PathVariable String caseType) {
        bugRelateCaseCommonService.checkPermission(projectId, SessionUtils.getUserId(), caseType);
    }
}
