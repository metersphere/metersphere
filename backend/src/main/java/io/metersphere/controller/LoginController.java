package io.metersphere.controller;

import io.metersphere.commons.constants.OssMode;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class LoginController {

    @Resource
    private UserService userService;
    @Resource
    private Environment env;

    @GetMapping(value = "/isLogin")
    public ResultHolder isLogin() {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            SessionUser user = SessionUtils.getUser();
            if (StringUtils.isBlank(user.getLanguage())) {
                user.setLanguage(LocaleContextHolder.getLocale().toString());
            }
            return ResultHolder.success(user);
        }
        String ossMode = env.getProperty("oss.mode");
        if (ossMode != null && StringUtils.equalsIgnoreCase(OssMode.CAS.name(), ossMode)) {
            return ResultHolder.error("oss");
        }
        return ResultHolder.error("");
    }

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {
        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LOCAL.name());
        return userService.login(request);
    }

    @GetMapping(value = "/currentUser")
    public ResultHolder currentUser() {
        return ResultHolder.success(SecurityUtils.getSubject().getSession().getAttribute("user"));
    }

    @GetMapping(value = "/signout")
    public ResultHolder logout() {
        String ossMode = env.getProperty("oss.mode");
        if (ossMode != null && StringUtils.equalsIgnoreCase(OssMode.CAS.name(), ossMode)) {
            return ResultHolder.error("oss");
        } else {
            SecurityUtils.getSubject().logout();
        }
        return ResultHolder.success("");
    }

    /*Get default language*/
    @GetMapping(value = "/language")
    public String getDefaultLanguage() {
        return userService.getDefaultLanguage();
    }

}
