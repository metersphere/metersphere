package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.dto.AuthSourceDTO;
import io.metersphere.system.request.AuthSourceRequest;
import io.metersphere.system.request.AuthSourceStatusRequest;
import io.metersphere.system.service.AuthSourceLogService;
import io.metersphere.system.service.AuthSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "认证设置")
@RestController
@RequestMapping("/system/authsource")
public class AuthSourceController {
    @Resource
    private AuthSourceService authSourceService;

    @PostMapping("/list")
    @Operation(summary = "认证设置列表查询")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ)
    public Pager<List<AuthSource>> list(@Validated @RequestBody BasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, authSourceService.list());
    }

    @PostMapping("/add")
    @Operation(summary = "新增认证设置")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#authSource)", msClass = AuthSourceLogService.class)
    public AuthSource add(@Validated @RequestBody AuthSourceRequest authSource) {
        return authSourceService.addAuthSource(authSource);
    }

    @PostMapping("/update")
    @Operation(summary = "更新认证设置")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#authSource)", msClass = AuthSourceLogService.class)
    public AuthSourceRequest update(@Validated @RequestBody AuthSourceRequest authSource) {
        return authSourceService.updateAuthSource(authSource);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取认证设置详细信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ)
    public AuthSourceDTO get(@PathVariable(value = "id") String id) {
        return authSourceService.getAuthSource(id);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除认证设置")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = AuthSourceLogService.class)
    public void delete(@PathVariable(value = "id") String id) {
        authSourceService.deleteAuthSource(id);
    }


    @PostMapping("/update/status")
    @Operation(summary = "更新状态")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request.getId())", msClass = AuthSourceLogService.class)
    public AuthSource updateStatus(@Validated @RequestBody AuthSourceStatusRequest request ) {
        return authSourceService.updateStatus(request.getId(), request.getEnable());
    }
}
