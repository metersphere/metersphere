package io.metersphere.functional.controller;

import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.request.*;
import io.metersphere.functional.service.FunctionalCaseMinderService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author guoyuqi
 */
@Tag(name = "用例管理-功能用例-脑图")
@RestController
@RequestMapping("/functional/mind/case")
public class FunctionalCaseMinderController {

    @Resource
    private FunctionalCaseMinderService functionalCaseMinderService;

    @PostMapping("/tree")
    @Operation(summary = "用例管理-功能用例-脑图-获取空白节点和模块的组合树")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@Validated @RequestBody FunctionalCaseMindTreeRequest request) {
        return functionalCaseMinderService.getTree(request);
    }

    @PostMapping("/list")
    @Operation(summary = "用例管理-功能用例-脑图用例跟根据模块ID查询列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FunctionalMinderTreeDTO>> getFunctionalCaseMinderTree(@Validated @RequestBody FunctionalCaseMindRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), 100 );
        return PageUtils.setPageInfo(page, functionalCaseMinderService.getMindFunctionalCase(request, false));

    }

    @PostMapping("/edit")
    @Operation(summary = "脑图保存")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    public void editFunctionalCaseBatch(@Validated @RequestBody FunctionalCaseMinderEditRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseMinderService.editFunctionalCaseBatch(request, userId);
    }

    @PostMapping("/review/list")
    @Operation(summary = "用例管理-功能用例-脑图用例跟根据模块ID查询列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public Pager<List<FunctionalMinderTreeDTO>> getReviewMindFunctionalCase(@Validated @RequestBody FunctionalCaseReviewMindRequest request) {
        String userId = StringUtils.EMPTY;
        if (request.isViewFlag()) {
            userId = SessionUtils.getUserId();
        }
        String viewStatusUserId = StringUtils.EMPTY;
        if (request.isViewStatusFlag()) {
            viewStatusUserId = SessionUtils.getUserId();
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), 100 );
        return PageUtils.setPageInfo(page, functionalCaseMinderService.getReviewMindFunctionalCase(request, false, userId, viewStatusUserId));
    }

    @PostMapping("/plan/list")
    @Operation(summary = "测试计划-功能用例-脑图用例跟根据模块ID查询列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public  Pager<List<FunctionalMinderTreeDTO>> getPlanFunctionalCaseMinderTree(@Validated @RequestBody FunctionalCasePlanMindRequest request) { Page<Object> page = PageHelper.startPage(request.getCurrent(), 100 );
        return PageUtils.setPageInfo(page, functionalCaseMinderService.getPlanMindFunctionalCase(request, false));
    }

    @PostMapping("/collection/list")
    @Operation(summary = "测试集-功能用例-脑图用例跟根据测试集ID查询列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public  Pager<List<FunctionalMinderTreeDTO>> getCollectionFunctionalCaseMinderTree(@Validated @RequestBody FunctionalCaseCollectionMindRequest request) { Page<Object> page = PageHelper.startPage(request.getCurrent(), 100 );
        return PageUtils.setPageInfo(page, functionalCaseMinderService.getCollectionMindFunctionalCase(request, false));
    }

}
