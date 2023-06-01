package io.metersphere.system.controller;


import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.system.domain.User;
import io.metersphere.system.service.UserService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/list-all")
    public List<User> listAll() {
        return userService.list();
    }

    @GetMapping("/get/{userId}")
    public User getUser(@PathVariable String userId) {
        return userService.getById(userId);
    }

    @PostMapping("/add")
    public UserDTO addUser(@Validated({Created.class}) @RequestBody UserDTO user) {
        return userService.add(user);
    }

    @PostMapping("/update")
    public UserDTO updateUser(@Validated({Updated.class}) @RequestBody UserDTO user) {
        return userService.update(user);
    }

    @GetMapping("/delete/{userId}")
    public UserDTO deleteUser(@PathVariable String userId) {
        return userService.delete(userId);
    }

    @PostMapping("/batch-add2")
    public boolean batchSaveUser2(@Validated({Created.class}) @RequestBody List<User> user) {
        return userService.batchSave2(user);
    }

    @PostMapping("/batch-add3")
    public boolean batchSaveUser3(@Validated({Created.class}) @RequestBody List<User> user) {
        return userService.batchSave3(user);
    }

    @GetMapping("/count")
    public long batchSaveUser() {
        return userService.count();
    }

}
