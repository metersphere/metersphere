package io.metersphere.quota.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Quota;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.quota.dto.QuotaConstants;
import io.metersphere.quota.dto.QuotaResult;
import io.metersphere.quota.service.QuotaManagementService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/quota")

public class QuotaController {

    @Resource
    private QuotaManagementService quotaManagementService;

    @GetMapping("/default/workspace")
    @RequiresPermissions(PermissionConstants.SYSTEM_QUOTA_READ)
    public Quota getWsDefaultQuota() {
        return quotaManagementService.getDefaultQuota(QuotaConstants.DefaultType.workspace);
    }

    @GetMapping("/default/project/{workspaceId}")
    public Quota getProjectDefaultQuota(@PathVariable String workspaceId) {
        return quotaManagementService.getProjectDefaultQuota(workspaceId);
    }

    @PostMapping("/save/default/workspace")
    @RequiresPermissions(PermissionConstants.SYSTEM_QUOTA_READ_EDIT)
    @MsAuditLog(module = OperLogModule.SYSTEM_QUOTA_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#quota.id)", content = "#msClass.getLogDetails(#quota.id)", msClass = QuotaManagementService.class)
    public void saveWsDefaultQuota(@RequestBody Quota quota) {
        quota.setId(QuotaConstants.DefaultType.workspace.name());
        quotaManagementService.saveQuota(quota);
    }

    @PostMapping("/save/default/project")
    @MsAuditLog(module = OperLogModule.SYSTEM_QUOTA_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#quota.id)", content = "#msClass.getLogDetails(#quota.id)", msClass = QuotaManagementService.class)
    public void saveProjectDefaultQuota(@RequestBody Quota quota) {
        if (StringUtils.isBlank(quota.getId())) {
            quota.setId(SessionUtils.getCurrentWorkspaceId());
        }
        quota.setId(QuotaConstants.prefix + quota.getId());
        quotaManagementService.saveQuota(quota);
    }

    @PostMapping("/list/workspace/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SYSTEM_QUOTA_READ)
    public Pager<List<QuotaResult>> listWsQuota(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String, String> param) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, quotaManagementService.listWorkspaceQuota(param.get("name")));
    }

    @PostMapping("/list/project/{goPage}/{pageSize}")
    public Pager<List<QuotaResult>> listProjectQuota(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String, String> param) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, quotaManagementService.listProjectQuota(param.get("workspaceId"), param.get("name")));
    }

    @PostMapping("/save")
    @RequiresPermissions(PermissionConstants.SYSTEM_QUOTA_READ_EDIT)
    @MsAuditLog(module = OperLogModule.SYSTEM_QUOTA_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#quota.id)", content = "#msClass.getLogDetails(#quota.id)", msClass = QuotaManagementService.class)
    public void saveQuota(@RequestBody Quota quota) {
        quotaManagementService.saveQuota(quota);
    }

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.SYSTEM_QUOTA_READ_EDIT)
    @MsAuditLog(module = OperLogModule.SYSTEM_QUOTA_MANAGEMENT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#quota.id)", msClass = QuotaManagementService.class)
    public void delete(@RequestBody Quota quota) {
        quotaManagementService.deleteQuota(quota.getId());
    }

    @GetMapping("/list/modules/{type}/{id}")
    public List<String> getQuotaWsModules(@PathVariable String type ,@PathVariable String id) {
        return quotaManagementService.getQuotaWsModules(type , id);
    }
}
