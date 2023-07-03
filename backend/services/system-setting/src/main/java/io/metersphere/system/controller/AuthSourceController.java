package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.service.AuthSourceService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/authsource")
public class AuthSourceController {
    @Resource
    private AuthSourceService authSourceService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public Pager<List<AuthSource>> list(@PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, authSourceService.list());
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_CREAT)
    @Log(type = OperationLogType.ADD, module = OperationLogModule.SYSTEM_PARAMETER_SETTING,
            details = "认证设置")
    public void add(@Validated @RequestBody AuthSource authSource) {
        authSourceService.addAuthSource(authSource);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PARAMETER_SETTING,
            details = "认证设置", sourceId = "#authSource.id")
    public void update(@Validated @RequestBody AuthSource authSource) {
        authSourceService.updateAuthSource(authSource);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public AuthSource get(@PathVariable(value = "id") String id) {
        return authSourceService.getAuthSource(id);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_DELETE)
    @Log(type = OperationLogType.DELETE, module = OperationLogModule.SYSTEM_PARAMETER_SETTING,
            details = "认证设置", sourceId = "#id")
    public void delete(@PathVariable(value = "id") String id) {
        authSourceService.deleteAuthSource(id);
    }
}
