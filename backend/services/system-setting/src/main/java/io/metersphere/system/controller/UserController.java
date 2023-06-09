package io.metersphere.system.controller;


import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.UserMaintainRequest;
import io.metersphere.system.service.UserService;
import io.metersphere.validation.groups.Created;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public UserMaintainRequest addUser(@Validated({Created.class}) @RequestBody UserMaintainRequest userCreateDTO) {
        userCreateDTO.setCreateUserToList(SessionUtils.getSessionId());
        return userService.add(userCreateDTO);
    }
}
