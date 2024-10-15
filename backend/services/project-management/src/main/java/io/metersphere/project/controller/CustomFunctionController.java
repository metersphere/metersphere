package io.metersphere.project.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.dto.customfunction.CustomFuncColumnsOptionDTO;
import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.customfunction.request.CustomFunctionPageRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionUpdateRequest;
import io.metersphere.project.service.CustomFunctionLogService;
import io.metersphere.project.service.CustomFunctionService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
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

/**
 * @author: LAN
 * @date: 2024/1/9 19:19
 * @version: 1.0
 */
@RestController
@RequestMapping(value = "/project/custom/func")
@Tag(name = "项目管理-公共脚本")
public class CustomFunctionController {

    @Resource
    private CustomFunctionService customFunctionService;
    @PostMapping("/page")
    @Operation(summary = "项目管理-公共脚本-列表")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<CustomFunctionDTO>> query(@Validated @RequestBody CustomFunctionPageRequest request) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, customFunctionService.getPage(request));
    }

    @GetMapping("/columns-option/{projectId}")
    @Operation(summary = "项目管理-公共脚本-请求头筛选相关选项")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public CustomFuncColumnsOptionDTO getColumnsOption(@PathVariable String projectId) {
        return customFunctionService.getColumnsOption(projectId);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "项目管理-公共脚本-脚本详情")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_READ)
    @CheckOwner(resourceId = "#id", resourceType = "custom_function")
    public CustomFunctionDTO get(@PathVariable String id) {
        return customFunctionService.getWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "项目管理-公共脚本-脚本添加")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = CustomFunctionLogService.class)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public CustomFunction add(@Validated @RequestBody CustomFunctionRequest request) {
        return customFunctionService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-公共脚本-脚本更新")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = CustomFunctionLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "custom_function")
    public void update(@Validated @RequestBody CustomFunctionUpdateRequest request) {
        customFunctionService.update(request, SessionUtils.getUserId());
    }

    @PostMapping("/status")
    @Operation(summary = "项目管理-公共脚本-脚本更新状态")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusLog(#request)", msClass = CustomFunctionLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "custom_function")
    public void updateStatus(@Validated @RequestBody CustomFunctionUpdateRequest request) {
        customFunctionService.updateStatus(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "项目管理-脚本删除")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.delLog(#id)", msClass = CustomFunctionLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "custom_function")
    public void delete(@PathVariable String id) {
        customFunctionService.delete(id);
    }

    @PostMapping("/history/page")
    @Operation(summary = "项目管理-公共脚本-变更历史-列表")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<OperationHistoryDTO>> page(@Validated @RequestBody OperationHistoryRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "id desc");
        return PageUtils.setPageInfo(page, customFunctionService.list(request));
    }
}
