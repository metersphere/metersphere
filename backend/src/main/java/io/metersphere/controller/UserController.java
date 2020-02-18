package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.member.AddMemberRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserRoleDTO;
import io.metersphere.service.UserService;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
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
    public List<User> getUserList() {
        return userService.getUserList();
    }

    @GetMapping("/delete/{userId}")
    public void deleteUser(@PathVariable(value = "userId") String userId) {
        userService.deleteUser(userId);
    }

    @PostMapping("/update")
    public void updateUser(@RequestBody User user) {
        userService.updateUser(user);
    }

    @GetMapping("/role/list/{userId}")
    public List<Role> getUserRolesList(@PathVariable(value = "userId") String userId) {
        return userService.getUserRolesList(userId);
    }

    @GetMapping("/rolelist/{userId}")
    public List<UserRoleDTO> convertUserRoleDTO(@PathVariable(value = "userId") String userId) {
        return userService.getUserRoleList(userId);
    }

    @PostMapping("/switch/source/{sourceId}")
    public void switchUserRole(@PathVariable(value = "sourceId") String sourceId) {
        UserDTO user = SessionUtils.getUser();
        userService.switchUserRole(user, sourceId);
    }

    @GetMapping("/info/{userId}")
    public User getUserInfo(@PathVariable(value = "userId") String userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 获取成员用户
     */
    @PostMapping("/member/list/{goPage}/{pageSize}")
    //@RequiresRoles(RoleConstants.TEST_MANAGER)
    public Pager<List<User>> getMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getMemberList(request));
    }

    /**
     * 添加成员
     */
    @PostMapping("/member/add")
    //@RequiresRoles(RoleConstants.TEST_MANAGER)
    public void addMember(@RequestBody AddMemberRequest request) {
        userService.addMember(request);
    }

    /**
     * 删除成员
     */
    @GetMapping("/member/delete/{workspaceId}/{userId}")
    //@RequiresRoles(RoleConstants.TEST_MANAGER)
    public void deleteMember(@PathVariable String workspaceId, @PathVariable String userId) {
        userService.deleteMember(workspaceId, userId);
    }

}
