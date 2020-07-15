package io.metersphere.controller;

import io.metersphere.commons.constants.UserSource;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LOCAL.name());
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
