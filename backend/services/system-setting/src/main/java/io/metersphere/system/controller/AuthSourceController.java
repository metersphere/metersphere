package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.ldap.service.LdapService;
import io.metersphere.system.ldap.vo.LdapLoginRequest;
import io.metersphere.system.ldap.vo.LdapRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.dto.AuthSourceDTO;
import io.metersphere.system.request.AuthSourceRequest;
import io.metersphere.system.request.AuthSourceStatusRequest;
import io.metersphere.system.service.AuthSourceLogService;
import io.metersphere.system.service.AuthSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统设置-系统-系统参数-认证设置")
@RestController
@RequestMapping("/system/authsource")
@Data
public class AuthSourceController {
    @Resource
    private AuthSourceService authSourceService;

    @Resource
    private LdapService ldapService;

    @PostMapping("/list")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-列表查询")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ)
    public Pager<List<AuthSource>> list(@Validated @RequestBody BasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, authSourceService.list());
    }

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-新增")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#authSource)", msClass = AuthSourceLogService.class)
    public AuthSource add(@Validated @RequestBody AuthSourceRequest authSource) {
        return authSourceService.addAuthSource(authSource);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-更新")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#authSource)", msClass = AuthSourceLogService.class)
    public AuthSourceRequest update(@Validated @RequestBody AuthSourceRequest authSource) {
        return authSourceService.updateAuthSource(authSource);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-详细信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ)
    public AuthSourceDTO get(@PathVariable(value = "id") String id) {
        return authSourceService.getAuthSource(id);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-删除")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = AuthSourceLogService.class)
    public void delete(@PathVariable(value = "id") String id) {
        authSourceService.deleteAuthSource(id);
    }


    @PostMapping("/update/status")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-更新状态")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request.getId())", msClass = AuthSourceLogService.class)
    public AuthSource updateStatus(@Validated @RequestBody AuthSourceStatusRequest request) {
        return authSourceService.updateStatus(request.getId(), request.getEnable());
    }


    @PostMapping("/ldap/test-connect")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-ldap测试连接")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE)
    public void ldapTestConnect(@Validated @RequestBody LdapRequest request) {
        ldapService.testConnect(request);
    }

    @PostMapping("/ldap/test-login")
    @Operation(summary = "系统设置-系统-系统参数-认证设置-ldap测试登录")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE)
    public void testLogin(@RequestBody LdapLoginRequest request) {
        ldapService.testLogin(request);
    }
}
