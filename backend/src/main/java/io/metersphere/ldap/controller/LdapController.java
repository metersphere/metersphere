package io.metersphere.ldap.controller;

import io.metersphere.base.domain.User;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.ldap.LdapService;
import io.metersphere.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

import static io.metersphere.commons.constants.SessionConstants.ATTR_USER;

@RestController
@RequestMapping("/ldap")
public class LdapController {

    @Resource
    private UserService userService;
    @Resource
    private LdapService ldapService;

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {
        ldapService.authenticate(request);

        SecurityUtils.getSubject().getSession().setAttribute("authenticate", "ldap");

        String username = request.getUsername();
        String password = request.getPassword();

        User u = userService.selectUser(request.getUsername());
        if (u == null) {
            User user = new User();
            user.setId(username);
            user.setName(username);
            // todo user email ?
            user.setEmail(username + "@fit2cloud.com");
            user.setPassword(password);
            userService.createUser(user);
        } else {
            request.setUsername(u.getId());
            request.setPassword(u.getPassword());
        }

        return userService.login(request);
    }





}
