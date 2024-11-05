package io.metersphere.bug.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.request.BugBatchRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.service.BugLogService;
import io.metersphere.bug.service.BugService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "缺陷管理-回收站")
@RestController
@RequestMapping("/bug/trash")
public class BugTrashController {

    @Resource
    private BugService bugService;

    @PostMapping("/page")
    @Operation(summary = "缺陷管理-回收站-获取缺陷列表")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public Pager<List<BugDTO>> page(@Validated @RequestBody BugPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "delete_time desc, num asc");
        request.setUseTrash(true);
        return PageUtils.setPageInfo(page, bugService.list(request));
    }

    @GetMapping("/recover/{id}")
    @Operation(summary = "缺陷管理-回收站-恢复")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    @Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#id)", msClass = BugLogService.class)
    public void recover(@PathVariable String id) {
        bugService.recover(id);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "缺陷管理-回收站-彻底删除")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteTrashLog(#id)", msClass = BugLogService.class)
    public void deleteTrash(@PathVariable String id) {
        bugService.deleteTrash(id);
    }

    @PostMapping("/batch-recover")
    @Operation(summary = "缺陷管理-回收站-批量恢复")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchRecover(@Validated @RequestBody BugBatchRequest request) {
        request.setUseTrash(true);
        bugService.batchRecover(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "缺陷管理-回收站-批量彻底删除")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDeleteTrashLog(#request)", msClass = BugLogService.class)
    public void batchDelete(@Validated @RequestBody BugBatchRequest request) {
        request.setUseTrash(true);
        bugService.batchDeleteTrash(request);
    }
}
