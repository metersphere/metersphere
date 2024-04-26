package io.metersphere.system.controller;


import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.domain.UserKey;
import io.metersphere.system.dto.UserKeyDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.ApiKeyHandler;
import io.metersphere.system.service.UserKeyLogService;
import io.metersphere.system.service.UserKeyService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/api/key")
@Tag(name = "系统设置-个人中心-我的设置-Api Keys")
public class UserApiKeysController {

    @Resource
    private UserKeyService userKeyService;

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_API_KEY_READ)
    @Operation(summary = "系统设置-个人中心-我的设置-Api Keys-获取Api Keys列表")
    public List<UserKey> getUserKeysInfo() {
        return userKeyService.getUserKeysInfo(SessionUtils.getUserId());
    }

    @GetMapping("/validate")
    @Operation(summary = "系统设置-个人中心-我的设置-Api Keys-验证Api Keys")
    public String validate(ServletRequest request) {
        return ApiKeyHandler.getUser(WebUtils.toHttp(request));
    }

    @GetMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_API_KEY_ADD)
    @Operation(summary = "系统设置-个人中心-我的设置-Api Keys-生成Api Keys")
    public void add() {
        userKeyService.add(SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_API_KEY_DELETE)
    @Operation(summary = "系统设置-个人中心-我的设置-Api Keys-删除Api Keys")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = UserKeyLogService.class)
    public void delete(@PathVariable String id) {
        userKeyService.checkUserKeyOwner(id, SessionUtils.getUserId());
        userKeyService.deleteUserKey(id);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-个人中心-我的设置-Api Keys-修改Api Keys")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_API_KEY_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = UserKeyLogService.class)
    public void update(@Validated @RequestBody UserKeyDTO request) {
        userKeyService.checkUserKeyOwner(request.getId(), SessionUtils.getUserId());
        userKeyService.updateUserKey(request);
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-个人中心-我的设置-Api Keys-开启Api Keys")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_API_KEY_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.enableLog(#id)", msClass = UserKeyLogService.class)
    public void enable(@PathVariable String id) {
        userKeyService.checkUserKeyOwner(id, SessionUtils.getUserId());
        userKeyService.enableUserKey(id);
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-个人中心-我的设置-Api Keys-关闭Api Keys")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_API_KEY_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.disableLog(#id)", msClass = UserKeyLogService.class)
    public void disabledUserKey(@PathVariable String id) {
        userKeyService.checkUserKeyOwner(id, SessionUtils.getUserId());
        userKeyService.disableUserKey(id);
    }
}
