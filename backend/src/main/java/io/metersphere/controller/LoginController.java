package io.metersphere.controller;

import io.metersphere.base.domain.UserRole;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class LoginController {

    @Resource
    private UserService userService;

    @GetMapping(value = "/isLogin")
    public ResultHolder isLogin() {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return ResultHolder.success(LocaleContextHolder.getLocale());
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
                UserDTO user = (UserDTO) subject.getSession().getAttribute("user");
                // 自动选中组织，工作空间
                if (StringUtils.isBlank(user.getLastOrganizationId())) {
                    List<UserRole> userRoles = user.getUserRoles();
                    List<UserRole> test = userRoles.stream().filter(ur -> ur.getRoleId().startsWith("test")).collect(Collectors.toList());
                    List<UserRole> org = userRoles.stream().filter(ur -> ur.getRoleId().startsWith("org")).collect(Collectors.toList());
                    if (test.size() > 0) {
                        String wsId = test.get(0).getSourceId();
                        userService.switchUserRole(user, "workspace", wsId);
                    } else if (org.size() > 0) {
                        String orgId = org.get(0).getSourceId();
                        userService.switchUserRole(user, "organization", orgId);
                    }
                }
                // 返回 userDTO
                return ResultHolder.success(subject.getSession().getAttribute("user"));
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
