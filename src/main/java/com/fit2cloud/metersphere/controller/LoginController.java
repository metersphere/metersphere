package com.fit2cloud.metersphere.controller;

import com.fit2cloud.metersphere.controller.request.LoginRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class LoginController {

    @GetMapping(value = "/isLogin")
    public ResultHolder isLogin() {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return ResultHolder.success("");
        }
        return ResultHolder.error("");
    }

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {
        String msg;
        String username = StringUtils.trim(request.getUsername());
        String password = StringUtils.trim(request.getPassword());
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return ResultHolder.error("user or password can't be null");
        }

        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Subject subject = SecurityUtils.getSubject();

        try {
            subject.login(token);
            if (subject.isAuthenticated()) {
                return ResultHolder.success("");
            } else {
                return ResultHolder.error("login fail");
            }
        } catch (ExcessiveAttemptsException e) {
            msg = "excessive attempts";
        } catch (LockedAccountException e) {
            msg = "the user has been locked.";
        } catch (DisabledAccountException e) {
            msg = "the user has been disabled. ";
        } catch (ExpiredCredentialsException e) {
            msg = "user expires. ";
        } catch (AuthenticationException e) {
            msg = e.getMessage();
        } catch (UnauthorizedException e) {
            msg = "not authorized. " + e.getMessage();
        }
        return ResultHolder.error(msg);
    }

    @GetMapping(value = "/signout")
    public ResultHolder logout() {
        SecurityUtils.getSubject().logout();
        return ResultHolder.success("");
    }

}
