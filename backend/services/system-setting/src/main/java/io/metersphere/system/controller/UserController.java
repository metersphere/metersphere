package io.metersphere.system.controller;


import io.metersphere.domain.User;
import io.metersphere.sdk.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
