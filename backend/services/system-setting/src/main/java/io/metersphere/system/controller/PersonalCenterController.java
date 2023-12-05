package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.dto.request.user.PersonalUpdatePasswordRequest;
import io.metersphere.system.dto.request.user.PersonalUpdateRequest;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.UserLogService;
import io.metersphere.system.service.UserService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "个人中心")
@RequestMapping("/personal")
public class PersonalCenterController {

    @Resource
    private UserService userService;

    @GetMapping("/get/{id}")
    @Operation(summary = "个人中心-获取信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_READ)
    public UserDTO getInformation(@PathVariable String id) {
        this.checkPermission(id);
        return userService.getUserDTOByKeyword(id);
    }

    @PostMapping("/update-info")
    @Operation(summary = "个人中心-修改信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateAccountLog(#request)", msClass = UserLogService.class)
    public boolean updateUser(@Validated @RequestBody PersonalUpdateRequest request) {
        this.checkPermission(request.getId());
        return userService.updateAccount(request, SessionUtils.getUserId());
    }

    @PostMapping("/update-password")
    @Operation(summary = "个人中心-修改密码")
    @RequiresPermissions(PermissionConstants.SYSTEM_PERSONAL_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updatePasswordLog(#request)", msClass = UserLogService.class)
    public boolean updateUser(@Validated @RequestBody PersonalUpdatePasswordRequest request) {
        this.checkPermission(request.getId());
        return userService.updatePassword(request);
    }

    private void checkPermission(String id) {
        if (!StringUtils.equals(id, SessionUtils.getUserId())) {
            throw new MSException("personal.no.permission");
        }
    }
}
