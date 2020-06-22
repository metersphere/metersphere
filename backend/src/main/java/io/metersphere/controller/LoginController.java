package io.metersphere.controller;

import io.metersphere.base.domain.UserRole;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
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

import static io.metersphere.commons.constants.SessionConstants.ATTR_USER;

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
        return userService.login(request);
    }

    @GetMapping(value = "/signout")
    public ResultHolder logout() {
        SecurityUtils.getSubject().logout();
        return ResultHolder.success("");
    }

    /*Get default language*/
    @GetMapping(value = "/language")
    public String getDefaultLanguage() {
        return userService.getDefaultLanguage();
    }


}
