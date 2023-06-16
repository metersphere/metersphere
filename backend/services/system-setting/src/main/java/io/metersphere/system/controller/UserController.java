package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.log.annotation.RequestLog;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserEditRequest;
import io.metersphere.system.dto.UserInfo;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.service.GlobalUserRoleService;
import io.metersphere.system.service.UserService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private GlobalUserRoleService globalUserRoleService;

    @GetMapping("/get/{email}")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public UserDTO getUser(@PathVariable String email) {
        return userService.getUserDTOByEmail(email);
    }

    @GetMapping("/get/global/system/role")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_ADD)
    public List<UserRoleOption> getGlobalSystemRole() {
        return globalUserRoleService.getGlobalSystemRoleList();
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_ADD)
    public UserBatchCreateDTO addUser(@Validated({Created.class}) @RequestBody UserBatchCreateDTO userCreateDTO) {
        userCreateDTO.setCreateUserToList(SessionUtils.getUserId());
        return userService.addBatch(userCreateDTO);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_UPDATE)
    @RequestLog(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_USER,
            sourceId = "#request.id", details = "#request.name")
    public UserEditRequest updateUser(@Validated({Updated.class}) @RequestBody UserEditRequest request) {
        request.setUpdateUser(SessionUtils.getUserId());
        return userService.updateUser(request);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public Pager<List<UserInfo>> list(@Validated @RequestBody BasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, userService.list(request));
    }
}
