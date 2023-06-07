package io.metersphere.system.controller;


import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.system.domain.User;
import io.metersphere.system.service.UserService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/get/{userId}")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public User getUser(@PathVariable String userId) {
        return userService.getById(userId);
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_ADD)
    public UserDTO addUser(@Validated({Created.class}) @RequestBody UserDTO user) {
        return userService.add(user);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_UPDATE)
    public UserDTO updateUser(@Validated({Updated.class}) @RequestBody UserDTO user) {
        return userService.update(user);
    }

    @GetMapping("/delete/{userId}")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_DELETE)
    public UserDTO deleteUser(@PathVariable String userId) {
        return userService.delete(userId);
    }

    @PostMapping("/batch-add3")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ_ADD)
    public boolean batchSaveUser3(@Validated({Created.class}) @RequestBody List<User> user) {
        return userService.batchSave3(user);
    }

    @GetMapping("/count")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_READ)
    public long batchSaveUser() {
        return userService.count();
    }

}
