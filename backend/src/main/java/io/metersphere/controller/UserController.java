package io.metersphere.controller;

import io.metersphere.base.domain.User;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public UserDTO insertUser(@RequestBody User user) {
        return userService.insert(user);
    }

    @GetMapping("/list")
    public List<User> getUserList() { return userService.getUserList(); }

    @GetMapping("/delete/{userId}")
    public void deleteUser(@PathVariable(value = "userId") String userId) { userService.deleteUser(userId); }

    @PostMapping("/update")
    public void updateUser(@RequestBody User user) { userService.updateUser(user); }
}
