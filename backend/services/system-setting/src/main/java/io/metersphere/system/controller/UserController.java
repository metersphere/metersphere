package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.response.UserInfo;
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
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/get/{email}")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public UserDTO getUser(@PathVariable String email) {
        return userService.getByEmail(email);
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_ADD)
    public UserBatchCreateDTO addUser(@Validated({Created.class}) @RequestBody UserBatchCreateDTO userCreateDTO) {
        userCreateDTO.setCreateUserToList(SessionUtils.getSessionId());
        return userService.add(userCreateDTO);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_UPDATE)
    public UserBatchCreateDTO updateUser(@Validated({Updated.class}) @RequestBody UserBatchCreateDTO userCreateDTO) {
        userCreateDTO.setCreateUserToList(SessionUtils.getSessionId());
        return userService.add(userCreateDTO);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_ADD)
    public Pager<List<UserInfo>> list(@Validated @RequestBody BasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, userService.list(request));
    }
}
